import React from "react";
import axios from "axios/index";
import {AppLayout, LoggedUserContext, PrimarySecondaryLayout} from "./common/layout";
import {ProjectBadge, ProjectHeader, ProjectSummary} from "../project/Project";
import {Well} from "../common/Well";
import "../project/Project.css"
import {CommitLog} from "../commitlog/CommitLog";
import {Container} from "../common/Container";
import {Documentation} from "../documentation/Documentation";
import {flatMap} from "../common/utils";
import {TagCloud} from "../common/TagCloud";
import PropTypes from "prop-types"
import queryString from 'query-string'
import {Dependencies} from "../dependency/Dependencies";

class ProjectApp extends React.Component {
    
    constructor(props) {
        super(props);
        const queryParams = queryString.parse(this.props.location.search);
        this.state = {
            sortBy: 'alphabetical',
            projects: [],
            projectId: this.props.match.params.id,
            project: {},
            instances: [],
            dependencies: [],
            impacted: [],
            branchId: queryParams.branchId,
            branch: null,
            docId: this.props.match.params.docId,
            doc: null,
            search: "",
            selectedSystems: [],
            selectedLanguages: [],
            selectedTags: []
            };
        this.retrieveProjects = this.retrieveProjects.bind(this);
        this.retrieveProject = this.retrieveProject.bind(this);
        this.retrieveInstances= this.retrieveInstances.bind(this);
        this.retrieveBranch = this.retrieveBranch.bind(this);
        this.retrieveDoc = this.retrieveDoc.bind(this);
        this.reloadProject = this.reloadProject.bind(this);
        console.info("projectApp !constructor ", this.props.match.params);
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
    retrieveBranch(projectId, branchId) {
        const thisComponent = this;
        axios.get('/data/projects/' + projectId + '/scm?branchId=' + branchId)
            .then(response => {
                console.log("branch data", response.data);
                thisComponent.setState({branch: response.data});
            })
            .catch(function (error) {
                console.log("branch error", error.response);
                thisComponent.setState({branch: null});
            });
    }
    retrieveDependencies(projectId) {
        const thisComponent = this;
        axios.get('/data/dependencies/outgoing/' + projectId)
            .then(response => {
                console.log("deps data");
                thisComponent.setState({dependencies: response.data});
            })
            .catch(function (error) {
                console.log("deps error", error.response);
                thisComponent.setState({dependencies: null});
            });
    }
    retrieveImpacted(projectId) {
        const thisComponent = this;
        axios.get('/data/dependencies/incoming/' + projectId)
            .then(response => {
                console.log("impacted data");
                thisComponent.setState({impacted: response.data});
            })
            .catch(function (error) {
                console.log("impacted error", error.response);
                thisComponent.setState({impacted: null});
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
            this.retrieveDependencies(this.state.projectId);
            this.retrieveImpacted(this.state.projectId);
            if (this.state.docId) {
                this.retrieveDoc(this.state.projectId, this.state.docId);
            }
            if (this.state.branchId) {
                this.retrieveBranch(this.state.projectId, this.state.branchId);
            }
        }
    }
    componentWillReceiveProps(nextProps) {
        const queryParams = queryString.parse(this.props.location.search);
        const nextQueryParams = queryString.parse(nextProps.location.search);
        const newProject = nextProps.match.params.id;
        const newDoc = nextProps.match.params.docId;
        const newBranch = nextQueryParams.branchId;
        console.info("projectApp !props-chg ", this.props.match.params.id, newProject);
        console.info("project branch", queryParams.branchId, newBranch);
        this.setState({projectId: newProject});
        if (this.props.match.params.id !== newProject && newProject) {
            this.retrieveProject(newProject);
            this.retrieveInstances(newProject);
            this.retrieveDependencies(newProject);
            this.retrieveImpacted(newProject);
        }
        if (newProject && newDoc &&
            (this.props.match.params.id !== newProject || this.props.match.params.docId !== newDoc)) {
            this.retrieveDoc(newProject, newDoc);
        }
        if (newProject && newBranch &&
            (this.props.match.params.id !== newProject || queryParams.branchId !== newBranch)) {
            this.retrieveBranch(newProject, newBranch);
        }
    }
    sortChanged(sortBy) {
        this.setState({sortBy: sortBy});
    }
    render() {
        const queryParams = queryString.parse(this.props.location.search);
        const projects = sortProjectsBy(this.state.sortBy, this.state.projects);
        const systems = flatMap(this.state.projects, p => p.systems);
        const languages = flatMap(this.state.projects, p => p.languages);
        const tags = flatMap(this.state.projects, p => p.tags);
        const project = this.state.project;
        const commits = project.commitLog || [];
        const instances = this.state.instances.filter(instance => instance.project == project.id);
        const title = (<span>Projects &nbsp;&nbsp;&nbsp; <input type="text" name="search" onChange={e => this.setState({search: e.target.value})}/></span>);
        const docId = this.props.match.params.docId;
        const branchId = queryParams.branchId;
        console.info("---", docId, branchId);
        /* {this.state.search && <span>Search: {this.state.search}</span>}
        {!this.state.search && <span>All</span>} */
        return (
            <AppLayout title={title}>
            <LoggedUserContext.Consumer>
            {loggedUser => (
                <PrimarySecondaryLayout>
                    <Container>
                        <ProjectSort sortBy={this.state.sortBy} onSortChanged={sortBy => this.sortChanged(sortBy)} />
                        <hr/>
                        <div className='project-list'>
                            <ProjectHeader />
                            {projects
                                .filter(project => this.matchesSearch(project))
                                .map(project => <ProjectSummary key={project.id} project={project} loggedUser={loggedUser} />)
                            }
                        </div>
                    </Container>
                    <div>
                        <Well block>
                            Systems:
                            <TagCloud tags={systems}
                                      selectionChanged={values => this.setState({selectedSystems: values})} />
                            <br/>
                            Languages:
                            <TagCloud tags={languages}
                                      selectionChanged={values => this.setState({selectedLanguages: values})} />
                            <br/>
                            Tags:
                            <TagCloud tags={tags}
                                      selectionChanged={values => this.setState({selectedTags: values})} />
                            <br/>
                        </Well>
                        <ProjectBadge project={project}
                                      onReload={id => this.retrieveProject(id)}
                                      onReloadScm={id => this.reloadProject(id)}
                                      onReloadSonar={() => this.reloadSonar()} />
                    </div>
                    <Container>
                        {   branchId ? [<b>Branch</b>,<CommitLog type={project.scmType} commits={this.state.branch}  />] :
                            docId ? [<b>Doc</b>,<Documentation doc={this.state.doc} />] :
                            this.props.location.pathname.endsWith("dependencies") ? [<b>Deps</b>,<Dependencies dependencies={this.state.dependencies} />] :
                            this.props.location.pathname.endsWith("impacted") ? [<b>Impacted</b>,<Dependencies dependencies={this.state.impacted} />] :
                            [<b>Commit Log</b>,<CommitLog type={project.scmType} commits={commits} instances={instances} />]

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
        let regExp = new RegExp(this.state.search, "i");
        let matchesLabel = label.match(regExp);
        let matchesSystems = this.matchesList(project.systems, this.state.selectedSystems);
        let matchesLanguages = this.matchesList(project.languages, this.state.selectedLanguages);
        let matchesTags = this.matchesList(project.tags, this.state.selectedTags);
        //console.info("MATCH", project.id, matchesLabel, matchesSystems, matchesLanguages, matchesTags);
        return matchesLabel && matchesSystems && matchesLanguages && matchesTags
            //(languages.filter(item => item.match(regExp))||[]).length > 0 ||
            //(tags.filter(item => item.match(regExp))||[]).length > 0;
        //return project.label && project.label.indexOf(this.state.search) !== -1;
    }
    matchesList(projectValues, selectedValues) {
        if (selectedValues && selectedValues.length > 0) {
            const values = (projectValues||[]);
            return (selectedValues||[]).find(value => values.indexOf(value) !== -1);
        }
        return true
    }
}

class ProjectSort extends React.Component {
    constructor(props) {
        super(props)
    }
    changeSelected(sortBy) {
        this.props.onSortChanged && this.props.onSortChanged(sortBy)
    }
    render() {
        return (
            <div>
                Sort:
                <button disabled={this.props.sortBy === 'alphabetical'} onClick={e => this.changeSelected('alphabetical')}>Alphabetical</button>
                <button disabled={this.props.sortBy === 'unreleased-changes'} onClick={e => this.changeSelected('unreleased-changes')}>Unreleased Changes</button>
                <button disabled={this.props.sortBy === 'loc'} onClick={e => this.changeSelected('loc')}>LOC</button>
                <button disabled={this.props.sortBy === 'coverage'} onClick={e => this.changeSelected('coverage')}>Coverage</button>
                <button disabled={this.props.sortBy === 'last-commit'} onClick={e => this.changeSelected('last-commit')}>Last Commit</button>
                <button disabled={this.props.sortBy === 'commit-activity'} onClick={e => this.changeSelected('commit-activity')}>Commit Activity</button>
            </div>
        )
    }
}

ProjectSort.propTypes = {
    sortBy: PropTypes.string,
    onSortChanged: PropTypes.func
};

function sortProjectsBy(sortBy, projects) {
    let result;
    switch (sortBy) {
        case 'alphabetical' :
            result = projects.sort((a, b) => a.label > b.label ? 1 : -1);
            break;
        case 'loc' :
            result = projects.sort((a, b) => (b.loc - a.loc) * 10 + (a.label > b.label ? 1 : -1));
            break;
        case 'coverage' :
            result = projects.sort((a, b) => (b.coverage - a.coverage) * 10 + (a.label > b.label ? 1 : -1));
            break;
        case 'last-commit' :
            result = projects.sort((a, b) => (b.lastCommit - a.lastCommit) * 10 + (a.label > b.label ? 1 : -1));
            break;
        case 'commit-activity' :
            result = projects.sort((a, b) => (b.commitActivity - a.commitActivity) * 10 + (a.label > b.label ? 1 : -1));
            break;
        case 'unreleased-changes' :
            result = projects.sort((a, b) => (b.changes - a.changes) * 10 + (a.label > b.label ? 1 : -1));
            break;
        default :
            result = projects
    }
    return result;
}

export {ProjectApp};