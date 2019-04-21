// @flow
import * as React from "react";
import Link from "react-router-dom/es/Link";
import {Version} from "../common/Version";
import {LinesOfCode} from "./LinesOfCode";
import {Coverage} from "./Coverage";
import {UnreleasedChangeCount} from "./UnreleasedChangeCount";
import {ScmLink} from "./ScmLink";
import {BuildLink} from "../build/BuildLink";
import {DateTime} from "../common/DateTime";
import {Badge} from "../common/Badge";
import {BuildStatus} from "../build/BuildStatus";
import {BuildNumber} from "../build/BuildNumber";
import {SonarLink} from "./SonarLink";
import "../common/items.css"
import type {ComponentType} from "./ComponentType";
import {Secure} from "../permission/Secure";
import {TagCloud} from "../common/TagCloud";
import {TagEditor} from "../common/TagEditor";
import {EditableLabel} from "../common/EditableLabel";
import {ScmEditor} from "./ScmEditor";
import {BuildEditor} from "./BuildEditor";
import {SonarEditor} from "./SonarEditor";

function ComponentHeader(props: {}) {
    return (
        <div className='component-row'>
            <div className='summary'><b>Component</b></div>
            <div className='released'><b>Released</b></div>
            <div className='latest'><b>Latest</b></div>
            <div className='loc'><b>LOC</b></div>
            <div className='coverage'><b>Coverage</b></div>
            <div className='scm'><b>SCM</b></div>
            <div className='last-commit'><b>LastCommit (Activity)</b></div>
            <div className='build'><b>Build</b></div>
        </div>
    )
}

type ComponentSummaryProps = {
    component: ComponentType
}
class ComponentSummary extends React.Component<ComponentSummaryProps> {
    render() {
        const component = this.props.component;
        const systems = component.systems || [];
        const languages = component.languages || [];
        const tags = component.tags || [];
        return (
            <div className='component-row'>
                <div className='summary'>
                    <span title={component.id}>
                        <Link to={"/component/" + component.id}>
                            <span style={{fontSize: 14}}>{component.label}</span>
                            {component.disabled &&
                                <span>&nbsp;<s>DISABLED</s></span>
                            }
                        </Link>
                        &nbsp;
                        <UnreleasedChangeCount changes={component.changes} />
                        <span style={{fontSize: 8, marginLeft: 2}}>({component.type})</span>
                        {systems.map(system =>
                            <span style={{fontSize: 8, marginLeft: 2}}>{system}</span>
                        )}
                        |
                        {languages.map(tag =>
                            <span style={{fontSize: 8, marginLeft: 2}}>{tag}</span>
                        )}
                        |
                        {tags.map(tag =>
                                <span style={{fontSize: 8, marginLeft: 2}}>{tag}</span>
                        )}
                    </span>
                    <br/>
                    <span style={{fontSize: 8}}>{component.mvn}</span>
                </div>
                <div className='released'><Version version={component.released} /></div>
                <div className='latest'><Version version={component.latest} /></div>
                <div className='loc'><LinesOfCode loc={component.loc} /></div>
                <div className='coverage'><Coverage coverage={component.coverage} /></div>
                <div className='scm'><ScmLink type={component.scmType} url={component.scmLocation} changes={component.changes} /></div>
                <div className='last-commit'>
                    <DateTime date={component.lastCommit} />&nbsp;
                    {(component.commitActivity != undefined && component.commitActivity != 0) &&
                        <Badge backgroundColor='#EAA910' color='white'>{component.commitActivity}</Badge>
                    }
                </div>
                <div className='build'>
                    <BuildLink server={component.jenkinsServer} url={component.jenkinsUrl} />
                    <BuildStatus status={component.buildStatus}/>
                    <br/>
                    <DateTime date={component.buildTimestamp}/>
                </div>
                <Secure permission="component.knowledge">
                    <div><span>(Level)</span></div>
                </Secure>
            </div>
        )
    }
}

