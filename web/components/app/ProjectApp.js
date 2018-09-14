import React from "react";
import axios from "axios/index";
import {LoggedUserContext, AppLayout, PrimarySecondaryLayout} from "./common/layout";
import {ProjectBadge, ProjectSummary} from "../project/Project";
import {Well} from "../common/Well";
import "../project/Project.css"
import {CommitLog} from "../commitlog/CommitLog";
import {Container} from "../common/Container";
import {Documentation} from "../documentation/Documentation";
import {flatMap, uniqCount} from "../common/utils";
import {TagCloud} from "../common/TagCloud";

class ProjectApp extends React.Component {
    
    constructor(props) {
        super(props);
        this.state = {
            sortBy: 'alphabetical',
            projects: [],
            projectId: this.props.match.params.id,
            project: {},
            instances: [],
            docId: this.props.match.params.docId,
            doc: null,
            search: ""};
        this.retrieveProjects = this.retrieveProjects.bind(this);
        this.retrieveProject = this.retrieveProject.bind(this);
        this.retrieveInstances= this.retrieveInstances.bind(this);
        this.retrieveDoc = this.retrieveDoc.bind(this);
        this.reloadProject = this.reloadProject.bind(this);
        console.info("projectApp !constructor ", this.props.match.params.id);
    }

    retrieveProjects() {
        console.log("projectApp Retr Projects ... ");
        const thisComponent = this;
        axios.get('/data/projects')
            .then(response => {
                console.log("projectApp data projects", response.data);
                thisComponent.setState({projects: response.data});
            })
            .catch(function (error) {
                console.log("projectApp error", error);
                thisComponent.setState({projects: []});
            });
    }
    retrieveProject(projectId) {
        const thisComponent = this;
        axios.get('/data/projects/' + projectId)
            .then(response => {
                console.log("project", response.data);
                thisComponent.setState({project: response.data});
            })
            .catch(function (error) {
                console.log("error", error.response);
                thisComponent.setState({project: {}});
            });
    }
    reloadProject(projectId) {
        console.info("reloading", projectId);
        axios.post('/data/projects/' + projectId + '/reload-scm')
            .then(response => {
                console.log("project reloaded", response.data);
            })
            .catch(function (error) {
                console.log("project reload error", error.response);
            });
    }
    reloadSonar(projectId) {
        axios.post('/data/projects/reload-sonar')
            .then(response => {
                console.log("sonar reloaded", response.data);
            })
            .catch(function (error) {
                console.log("sonar reload error", error.response);
            });
    }
    retrieveDoc(projectId, docId) {
        const thisComponent = this;
        axios.get('/data/projects/' + projectId + '/doc/' + docId)
            .then(response => {
                console.log("doc data");
                thisComponent.setState({doc: response.data});
            })
            .catch(function (error) {
                console.log("doc error", error.response);
                thisComponent.setState({doc: null});
            });
    }
    retrieveInstances(projectId) {
        console.log("projectApp Retr Instances... " + projectId);
        const thisComponent = this;
        axios.get('/data/instances') // FIXME Retrieve only instances corresponding to project
            .then(response => {
                console.log("projectApp data instances", response.data);
                thisComponent.setState({instances: response.data});
            })
            .catch(function (error) {
                console.log("projectApp error", error);
                thisComponent.setState({instances: []});
            });
    }

