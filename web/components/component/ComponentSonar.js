// @flow
import React, {Fragment} from "react"
import type {ComponentType} from "./ComponentType";
import {SonarEditor} from "./SonarEditor";
import {LinesOfCode} from "./LinesOfCode";
import {Coverage} from "./Coverage";
import {SonarLink} from "./SonarLink";
import {Secure} from "../permission/Secure";

type Props = {
    component: ComponentType,
    sonarServers: Array<string>,
    onSonarEdited: (componentId: string, server: ?string) => void,
    onReloadSonar: (componentId: string) => void,
}

type State = {
    sonarEdition: boolean,
}

class ComponentSonar extends React.Component<Props, State> {
    constructor(props: Props) {
        super(props);
        this.state = {
            sonarEdition: false
        }
    }
    startEditSonar() {
        this.setState({sonarEdition: true});
        //$FlowFixMe
        //setTimeout(() => this.sonarServerInput.current && this.sonarServerInput.current.focus(), 0)
    }
    cancelEditSonar() {
        this.setState({sonarEdition: false});
    }
    editSonar(componentId: string, server?: ?string) {
        this.setState({sonarEdition: false});
        this.props.onSonarEdited(componentId, server)
    }

    render() {
        const component = this.props.component;
        return (
            <div>
                {!this.state.sonarEdition &&
                    <Fragment>
                        <div style={{display: 'inline-block'}}>
                            <b>Code Metrics: </b>
                            <br/>
                            <LinesOfCode loc={component.loc}/>
                            <Coverage coverage={component.coverage}/>
                            <SonarLink url={component.sonarUrl}/>
                        </div>
                        <div style={{float: 'right'}}>
                            <Secure permission="component.edit">
                                <button onClick={() => this.startEditSonar()}>Edit</button>
                            </Secure>
                            <button onClick={e => this.props.onReloadSonar(component.id)}>ReloadSONAR</button>
                        </div>
                    </Fragment>
                }
                {this.state.sonarEdition &&
                <SonarEditor servers={this.props.sonarServers}
                             server={component.sonarServer}
                             onEdited={s => this.editSonar(component.id, s)}
                             onEditionCancelled={() => this.cancelEditSonar()} />
                }
            </div>
        );
    }
}

export { ComponentSonar }