type ComponentBadgeProps = {
    buildServers: Array<string>,
    sonarServers: Array<string>,
    component: ComponentType,
    onReload: (componentId: string) => void,
    onReloadScm: (componentId: string) => void,
    onReloadBuild: (componentId: string) => void,
    onReloadSonar: (componentId: string) => void,
    onLabelEdited: (componentId: string, label: string) => void,
    onTypeEdited: (componentId: string, type: string) => void,
    onArtifactIdEdited: (componentId: string, artifactId: string) => void,
    onScmEdited: (componentId: string, type: string, url: string, path: ?string) => void,
    onSonarEdited: (componentId: string, server: ?string) => void,
    onBuildEdited: (componentId: string, server: ?string, job: ?string) => void,
    onSystemAdd: (componentId: string, system: string) => void,
    onSystemDelete: (componentId: string, system: string) => void,
    onLanguageAdd: (componentId: string, language: string) => void,
    onLanguageDelete: (componentId: string, language: string) => void,
    onTagAdd: (componentId: string, tags: string) => void,
    onTagDelete: (componentId: string, tags: string) => void,
    onEnable: (componentId: string) => void,
    onDisable: (componentId: string) => void,
    onDelete: (componentId: string) => void
}
type ComponentBadgeState = {
    scmEdition: boolean,
    sonarEdition: boolean,
    buildEdition: boolean
}

