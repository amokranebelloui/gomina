// @flow

import * as React from "react"
import type {NewComponentType} from "./NewComponent";
import {NewComponent} from "./NewComponent";
import * as axios from "axios";
import {ComponentAdded} from "./ComponentAdded";
import type {ComponentType} from "./ComponentType";
import {Button, Dialog, Classes} from "@blueprintjs/core";

type Props = {
    systemsContext?: Array<string>,
    onComponentAdded?: ComponentType => void,
}

type State = {
    buildServers: Array<string>,
    sonarServers: Array<string>,
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
            buildServers: [],
            sonarServers: [],
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
    componentDidMount() {
        const thisComponent = this;
        axios.get('/data/components/build/servers')
            .then(response => thisComponent.setState({buildServers: response.data}))
            .catch(() => thisComponent.setState({buildServers: []}));
        axios.get('/data/components/sonar/servers')
            .then(response => thisComponent.setState({sonarServers: response.data}))
            .catch(() => thisComponent.setState({sonarServers: []}));
    }
    render() {
        return (
            <div>
                {(!this.state.adding || this.state.successful) &&
                    <Button text="Add Component" onClick={e => this.newComponent()} />
                }

                <Dialog icon="projects" title="New Component"
                        className={Classes.OVERLAY_SCROLL_CONTAINER} autoFocus={true} enforceFocus={true} usePortal={true}
                        isOpen={this.state.adding} onClose={e => this.cancelAddComponent()}>
                    <div className={Classes.DIALOG_BODY}>
                        {(this.state.successful
                        ? this.state.data && <ComponentAdded component={this.state.data}/>
                        : <NewComponent
                        systemsContext={this.props.systemsContext}
                        sonarServers={this.state.sonarServers}
                        buildServers={this.state.buildServers}
                        processing={this.state.processing}
                        error={this.state.error}
                        onAdd={d => {this.addComponent(d) }}
                        onCancel={e => this.cancelAddComponent()} />
                        )}
                    </div>
                </Dialog>


            </div>
        )
    }
}

export { AddComponent }