// @flow
import React, {Fragment} from "react";
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
import {Label} from "../common/Label";
import {EditableDate} from "../common/EditableDate";
import {StarRating} from "../common/StarRating";
import {Branch, sortBranchesDetails} from "../commitlog/Branch";
import queryString from "query-string"

function ComponentHeader(props: {}) {
    return (
        <div className='component-row'>
            <div className='summary'><b>Component</b></div>
            <div className='loc'><b>LOC</b></div>
            <div className='coverage'><b>Coverage</b></div>
            <div className='scm'><b>SCM / LastCommit / Activity</b></div>
            <div className='build'><b>Build</b></div>
        </div>
    )
}

type ComponentSummaryProps = {
    component: ComponentType,
    knowledge: ?number,
}
class ComponentSummary extends React.Component<ComponentSummaryProps> {
    render() {
        const component = this.props.component;
        const systems = component.systems || [];
        const languages = component.languages || [];
        const tags = component.tags || [];
        let deployedStyle = {color: 'white', backgroundColor: '#ebebeb'};
        return (
            <div className='component-row'>
                <div className='summary'>
                    <span className="items">
                        <Link to={"/component/" + component.id}>
                            <span style={{fontSize: 14}}>{component.label}</span>
                            {component.disabled &&
                                <span>&nbsp;<s>DISABLED</s></span>
                            }
                        </Link>
                        <span style={{fontSize: 8, marginLeft: 2}}>({component.type})</span>
                        <Secure permission="component.knowledge">
                            <StarRating value={this.props.knowledge} />
                        </Secure>
                        <Version version={component.released} />
                        <Version version={component.latest} displaySnapshotRevision={true} {...deployedStyle} />
                        <UnreleasedChangeCount changes={component.changes} />
                    </span>
                    <br/>
                    <span className="items">
                        {component.artifactId && <Fragment><span style={{fontSize: 8}}>{component.artifactId}</span> | </Fragment>}
                        {systems.map(system =>
                            <span style={{fontSize: 8}}>{system}</span>
                        )}
                        |
                        {languages.map(tag =>
                            <span style={{fontSize: 8}}>{tag}</span>
                        )}
                        |
                        {tags.map(tag =>
                            <span style={{fontSize: 8}}>{tag}</span>
                        )}
                    </span>
                </div>
                <div className='loc'><LinesOfCode loc={component.loc} /></div>
                <div className='coverage'><Coverage coverage={component.coverage} /></div>
                <div className='scm'>
                    <ScmLink type={component.scmType} url={component.scmLocation} changes={component.changes} />
                    <DateTime date={component.lastCommit} />&nbsp;
                    {(component.commitActivity != undefined && component.commitActivity != 0) &&
                        <Badge backgroundColor='#EAA910' color='white'>{component.commitActivity}</Badge>
                    }
                </div>
                <div className='build'>
                    <ul className='item-container items'>
                        <li><BuildStatus status={component.buildStatus}/></li>
                        <li><BuildLink server={component.jenkinsServer} url={component.jenkinsUrl} /></li>
                        <li><DateTime date={component.buildTimestamp}/></li>
                    </ul>
                </div>
            </div>
        )
    }
}

