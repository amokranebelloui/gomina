import React from "react";
import {Link} from "react-router-dom";
import {Version} from "../common/Version";
import sonarIcon from "./sonar.ico"
import {LinesOfCode} from "./LinesOfCode";
import {Coverage} from "./Coverage";
import {UnreleasedChangeCount} from "./UnreleasedChangeCount";
import {ScmLink} from "./ScmLink";
import {BuildLink} from "./BuildLink";
import {ImageLink} from "./ImageLink";

class ProjectSummary extends React.Component {
    render() {
        const project = this.props.project;
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
                        {project.tags && <span style={{fontSize: 8, marginLeft: 2}}>({project.tags})</span>}
                    </span>
                    <br/>
                    <span style={{fontSize: 8}}>{project.mvn}</span>
                </div>
                <div className='released'><Version version={project.released} /></div>
                <div className='latest'><Version version={project.latest} /></div>
                <div className='loc'><LinesOfCode loc={project.loc} /></div>
                <div className='coverage'><Coverage coverage={project.coverage} /></div>
                <div className='scm'><ScmLink projectId={project.id} url={project.scmUrl} changes={project.changes} /></div>
                <div className='build'><BuildLink server={project.jenkinsServer} url={project.jenkinsUrl} /></div>
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
                <span style={{fontSize: 9}}>{project.scmRepo + ': ' + project.scmUrl}</span>
                <br/>
                {project.owner && [<span>Owner {project.owner}</span>, <br/>]}
                {project.critical && [<span>Criticality {project.critical}</span>, <br/>]}

                <ImageLink src={sonarIcon} url={project.sonarUrl} />
                <LinesOfCode loc={project.loc}/>
                <Coverage coverage={project.coverage}/>
                <br/>
                <BuildLink server={project.jenkinsServer} url={project.jenkinsUrl} />
                <span>{project.jenkinsJob}</span>
                <br/>
                <span>
                    Jenkins &nbsp;
                    {project.buildNumber} &nbsp;
                    {project.buildStatus} &nbsp;
                    {project.buildTimestamp && new Date(project.buildTimestamp).toLocaleString()} &nbsp;
                </span>
                <br/>
                <button onClick={e => props.onReload(project.id)}>RELOAD</button>
                <button onClick={e => props.onReloadScm(project.id)}>RELOAD SCM</button>
                <button onClick={e => props.onReloadSonar()}>RELOAD SONAR</button>
                <hr />
                <Link to={'/project/' + props.project.id}>SVN Log</Link>
                <span>|</span>
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


export {ProjectSummary, ProjectBadge}



