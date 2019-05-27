// @flow
import React, {Fragment} from "react"
import {ScmLink} from "./ScmLink";
import {Secure} from "../permission/Secure";
import {ScmEditor} from "./ScmEditor";
import {Badge} from "../common/Badge";
import {DateTime} from "../common/DateTime";
import type {ComponentType} from "./ComponentType";

type Props = {
    component: ComponentType,
    onScmEdited: (componentId: string, type: string, url: string, path: ?string, hasMetadata: ?boolean) => void,
    onReloadScm: (componentId: string) => void,
}

type State = {
    scmEdition: boolean,
}

class ComponentScm extends React.Component<Props, State> {
    constructor(props: Props) {
        super(props);
        this.state = {
            scmEdition: false,
        }
    }
    startEditScm() {
        this.setState({scmEdition: true});
        //$FlowFixMe
        //setTimeout(() => this.textInput.current && this.textInput.current.focus(), 0)
    }
    cancelEditScm() {
        this.setState({scmEdition: false});
    }
    editScm(componentId: string, type: string, url: string, path: ?string, hasMetadata: ?boolean) {
        this.setState({scmEdition: false});
        this.props.onScmEdited(componentId, type, url, path, hasMetadata)
    }
    render() {
        const component = this.props.component;
        return (
            <div>
                {!this.state.scmEdition &&
                    <div>
                        <div style={{float: 'right'}}>
                            <Secure permission="component.edit">
                                <button onClick={() => this.startEditScm()}>Edit</button>
                            </Secure>
                            <button onClick={() => this.props.onReloadScm(component.id)}>ReloadSCM</button>
                        </div>
                        <div style={{display: 'inline'}}>
                            <b>SCM: </b>
                            <br/>
                            <ScmLink type={component.scmType} />&nbsp;
                            <span style={{fontSize: 9}}>{component.scmLocation ? component.scmLocation : 'not under scm'}</span>
                            &nbsp;
                            <br/>
                            {component.hasMetadata ? <span><b>Has metadata</b></span> : <span>No Metadata</span>}
                        </div>
                    </div>
                }
                {this.state.scmEdition &&
                    <div>
                        <ScmEditor type={component.scmType} url={component.scmUrl} path={component.scmPath} hasMetadata={component.hasMetadata}
                                   onEdited={(type, url, path, md) => this.editScm(component.id, type, url, path, md)}
                                   onEditionCancelled={() => this.cancelEditScm()} />
                    </div>
                }

                Last commit: <DateTime date={component.lastCommit} />&nbsp;
                {(component.commitActivity != undefined && component.commitActivity != 0) &&
                    <Badge backgroundColor='lightgray' color='white' title="Activity Score">{component.commitActivity}</Badge>
                }
                <br/>
                Commit to Release: {component.commitToRelease || '?'} days
                <br/>
            </div>
        );
    }
}

export { ComponentScm }