type ComponentBadgeProps = {
    buildServers: Array<string>,
    sonarServers: Array<string>,
    component: ComponentType,
    knowledge: ?number,
    onReload: (componentId: string) => void,
    onReloadScm: (componentId: string) => void,
    onReloadBuild: (componentId: string) => void,
    onReloadSonar: (componentId: string) => void,
    onLabelEdited: (componentId: string, label: string) => void,
    onTypeEdited: (componentId: string, type: string) => void,
    onArtifactIdEdited: (componentId: string, artifactId: string) => void,
    onScmEdited: (componentId: string, type: string, url: string, path: ?string, hasMetadata: ?boolean) => void,
    onInceptionDateEdited: (componentId: string, date: ?string) => void,
    onOwnerEdited: (componentId: string, owner: ?string) => void,
    onCriticityEdited: (componentId: string, criticity: ?string) => void,
    onSonarEdited: (componentId: string, server: ?string) => void,
    onBuildEdited: (componentId: string, server: ?string, job: ?string) => void,
    onSystemAdd: (componentId: string, system: string) => void,
    onSystemDelete: (componentId: string, system: string) => void,
    onLanguageAdd: (componentId: string, language: string) => void,
    onLanguageDelete: (componentId: string, language: string) => void,
    onTagAdd: (componentId: string, tags: string) => void,
    onTagDelete: (componentId: string, tags: string) => void,
    onKnowledgeChange: (componentId: string, knowledge: number) => void,
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
    editScm(componentId: string, type: string, url: string, path: ?string, hasMetadata: ?boolean) {
        this.setState({scmEdition: false});
        this.props.onScmEdited(componentId, type, url, path, hasMetadata)
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
                        <StarRating value={this.props.knowledge} editable={true}
                                    onRatingChange={k => this.props.onKnowledgeChange && this.props.onKnowledgeChange(component.id, k)} />
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
                        <Label label={component.artifactId} altText={'no artifact id'} />
                    }>
                        <EditableLabel label={component.artifactId} altText={'no artifact id'}
                                       onLabelEdited={artifactId => this.props.onArtifactIdEdited(component.id, artifactId)} />
                    </Secure>
                    <br/>

                    {!this.state.scmEdition &&
                    <span>
                        <ScmLink type={component.scmType} />&nbsp;
                        <span style={{fontSize: 9}}>{component.scmLocation ? component.scmLocation : 'not under scm'}</span>
                        &nbsp;
                        <Secure permission="component.edit">
                            <button onClick={() => this.startEditScm()}>Edit</button>
                        </Secure>
                        <button onClick={() => this.props.onReloadScm(component.id)}>ReloadSCM</button>
                        <br/>
                        {component.hasMetadata ? <span><b>Has metadata</b></span> : <span>No Metadata</span>}
                    </span>
                    }
                    {this.state.scmEdition &&
                    <ScmEditor type={component.scmType} url={component.scmUrl} path={component.scmPath} hasMetadata={component.hasMetadata}
                               onEdited={(type, url, path, md) => this.editScm(component.id, type, url, path, md)}
                               onEditionCancelled={() => this.cancelEditScm()} />
                    }
                    <br/>
                    
                    Last commit: <DateTime date={component.lastCommit} />&nbsp;
                    {(component.commitActivity != undefined && component.commitActivity != 0) &&
                    <Badge backgroundColor='#EAA910' color='white'>{component.commitActivity}</Badge>
                    }
                    <br/>
                    Commit to Release: {component.commitToRelease || '?'} days
                    <br/>

                    <b>Type</b>&nbsp;
                    <Secure permission="component.edit" fallback={
                        <span>{component.type}</span>
                    }>
                        <EditableLabel label={component.type}
                                       onLabelEdited={t => this.props.onTypeEdited(component.id, t)}/>
                    </Secure>
                    <br/>

                    <b>Inception</b>&nbsp;
                    <Secure permission="component.edit" fallback={
                        <DateTime date={component.inceptionDate}  />
                    }>
                        <EditableDate date={component.inceptionDate}
                                      onDateEdited={inceptionDate => this.props.onInceptionDateEdited && this.props.onInceptionDateEdited(component.id, inceptionDate)} />
                    </Secure>
                    <br/>

                    <b>Owner</b>&nbsp;
                    <Secure permission="component.edit" fallback={
                        <Label label={component.owner}  />
                    }>
                        <EditableLabel label={component.owner}
                                       onLabelEdited={o => this.props.onOwnerEdited && this.props.onOwnerEdited(component.id, o)} />
                    </Secure>
                    <br/>

                    <b>Criticity</b>&nbsp;
                    <Secure permission="component.edit" fallback={
                        <Label label={component.criticity}  />
                    }>
                        <EditableLabel label={component.criticity}
                                       onLabelEdited={inceptionDate => this.props.onCriticityEdited && this.props.onCriticityEdited(component.id, inceptionDate)} />
                    </Secure>
                    <br/>

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
    component: ComponentType,
    location: any
}

function ComponentMenu(props: ComponentMenuProps) {
    const component = props.component;
    /*
    <Link to={'/component/' + component.id}>Main</Link><span>|</span>
    */
    const params = queryString.parse(props.location.search);
    const pathname = props.location.pathname;
    return (
        component &&
            <div style={{marginTop: '4px'}}>
                <div className="items" style={{display: 'inline'}}>
                    <Link to={'/component/' + props.component.id}>
                        <Badge backgroundColor={pathname === '/component/'+props.component.id ? 'lightgray' : null}>HOME</Badge>
                    </Link>

                    {component.branches && sortBranchesDetails(component.branches)
                        .map(branch =>
                            <span key={branch.name} title={"from: " + (branch.origin || "") + " rev:" + (branch.originRevision || "")}>
                                <Link to={'/component/' + props.component.id + '/scm?branchId=' + branch.name}>
                                    <Branch branch={branch} selected={pathname.match(RegExp('/component/.*/scm')) && params.branchId === branch.name || false} />
                                </Link>
                            </span>
                        )
                    }

                    <Link to={'/component/' + props.component.id + '/api'}>
                        <Badge backgroundColor={pathname.match(RegExp('/component/.*/api')) ? 'lightgray' : null}>API</Badge>
                    </Link>
                    <Link to={'/component/' + props.component.id + '/dependencies'}>
                        <Badge backgroundColor={pathname.match(RegExp('/component/.*/dependencies')) ? 'lightgray' : null}>DEPS</Badge>
                    </Link>
                    <Link to={'/component/' + props.component.id + '/libraries'}>
                        <Badge backgroundColor={pathname.match(RegExp('/component/.*/libraries')) ? 'lightgray' : null}>LIBS</Badge>
                    </Link>
                    <Link to={'/component/' + props.component.id + '/events'}>
                        <Badge backgroundColor={pathname.match(RegExp('/component/.*/events')) ? 'lightgray' : null}>EVENTS</Badge>
                    </Link>

                </div>
                <div className="items" style={{display: 'inline', float: 'right'}}>
                    {component.docFiles && component.docFiles.map(doc =>
                        <Link key={doc} to={'/component/' + props.component.id + '/doc/' + doc}>
                            <Badge backgroundColor={pathname.match(RegExp('/component/.*/doc/'+doc)) ? 'lightgray' : null}>{doc}</Badge>
                        </Link>
                    )}
                </div>
                <div style={{clear: 'both'}}></div>
            </div>
        || null
    )
}

export {ComponentHeader, ComponentSummary, ComponentBadge, ComponentMenu}



