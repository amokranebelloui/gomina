// @flow

import * as React from "react"

type T = any;

type Props = {
    type?: ?string,
    editionForm: () => any,
    successDisplay: T => any,
    action: () => ?Promise<any>,
    onEnterAdd?: () => void,
    onItemAdded?: T => void,
    onAddError?: any => void,
}

type State = {
    adding: boolean,
    processing: boolean,
    successful: boolean,
    data: ?any,
    error?: ?string
}

class InlineAdd extends React.Component<Props, State> {
    constructor(props: Props) {
        super(props);
        this.state = {
            adding: false,
            processing: false,
            successful: false,
            data: null,
            error: null
        }
    }
    enterAdd() {
        this.setState({
            adding: true,
            processing: false,
            successful: false,
            error: null
        })
        this.props.onEnterAdd && this.props.onEnterAdd()
    }
    confirmAdd() {
        console.info("Confirm Add " + (this.props.type || "Item"));
        this.setState({
            processing: true,
            successful: false,
            error: null
        });
        const thisComponent = this;
        const action = this.props.action();
        action && action
            .then(response => {
                console.log((thisComponent.props.type || "Item") + " Added", response.data);
                thisComponent.setState({
                    processing: false,
                    successful: true,
                    data: response.data
                });
                thisComponent.props.onItemAdded && thisComponent.props.onItemAdded(response.data);
                setTimeout(
                    () => {
                        if (this.state.successful) {
                            thisComponent.setState({
                                adding: false
                            })
                        }
                    }, 8000
                );
            })
            .catch(function (error) {
                const errorMsg = error.response && error.response.data || error.response || error;
                console.log("Add " + (thisComponent.props.type || "Item") + " Error", errorMsg);
                thisComponent.setState({
                    processing: false,
                    successful: false,
                    error: errorMsg});
                thisComponent.props.onAddError && thisComponent.props.onAddError(errorMsg);
            });
    }
    cancelAdd() {
        this.setState({
            adding: false,
            processing: false,
            successful: false,
            error: null
        })
    }
    render() {
        /*

                <br/>
                <br/>
                <span style={{opacity: .4}}>{JSON.stringify(this.state)}</span>
         */
        return (
            <div>
                {(!this.state.adding || this.state.successful) &&
                    <input type="button" value={"Add " + (this.props.type || "Item")} onClick={e => this.enterAdd()}/>
                }
                {this.state.adding &&
                (this.state.successful
                        ? this.props.successDisplay(this.state.data)
                        : <div>
                            <div>
                                {this.props.editionForm()}
                            </div>
                            <hr style={{marginTop: '4px', marginBottom: '4px'}} />
                            {this.state.error &&
                                <span style={{color: 'red'}}><i>Error: {this.state.error}</i></span>
                            }
                            {!this.state.processing
                                ? <input type="button" value="Add" onClick={e => this.confirmAdd()} />
                                : <span><i>Adding...</i></span>
                            }
                            <input type="button" value="Cancel" onClick={e => this.cancelAdd()} />
                        </div>
                )
                }
            </div>
        )
    }
}

export { InlineAdd }