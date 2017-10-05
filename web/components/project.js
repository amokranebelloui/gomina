import React from "react";
import {Link} from "react-router-dom";
import {groupBy, Well, Version, Badge} from "./gomina";

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

class BuildLink extends React.Component {
    render() {
        return (
            <a href={this.props.url} target="_blank" title={this.props.url}
               style={{backgroundColor: '#E8BD30', color: 'white', padding: 2, borderRadius: '3px', fontSize: 10}}>
                build
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
            <span title={this.props.url}
                  style={{backgroundColor: backgroundColor, color: 'white', padding: 2, borderRadius: '3px', fontSize: 10}}>
                <Link to={"/project/" + this.props.projectId}>
                    {hasChanges ? 'svn (' + changesCount + ' chg)' : 'svn'}
                </Link>
            </span>
        )
    }
}

class ProjectSummary extends React.Component {
    render() {
        const project = this.props.project;
        return (
            <Well block margin="0 0 2px 0">
                <div style={{display: 'block', verticalAlign: 'top', overflow:'auto'}}>
                    <div style={{display: 'block', float: 'left', marginRight: 2}}>
                            <span title={project.id}>
                                <b>{project.label}</b>
                                <span style={{fontSize: 10, marginLeft: 2}}>({project.type})</span>
                            </span>
                        <br/>
                        <span style={{fontSize: 11}}>{project.mvn}</span>
                    </div>
                    <div style={{display: 'block', float: 'right', verticalAlign: 'top'}}>
                        <LinesOfCode loc={project.loc} />&nbsp;
                        <Coverage coverage={project.coverage} />&nbsp;
                    </div>
                    <div style={{display: 'block'}}>
                        <BuildLink url={project.jenkins} /> (BuildSt)&nbsp;
                        <ScmLink projectId={project.id} url={project.repo + ':' + project.svn} changes={project.changes} />&nbsp;
                        <Version version={project.released} />&nbsp;
                        <Version version={project.latest} />
                    </div>

                </div>
            </Well>
        )
    }
}


class ScmLog extends React.Component {
    render() {
        //const project = this.props.project;
        const sortedCommits = this.props.commits;
        const instances = this.props.instances;
        const instancesByRevision = groupBy(instances, 'revision');
        const log = {};
        sortedCommits.map(commit => {
            log[commit.revision] = {message: commit.message, instances: []};
        });
        Object.keys(instancesByRevision).map(revision => {
            log[revision] = log[revision] || {};
            log[revision].instances = instancesByRevision[revision] || [];
            log[revision].version = instancesByRevision[revision][0].version;
        });
        return (
            <div>
                <b>SVN Log</b>
                <br/>
                {Object.keys(log).sort((a,b) => b-a).map(revision =>
                    <Well block key={revision}>
                        <div style={{overflow: 'auto'}}>
                                    <span style={{float: 'left', marginRight: '2px'}}>
                                        {revision || '-'}
                                    </span>
                            <span style={{display: 'block', marginLeft: '60px', minHeight: '20px', paddingLeft: '1px', paddingRight: '1px'}}>
                                        {log[revision].message || '-'}

                                <span style={{float: 'right', position: 'top'}}>
                                            {log[revision].version &&
                                            <span style={{paddingLeft: '2px', paddingRight: '2px'}}>
                                                    <Version version={log[revision].version} />
                                                </span>
                                            }
                                    {log[revision].instances.map(instance =>
                                        <span key={instance.id} style={{paddingLeft: '2px', paddingRight: '2px'}}>
                                                    <Badge backgroundColor="#EEEEAA">
                                                        {instance.env} {instance.name}&nbsp;
                                                    </Badge>
                                                </span>
                                    )}

                                        </span>
                                    </span>

                        </div>
                    </Well>
                )}
            </div>
        )
    }
}

export {BuildLink, ScmLog, ProjectSummary, ScmLink, LinesOfCode, Coverage}



