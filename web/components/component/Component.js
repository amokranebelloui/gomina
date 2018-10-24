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
    component: ComponentType,
    loggedUser?: ?string
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
                {this.props.loggedUser &&
                    <div><span>(Level)</span></div>
                }
            </div>
        )
    }
}

type ComponentBadgeProps = {
    component: ComponentType,
    onReload: (componentId: string) => void,
    onReloadScm: (componentId: string) => void,
    onReloadSonar: () => void
}
function ComponentBadge(props: ComponentBadgeProps) {
    const component = props.component;
    if (component && component.id) {
        return (
            <div className='component-badge'>
                <span title={component.id}>
                    <span style={{fontSize: 14}}><b>{component.label}</b></span>
                    <span style={{fontSize: 8, marginLeft: 2}}>({component.type})</span>
                </span>
                <br/>
                <span style={{fontSize: 9}}>{component.mvn}</span>
                <br/>

                <ScmLink type={component.scmType} url={component.scmLocation}/>
                <span style={{fontSize: 9}}>{component.scmLocation ? component.scmLocation : 'not under scm'}</span>
                <br/>

                <span key="owner">Owner {component.owner || <span style={{opacity: "0.5"}}>Unknown</span>}</span><br/>
                <span key="criticality">Criticality {component.critical || <span style={{opacity: "0.5"}}>"?"</span>}</span><br/>

                <LinesOfCode loc={component.loc}/>
                <Coverage coverage={component.coverage}/>
                <SonarLink url={component.sonarUrl} />
                <br/>

                <BuildLink
                    server={component.jenkinsServer}
                    job={component.jenkinsJob}
                    url={component.jenkinsUrl} />
                <BuildNumber number={component.buildNumber}/>
                <BuildStatus status={component.buildStatus}/>
                <DateTime date={component.buildTimestamp}/>
                <br/>

                <button onClick={e => props.onReload(component.id)}>RELOAD</button>
                <button onClick={e => props.onReloadScm(component.id)}>RELOAD SCM</button>
                <button onClick={e => props.onReloadSonar()}>RELOAD SONAR</button>

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



