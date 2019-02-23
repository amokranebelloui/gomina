// @flow
import * as React from "react"

type NewComponentData = {
    id: string
}

type Props = {
    processing: boolean,
    error?: ?string,
    onAdd: NewComponentData => void,
    onCancel: () => void
}
type State = {
    id: string,
    scmUrl?: ?string
}
class NewComponent extends React.Component<Props, State> {
    constructor(props: Props) {
        super(props);
        this.state = {
            id: ''
        };
    }
    render() {
        return (
            <div>
                <b>Add new component</b>
                <br/>
                ID <input type="text" name="id" placeholder="Component Id" onChange={e => this.setState({id: e.target.value})} />
                <br/>
                SCM <input type="text" name="scm" placeholder="SCM://url" />
                <br/>
                {this.props.error &&
                    <span style={{color: 'red'}}><i>Error: {this.props.error}</i></span>
                }
                {!this.props.processing
                    ? <input type="button" value="Add" onClick={e => this.props.onAdd({id: this.state.id})} />
                    : <span><i>Adding</i></span>
                }
                <input type="button" value="Cancel" onClick={e => this.props.onCancel()} />
            </div>
        )
    }
}


export { NewComponent }
export type { NewComponentData }
