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
import {Tags} from "../common/Tags";

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
    component: ComponentType,
    onReload: (componentId: string) => void,
    onReloadScm: (componentId: string) => void,
    onReloadBuild: (componentId: string) => void,
    onReloadSonar: (componentId: string) => void,
    onEnable: (componentId: string) => void,
    onDisable: (componentId: string) => void,
    onDelete: (componentId: string) => void
}
function ComponentBadge(props: ComponentBadgeProps) {
    const component = props.component;
    if (component && component.id) {
        return (
            <div className='component-badge'>
                <span title={component.id}>
                    <span style={{fontSize: 14}}><b>{component.label}</b></span>
                    {component.disabled &&
                        <span>&nbsp;<s>DISABLED</s></span>
                    }
                    <span style={{fontSize: 8, marginLeft: 2}}>({component.type})</span>
                </span>
                <button onClick={e => props.onReload(component.id)}>RELOAD</button>
                <br/>
                <span style={{fontSize: 9}}>{component.mvn}</span>
                <br/>

                <ScmLink type={component.scmType} url={component.scmLocation}/>
                <span style={{fontSize: 9}}>{component.scmLocation ? component.scmLocation : 'not under scm'}</span>
                <button onClick={e => props.onReloadScm(component.id)}>SCM</button>
                <br/>

                <span key="owner">Owner {component.owner || <span style={{opacity: "0.5"}}>Unknown</span>}</span><br/>
                <span key="criticality">Criticality {component.critical || <span style={{opacity: "0.5"}}>"?"</span>}</span><br/>

                <hr/>

                Systems: <TagCloud tags={component.systems} /><br/>
                Languages: <TagCloud tags={component.languages} /><br/>
                Tags: <TagCloud tags={component.tags} /><br/>

                <hr/>

                <LinesOfCode loc={component.loc}/>
                <Coverage coverage={component.coverage}/>
                <SonarLink url={component.sonarUrl} />
                <button onClick={e => props.onReloadSonar(component.id)}>SONAR</button>
                <br/>

                <BuildLink
                    server={component.jenkinsServer}
                    job={component.jenkinsJob}
                    url={component.jenkinsUrl} />
                <BuildNumber number={component.buildNumber}/>
                <BuildStatus status={component.buildStatus}/>
                <DateTime date={component.buildTimestamp}/>
                <button onClick={e => props.onReloadBuild(component.id)}>BUILD</button>
                <br/>

                <Secure permission="component.disable">
                    {component.disabled
                        ? <button onClick={e => props.onEnable(component.id)}>Enable</button>
                        : <button onClick={e => props.onDisable(component.id)}>Disable</button>
                    }
                </Secure>
                <Secure permission="component.delete">
                    <button onClick={e => props.onDelete(component.id)}>Delete</button>
                </Secure>
            </div>
        )
    }
    else {
        return (<div>Select a component to see details</div>)
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



