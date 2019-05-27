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
import "../common/items.css"
import type {ComponentType} from "./ComponentType";
import {Secure} from "../permission/Secure";
import {StarRating} from "../common/StarRating";
import {Branch, isTrunk, sortBranchesDetails} from "../commitlog/Branch";
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
                            <span key={system} style={{fontSize: 8}}>{system}</span>
                        )}
                        |
                        {languages.map(language =>
                            <span key={language} style={{fontSize: 8}}>{language}</span>
                        )}
                        |
                        {tags.map(tag =>
                            <span key={tag} style={{fontSize: 8}}>{tag}</span>
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


type ComponentMenuProps = {
    selectedBranch: string,
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
    const isHome = pathname === '/component/'+props.component.id;
    const isScm = pathname.match(RegExp('/component/.*/scm'));
    const isBranches = pathname.match(RegExp('/component/.*/branches'));
    const isApi = pathname.match(RegExp('/component/.*/api'));
    const isDeps = pathname.match(RegExp('/component/.*/dependencies'));
    const isLibs = pathname.match(RegExp('/component/.*/libraries'));
    const isEvents = pathname.match(RegExp('/component/.*/events'));
    const isVersions = pathname.match(RegExp('/component/.*/versions'));
    const showBranches = (isScm || isLibs);
    const branchSuffix = props.selectedBranch ? '?branchId=' + props.selectedBranch : '';
    return (
        component &&
            <div style={{marginTop: '4px'}}>
                <div className="items" style={{display: 'inline'}}>
                    <Link to={'/component/' + props.component.id + branchSuffix}>
                        <Badge backgroundColor={isHome ? 'lightgray' : null}>HOME</Badge>
                    </Link>
                    <Link to={'/component/' + props.component.id + '/scm' + branchSuffix}>
                        <Badge backgroundColor={isScm ? 'lightgray' : null}>COMMITS</Badge>
                    </Link>
                    <Link to={'/component/' + props.component.id + '/branches' + branchSuffix}>
                        <Badge backgroundColor={isBranches ? 'lightgray' : null}>BRANCHES</Badge>
                    </Link>
                    <Link to={'/component/' + props.component.id + '/libraries' + branchSuffix}>
                        <Badge backgroundColor={isLibs ? 'lightgray' : null}>LIBS</Badge>
                    </Link>
                    <Link to={'/component/' + props.component.id + '/versions' + branchSuffix}>
                        <Badge backgroundColor={isVersions ? 'lightgray' : null}>VERSIONS</Badge>
                    </Link>
                    <Link to={'/component/' + props.component.id + '/api' + branchSuffix}>
                        <Badge backgroundColor={isApi ? 'lightgray' : null}>API</Badge>
                    </Link>
                    <Link to={'/component/' + props.component.id + '/dependencies' + branchSuffix}>
                        <Badge backgroundColor={isDeps ? 'lightgray' : null}>DEPS</Badge>
                    </Link>
                    <Link to={'/component/' + props.component.id + '/events' + branchSuffix}>
                        <Badge backgroundColor={isEvents ? 'lightgray' : null}>EVENTS</Badge>
                    </Link>

                    |
                    
                    {component.docFiles && component.docFiles.map(doc =>
                        <Link key={doc} to={'/component/' + props.component.id + '/doc/' + doc + branchSuffix}>
                            <Badge backgroundColor={pathname.match(RegExp('/component/.*/doc/'+doc)) ? 'lightgray' : null}>{doc}</Badge>
                        </Link>
                    )}
                    
                </div>
                <div className="items" style={{display: 'inline', float: 'right'}}>
                    {component.branches && sortBranchesDetails(component.branches).map(branch =>
                        <span key={branch.name} title={"from: " + (branch.origin || "") + " rev:" + (branch.originRevision || "")}>
                            {branch.dismissed !== true && (
                                showBranches ?
                                    <Link to={pathname + '?branchId=' + branch.name}>
                                        <Branch branch={branch}
                                                selected={sameBranch(params.branchId, branch.name) || !showBranches && isTrunk(branch.name)}/>
                                    </Link>
                                : isTrunk(branch.name) ?
                                    <Link to={pathname + '?branchId=' + branch.name}>
                                        <Branch branch={branch} selected={true}/>
                                    </Link>
                                :
                                    <span style={{opacity: '.2'}}>
                                        <Branch branch={branch} selected={sameBranch(params.branchId, branch.name)}/>
                                    </span>

                            )}
                        </span>
                    )}
                </div>
                <div style={{clear: 'both'}}></div>
            </div>
        || null
    )
}

function sameBranch(b1: ?string, b2: ?string) {
    return b1 === b2 || !b1 && isTrunk(b2) || !b2 && isTrunk(b1)
}

export {ComponentHeader, ComponentSummary, ComponentMenu}