    componentDidMount() {
        console.info("projectApp !mount ", this.props.match.params.id);
        this.retrieveProjects();
        if (this.state.projectId) {
            this.retrieveProject(this.state.projectId);
            this.retrieveInstances(this.state.projectId);
            if (this.state.docId) {
                this.retrieveDoc(this.state.projectId, this.state.docId);
            }
        }
    }
    componentWillReceiveProps(nextProps) {
        const newProject = nextProps.match.params.id;
        const newDoc = nextProps.match.params.docId;
        console.info("projectApp !props-chg ", this.props.match.params.id, newProject);
        this.setState({projectId: newProject});
        if (this.props.match.params.id != newProject && newProject) {
            this.retrieveProject(newProject);
            this.retrieveInstances(newProject);
        }
        if (newProject && newDoc &&
            (this.props.match.params.id != newProject || this.props.match.params.docId != newDoc)) {
            this.retrieveDoc(newProject, newDoc);
        }
    }
    changeSelected(sortBy) {
        this.setState({sortBy: sortBy});
    }
    render() {
        //console.info("!render ", this.props.match.params.id);
        console.info("RRR", this.state.search);
        let projects;
        switch (this.state.sortBy) {
            case 'alphabetical' : projects = this.state.projects.sort((a, b) => a.label > b.label ? 1 : -1); break;
            case 'loc' : projects = this.state.projects.sort((a, b) => (b.loc - a.loc) * 10 + (a.label > b.label ? 1 : -1)); break;
            case 'coverage' : projects = this.state.projects.sort((a, b) => (b.coverage - a.coverage) * 10 + (a.label > b.label ? 1 : -1)); break;
            case 'last-commit' : projects = this.state.projects.sort((a, b) => (b.lastCommit - a.lastCommit) * 10 + (a.label > b.label ? 1 : -1)); break;
            case 'commit-activity' : projects = this.state.projects.sort((a, b) => (b.commitActivity - a.commitActivity) * 10 + (a.label > b.label ? 1 : -1)); break;
            case 'unreleased-changes' : projects = this.state.projects.sort((a, b) => (b.changes - a.changes) * 10 + (a.label > b.label ? 1 : -1)); break;
            default : projects = this.state.projects
        }
        const languages = uniqCount(flatMap(this.state.projects, p => p.languages))
            .sort((i1, i2) => i2.count - i1.count);
        const tags = uniqCount(flatMap(this.state.projects, p => p.tags))
            .sort((i1, i2) => i2.count - i1.count);
        const project = this.state.project;
        const commits = project.commitLog || [];
        const instances = this.state.instances.filter(instance => instance.project == project.id);
        const title = (<span>Projects &nbsp;&nbsp;&nbsp; <input type="text" name="search" onChange={e => this.setState({search: e.target.value})}/></span>);
        const docId = this.props.match.params.docId;
        return (
            <AppLayout title={title}>
            <LoggedUserContext.Consumer>
            {loggedUser => (
                <PrimarySecondaryLayout>
                    <Container>
                        {this.state.search && <span>Search: {this.state.search}</span>}
                        {!this.state.search && <span>All</span>}
                        &nbsp;
                        Sorted by {this.state.sortBy}
                        <br/>
                        Languages: <TagCloud tags={languages} /><br/>
                        Tags: <TagCloud tags={tags} /><br/>
                        <button disabled={this.state.sortBy === 'alphabetical'} onClick={e => this.changeSelected('alphabetical')}>Alphabetical</button>
                        <button disabled={this.state.sortBy === 'unreleased-changes'} onClick={e => this.changeSelected('unreleased-changes')}>Unreleased Changes</button>
                        <button disabled={this.state.sortBy === 'loc'} onClick={e => this.changeSelected('loc')}>LOC</button>
                        <button disabled={this.state.sortBy === 'coverage'} onClick={e => this.changeSelected('coverage')}>Coverage</button>
                        <button disabled={this.state.sortBy === 'last-commit'} onClick={e => this.changeSelected('last-commit')}>Last Commit</button>
                        <button disabled={this.state.sortBy === 'commit-activity'} onClick={e => this.changeSelected('commit-activity')}>Commit Activity</button>
                        <div className='project-list'>
                            <div className='project-row'>
                                <div className='summary'><b>Project</b></div>
                                <div className='released'><b>Released</b></div>
                                <div className='latest'><b>Latest</b></div>
                                <div className='loc'><b>LOC</b></div>
                                <div className='coverage'><b>Coverage</b></div>
                                <div className='scm'><b>SCM</b></div>
                                <div className='last-commit'><b>LastCommit/Activity</b></div>
                                <div className='build'><b>Build</b></div>
                            </div>
                            {projects
                                .map(project => {console.info(project.label, this.state.search); return project})
                                .filter(project => this.matchesSearch(project))
                                .map(project => <ProjectSummary key={project.id} project={project} loggedUser={loggedUser} />)
                            }
                        </div>
                    </Container>
                    <Well block>
                        <ProjectBadge project={project}
                                      onReload={id => this.retrieveProject(id)}
                                      onReloadScm={id => this.reloadProject(id)}
                                      onReloadSonar={() => this.reloadSonar()} />
                    </Well>
                    <Container>
                        {docId
                            ? [<b>Doc</b>,<Documentation doc={this.state.doc} />]
                            : [<b>Commit Log</b>,<CommitLog commits={commits} instances={instances} />]
                        }
                    </Container>
                </PrimarySecondaryLayout>
            )}
            </LoggedUserContext.Consumer>
            </AppLayout>
        );
    }

    matchesSearch(project) {
        let label = (project.label || "");
        let languages = (project.languages || []);
        let tags = (project.tags || []);
        let regExp = new RegExp(this.state.search, "i");
        return label.match(regExp) ||
            (languages.filter(item => item.match(regExp))||[]).length > 0 ||
            (tags.filter(item => item.match(regExp))||[]).length > 0;
        //return project.label && project.label.indexOf(this.state.search) !== -1;
    }
}

export {ProjectApp};