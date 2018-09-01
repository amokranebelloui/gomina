import React from "react";
import {Link} from "react-router-dom";
import {Badge} from "../common/component-library";
import {Version} from "../common/version";
import sonarIcon from "./sonar.ico"

class LinesOfCode extends React.Component {
    render() {
        const base = {padding: '2px', borderRadius: '3px', display: 'block'};
        const high = {width: 110, backgroundColor: 'black', color: 'white'};
        const medium = {width: 80, backgroundColor: 'gray', color: 'white'};
        const low = {width: 50, backgroundColor: 'lightgray', color: 'white'};
        const unknown = {width: 110, backgroundColor: '#FFEEEE', color: 'gray'};

        const loc = this.props.loc;
        const style = loc > 10000 ? high : loc > 5000 ? medium : loc > 0 ? low : unknown;
        return (
            <span style={{display: 'inline-block', width: 114, backgroundColor: '#E8E8E8', borderRadius: '3px'}}>
                    <span style={Object.assign(base, style)}>{loc || 'Unknown'}</span>
                </span>
        );
    }
}

class Coverage extends React.Component {
    render() {
        const base = {padding: '2px', borderRadius: '3px', display: 'block'};
        const high = {width: 110, backgroundColor: 'green', color: 'white'};
        const medium = {width: 80, backgroundColor: 'orange', color: 'white'};
        const low = {width: 50, backgroundColor: 'red', color: 'white'};
        const unknown= {width: 50, backgroundColor: '#lightgray', color: 'gray'};

        const coverage = this.props.coverage;
        const style = coverage > 20 ? high : coverage > 8 ? medium : coverage > 0 ? low : unknown;
        return (
            <span style={{display: 'inline-block', width: 114, backgroundColor: '#E8E8E8', borderRadius: '3px'}}>
                    <span style={{float: 'right', color: '#BBBBBB', fontSize: 10}}>
                        (22/24)
                    </span>
                    <span style={Object.assign(base, style)}>
                        {coverage ? coverage + ' %' : 'Unknown'}
                    </span>
                </span>
        );
    }
}

class ImageLink extends React.Component {
    render() {
        const text = this.props.server ? this.props.server : "build";
        //const text = this.props.src ? this.props.src : "build";
        return (
            <a href={this.props.url} target="_blank" title={this.props.url}
               style={{padding: 2, borderRadius: '3px', fontSize: 10}}>
                <img src={this.props.src} width="16" height="16" />
            </a>
        )
    }
}

class BuildLink extends React.Component {
    render() {
        const text = this.props.server ? this.props.server : "build";
        return (
            <a href={this.props.url} target="_blank" title={this.props.url}
               style={{backgroundColor: '#E8BD30', color: 'white', padding: 2, borderRadius: '3px', fontSize: 10}}>
                {text}
            </a>
        )
    }
}

class ScmLink extends React.Component {
    render() {
        const changesCount = this.props.changes;
        const hasChanges = changesCount > 0;
        const backgroundColor = hasChanges ? '#EAA910' : '#F2E18F';
        return (
            <a href={this.props.url} target="_blank" title={this.props.url}
               style={{backgroundColor: backgroundColor, color: 'white', padding: 2, borderRadius: '3px', fontSize: 10}}>
                svn
            </a>
        )
    }
}

class UnreleasedChangeCount extends React.Component {
    render() {
        const changesCount = this.props.changes;
        const hasChanges = changesCount > 0;
        //const backgroundColor = hasChanges ? '#EAA910' : '#F2E18F';
        return (
            hasChanges && <Badge backgroundColor='#EAA910' color='white'>{changesCount}</Badge>
        )
    }
}

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


class Documentation extends React.Component {
    // FIXME Initial values?
    componentWillReceiveProps(nextProps) { // Only on changes
        this.content.innerHTML = nextProps.doc;
    }
    render() {
        return (
            <div>
                <b>Doc</b>
                <br/>
                <div ref={(ref) => { this.content = ref; }}></div>
            </div>
        )
    }
}


export {BuildLink, ImageLink, ProjectSummary, ProjectBadge, UnreleasedChangeCount, ScmLink, Documentation, LinesOfCode, Coverage}



