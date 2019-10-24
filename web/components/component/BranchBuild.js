// @flow
import React, {Fragment} from "react"
import type {BranchDetailType, ComponentType} from "./ComponentType";
import {BuildEditor} from "./BuildEditor";
import {Secure} from "../permission/Secure";
import {DateTime} from "../common/DateTime";
import {BuildStatus} from "../build/BuildStatus";
import {BuildNumber} from "../build/BuildNumber";
import {BuildLink} from "../build/BuildLink";

type Props = {
    component: ComponentType,
    branch: BranchDetailType,
    buildServers: Array<string>,
    onBuildEdited: (componentId: string, server: ?string, job: ?string) => void,
    onReloadBuild: (componentId: string) => void,
}

type State = {
    buildEdition: boolean
}

class BranchBuild extends React.Component<Props, State> {
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
        const branch = this.props.branch;
        return (
            <div style={{display: 'inline-block'}}>
                {!this.state.buildEdition &&
                    <Fragment>
                        <BuildLink
                            server={branch.buildServer}
                            job={branch.buildJob}
                            url={branch.buildUrl} />
                        <BuildNumber number={branch.buildNumber}/>
                        <BuildStatus status={branch.buildStatus}/>
                        <DateTime date={branch.buildTimestamp}/>
                        <Secure permission="component.edit">
                            <button onClick={() => this.startEditBuild()}>Edit</button>
                        </Secure>
                        <button onClick={e => this.props.onReloadBuild(component.id)}>ReloadBUILD</button>
                    </Fragment>
                }
                {this.state.buildEdition &&
                    <BuildEditor servers={this.props.buildServers}
                                 server={branch.buildServer} job={branch.buildJob}
                                 onEdited={(s, j) => this.editBuild(component.id, s, j)}
                                 onEditionCancelled={() => this.cancelEditBuild()} />
                }
            </div>
        );
    }
}

export { BranchBuild }