class ComponentBadge extends React.Component<ComponentBadgeProps, ComponentBadgeState> {
    constructor(props: ComponentBadgeProps) {
        super(props);
        this.state = {
            scmEdition: false,
            sonarEdition: false,
            buildEdition: false
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
    editScm(componentId: string, type: string, url: string, path: ?string) {
        this.setState({scmEdition: false});
        this.props.onScmEdited(componentId, type, url, path)
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
        if (component && component.id) {
            return (
                <div className='component-badge'>
                    <span title={component.id}>
                        <Secure permission="component.edit" fallback={
                            <span style={{fontSize: 16, fontWeight: 'bold'}}>{component.label}</span>
                        }>
                            <EditableLabel label={component.label} style={{fontSize: 16, fontWeight: 'bold'}}  altText={'no label'}
                                           onLabelEdited={l => this.props.onLabelEdited(component.id, l)}/>
                        </Secure>
                        &nbsp;
                        <button onClick={e => this.props.onReload(component.id)}>RELOAD</button>
                        {component.disabled && <span>&nbsp;<s>DISABLED</s></span>}
                    </span>
                    &nbsp;
                    <Secure permission="component.disable">
                        {component.disabled
                            ? <button onClick={e => this.props.onEnable(component.id)}>Enable</button>
                            : <button onClick={e => this.props.onDisable(component.id)}>Disable</button>
                        }
                    </Secure>
                    <br/>

                    <Secure permission="component.edit" fallback={
                        <span style={{fontSize: 9}}>{component.mvn || 'no artifact id'}</span>
                    }>
                        <EditableLabel label={component.mvn} altText={'no artifact id'}
                                       onLabelEdited={artifactId => this.props.onArtifactIdEdited(component.id, artifactId)}/>
                    </Secure>
                    <br/>

                    {!this.state.scmEdition &&
                    <div>
                        <ScmLink type={component.scmType} />&nbsp;
                        <span style={{fontSize: 9}}>{component.scmLocation ? component.scmLocation : 'not under scm'}</span>
                        &nbsp;
                        <Secure permission="component.edit">
                            <button onClick={() => this.startEditScm()}>Edit</button>
                        </Secure>
                        <button onClick={() => this.props.onReloadScm(component.id)}>ReloadSCM</button>
                        Last commit: <DateTime date={component.lastCommit} />&nbsp;
                        {(component.commitActivity != undefined && component.commitActivity != 0) &&
                        <Badge backgroundColor='#EAA910' color='white'>{component.commitActivity}</Badge>
                        }
                        <br/>
                        Commit to Release: {component.commitToRelease || '?'} days
                    </div>
                    }
                    {this.state.scmEdition &&
                    <ScmEditor type={component.scmType} url={component.scmUrl} path={component.scmPath}
                               onEdited={(type, url, path) => this.editScm(component.id, type, url, path)}
                               onEditionCancelled={() => this.cancelEditScm()} />
                    }
                    <br/>

                    <span key="type">Type &nbsp;
                    <Secure permission="component.edit" fallback={
                        <span>{component.type}</span>
                    }>
                        <EditableLabel label={component.type}
                                       onLabelEdited={t => this.props.onTypeEdited(component.id, t)}/>
                    </Secure>
                    <br/></span>

                    <span key="owner">Owner {component.owner || <span style={{opacity: "0.5"}}>Unknown</span>}</span><br/>
                    <span key="criticality">Criticality {component.critical || <span style={{opacity: "0.5"}}>"?"</span>}</span><br/>

                    <hr/>

                    Systems:
                    <Secure permission="component.edit" fallback={
                        <TagCloud tags={component.systems} />
                    }>
                        <TagEditor tags={component.systems}
                                   onTagAdd={t => this.props.onSystemAdd(component.id, t)}
                                   onTagDelete={t => this.props.onSystemDelete(component.id, t)}
                        />
                    </Secure>
                    <br/>
                    Languages:
                    <Secure permission="component.edit" fallback={
                        <TagCloud tags={component.languages} />
                    }>
                        <TagEditor tags={component.languages}
                                   onTagAdd={t => this.props.onLanguageAdd(component.id, t)}
                                   onTagDelete={t => this.props.onLanguageDelete(component.id, t)}
                        />
                    </Secure>
                    <br/>
                    Tags:
                    <Secure permission="component.edit" fallback={
                        <TagCloud tags={component.tags} />
                    }>
                        <TagEditor tags={component.tags}
                                   onTagAdd={t => this.props.onTagAdd(component.id, t)}
                                   onTagDelete={t => this.props.onTagDelete(component.id, t)}
                        />
                    </Secure>
                    <br/>

                    <hr/>

                    {!this.state.sonarEdition &&
                    <div>
                        <LinesOfCode loc={component.loc}/>
                        <Coverage coverage={component.coverage}/>
                        <SonarLink url={component.sonarUrl}/>
                        <Secure permission="component.edit">
                            <button onClick={() => this.startEditSonar()}>Edit</button>
                        </Secure>
                        <button onClick={e => this.props.onReloadSonar(component.id)}>ReloadSONAR</button>
                    </div>
                    }
                    {this.state.sonarEdition &&
                    <SonarEditor servers={this.props.sonarServers}
                                 server={component.sonarServer}
                                 onEdited={s => this.editSonar(component.id, s)}
                                 onEditionCancelled={() => this.cancelEditSonar()} />
                    }
                    <br/>

                    {!this.state.buildEdition &&
                    <div>
                        <BuildLink
                            server={component.jenkinsServer}
                            job={component.jenkinsJob}
                            url={component.jenkinsUrl} />
                        <BuildNumber number={component.buildNumber}/>
                        <BuildStatus status={component.buildStatus}/>
                        <DateTime date={component.buildTimestamp}/>
                        <Secure permission="component.edit">
                            <button onClick={() => this.startEditBuild()}>Edit</button>
                        </Secure>
                        <button onClick={e => this.props.onReloadBuild(component.id)}>ReloadBUILD</button>
                    </div>
                    }
                    {this.state.buildEdition &&
                    <div>
                        <BuildEditor servers={this.props.buildServers}
                                     server={component.jenkinsServer} job={component.jenkinsJob}
                                     onEdited={(s, j) => this.editBuild(component.id, s, j)}
                                     onEditionCancelled={() => this.cancelEditBuild()} />
                    </div>
                    }

                    <br/>

                    <hr/>

                    <Secure permission="component.delete">
                        <button onClick={e => this.props.onDelete(component.id)}>Delete</button>
                    </Secure>
                    <br/>

                </div>
            )
        }
        else {
            return (<div>Select a component to see details</div>)
        }
    }
}

type ComponentMenuProps = {
    component: ComponentType
}
function ComponentMenu(props: ComponentMenuProps) {
    const component = props.component;
    return (
        component &&
        <div className="items">
            <Link to={'/component/' + component.id}>Main</Link>
            <span>|</span>
            {component.branches && component.branches
                .map(branch =>
                    <span key={branch.name} title={"from: " + (branch.origin || "") + " rev:" + (branch.originRevision || "")}>
                        <Link to={'/component/' + props.component.id + '/scm?branchId=' + branch.name}>
                            {branch.name}
                        </Link>
                    </span>
                )
            }
            <span>|</span>
            {component.docFiles && component.docFiles
                .map(doc => <Link key={doc} to={'/component/' + props.component.id + '/doc/' + doc}>{doc}</Link>)
            }
        </div>
        || null
    )
}

export {ComponentHeader, ComponentSummary, ComponentBadge, ComponentMenu}



