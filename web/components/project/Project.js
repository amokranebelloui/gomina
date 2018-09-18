import React from "react";
import {Link} from "react-router-dom";
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
import {Revision} from "../commitlog/Revision";

function ProjectHeader(props) {
    return (
        <div className='project-row'>
            <div className='summary'><b>Project</b></div>
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

class ProjectSummary extends React.Component {
    render() {
        const project = this.props.project;
        const systems = project.systems || [];
        const languages = project.languages || [];
        const tags = project.tags || [];
        return (
            <div className='project-row'>
                <div className='summary'>
                    <span title={project.id}>
                        <Link to={"/project/" + project.id}>
                            <span style={{fontSize: 14}}>{project.label}</span>
                        </Link>
                        &nbsp;
                        <UnreleasedChangeCount changes={project.changes} />
                        <span style={{fontSize: 8, marginLeft: 2}}>({project.type})</span>
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
                    <span style={{fontSize: 8}}>{project.mvn}</span>
                </div>
                <div className='released'><Version version={project.released} /></div>
                <div className='latest'><Version version={project.latest} /></div>
                <div className='loc'><LinesOfCode loc={project.loc} /></div>
                <div className='coverage'><Coverage coverage={project.coverage} /></div>
                <div className='scm'><ScmLink projectId={project.id} type={project.scmType} url={project.scmUrl} changes={project.changes} /></div>
                <div className='last-commit'>
                    <DateTime date={project.lastCommit} />&nbsp;
                    {(project.commitActivity != undefined && project.commitActivity != 0) &&
                        <Badge backgroundColor='#EAA910' color='white'>{project.commitActivity}</Badge>
                    }
                </div>
                <div className='build'>
                    <BuildLink server={project.jenkinsServer} url={project.jenkinsUrl} />
                </div>
                {this.props.loggedUser &&
                    <div>(Level)</div>
                }
            </div>
        )
    }
}

function ProjectBadge(props) {
    const project = props.project;
    if (project && project.id) {
        return (
            <div className='project-badge'>
                <span title={project.id}>
                    <span style={{fontSize: 14}}><b>{project.label}</b></span>
                    <span style={{fontSize: 8, marginLeft: 2}}>({project.type})</span>
                </span>
                <br/>
                <span style={{fontSize: 9}}>{project.mvn}</span>
                <br/>

                <ScmLink type={project.scmType} url={project.scmUrl}/>
                <span style={{fontSize: 9}}>{project.scmRepo ? project.scmRepo : 'not under scm'}</span>
                <br/>

                {project.owner && [<span>Owner {project.owner}</span>, <br/>]}
                {project.critical && [<span>Criticality {project.critical}</span>, <br/>]}

                <LinesOfCode loc={project.loc}/>
                <Coverage coverage={project.coverage}/>
                <SonarLink url={project.sonarUrl} />
                <br/>

                <BuildLink
                    server={project.jenkinsServer}
                    job={project.jenkinsJob}
                    url={project.jenkinsUrl} />
                <BuildNumber number={project.buildNumber}/>
                <BuildStatus status={project.buildStatus}/>
                <DateTime date={project.buildTimestamp}/>
                <br/>

                <button onClick={e => props.onReload(project.id)}>RELOAD</button>
                <button onClick={e => props.onReloadScm(project.id)}>RELOAD SCM</button>
                <button onClick={e => props.onReloadSonar()}>RELOAD SONAR</button>
                
                <hr />
                
                <Link to={'/project/' + props.project.id}>SVN Log</Link>
                <span>|</span>
                {project.branches
                    .map(branch => [
                        <Link key={branch.name} to={'/project/' + props.project.id + '/' + branch.name}>
                            {branch.name}
                        </Link>,
                        <span>{branch.origin}</span>,
                        <Revision type={project.scmType} revision={branch.originRevision}/>
                    ])
                }
                <br/>
                {project.docFiles
                    .map(doc => <Link key={doc} to={'/project/' + props.project.id + '/doc/' + doc}>{doc}</Link>)
                }
            </div>
        )
    }
    else {
        return (<div>Select a project to see details</div>)
    }
}


export {ProjectHeader, ProjectSummary, ProjectBadge}



