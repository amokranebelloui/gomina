// @flow

import * as React from "react"
import type {NewComponentType} from "./NewComponent";
import {NewComponent} from "./NewComponent";
import * as axios from "axios";
import {ComponentAdded} from "./ComponentAdded";
import type {ComponentType} from "./ComponentType";


type Props = {
    systemsContext?: Array<string>,
    onComponentAdded?: ComponentType => void,
}

type State = {
    adding: boolean,
    processing: boolean,
    successful: boolean,
    data: ?ComponentType,
    error?: ?string
}

class AddComponent extends React.Component<Props, State> {
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
    // New Component
    newComponent() {
        this.setState({
            adding: true,
            processing: false,
            successful: false,
            error: null
        })
    }
    addComponent(newComponentData: NewComponentType) {
        console.info("Adding Component", newComponentData);
        this.setState({
            processing: true,
            successful: false,
            error: null
        });
        const thisComponent = this;
        axios.post('/data/components/add?componentId=' + newComponentData.id, newComponentData)
            .then(response => {
                console.log("component added", response.data);
                thisComponent.setState({
                    processing: false,
                    successful: true,
                    data: response.data
                });
                thisComponent.props.onComponentAdded && thisComponent.props.onComponentAdded(response.data);
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
                thisComponent.setState({
                    processing: false,
                    successful: false,
                    error: error.response.data});
                console.log("component add error", error.response);
            });
    }
    cancelAddComponent() {
        this.setState({
            adding: false,
            processing: false,
            successful: false,
            error: null
        })
    }
    render() {
        return (
            <div>
                {(!this.state.adding || this.state.successful) &&
                <input type="button" value="Add Component" onClick={e => this.newComponent()}/>
                }
                {this.state.adding &&
                (this.state.successful
                        ? this.state.data && <ComponentAdded component={this.state.data}/>
                        : <NewComponent
                            systemsContext={this.props.systemsContext}
                            processing={this.state.processing}
                            error={this.state.error}
                            onAdd={d => {this.addComponent(d) }}
                            onCancel={e => this.cancelAddComponent()} />
                )
                }
            </div>
        )
    }
}

export { AddComponent }