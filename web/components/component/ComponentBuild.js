// @flow
import React, {Fragment} from "react"
import type {ComponentType} from "./ComponentType";
import {BuildEditor} from "./BuildEditor";
import {Secure} from "../permission/Secure";
import {DateTime} from "../common/DateTime";
import {BuildStatus} from "../build/BuildStatus";
import {BuildNumber} from "../build/BuildNumber";
import {BuildLink} from "../build/BuildLink";

type Props = {
    component: ComponentType,
    buildServers: Array<string>,
    onBuildEdited: (componentId: string, server: ?string, job: ?string) => void,
    onReloadBuild: (componentId: string) => void,
}

type State = {
    buildEdition: boolean
}

class ComponentBuild extends React.Component<Props, State> {
    constructor(props: Props) {
        super(props);
        this.state = {
            buildEdition: false
        }
    }
    startEditBuild() {
        this.setState({buildEdition: true});
        //$FlowFixMe
        //setTimeout(() => this.buildServerInput.current && this.buildServerInput.current.focus(), 0)
    }
    cancelEditBuild() {
        this.setState({buildEdition: false});
    }
    editBuild(componentId: string, server: ?string, job?: ?string) {
        this.setState({buildEdition: false});
        this.props.onBuildEdited(componentId, server, job)
    }
    render() {
        const component = this.props.component;
        return (
            <div>
                {!this.state.buildEdition &&
                    <Fragment>
                        <div style={{display: 'inline-block'}}>
                            <b>Build: </b>
                            <br/>
                            <BuildLink
                                server={component.jenkinsServer}
                                job={component.jenkinsJob}
                                url={component.jenkinsUrl} />
                            <BuildNumber number={component.buildNumber}/>
                            <BuildStatus status={component.buildStatus}/>
                            <DateTime date={component.buildTimestamp}/>
                        </div>
                        <div style={{float: 'right'}}>
                            <Secure permission="component.edit">
                                <button onClick={() => this.startEditBuild()}>Edit</button>
                            </Secure>
                            <button onClick={e => this.props.onReloadBuild(component.id)}>ReloadBUILD</button>
                        </div>
                    </Fragment>
                }
                {this.state.buildEdition &&
                <div>
                    <BuildEditor servers={this.props.buildServers}
                                 server={component.jenkinsServer} job={component.jenkinsJob}
                                 onEdited={(s, j) => this.editBuild(component.id, s, j)}
                                 onEditionCancelled={() => this.cancelEditBuild()} />
                </div>
                }
            </div>
        );
    }
}

export { ComponentBuild }