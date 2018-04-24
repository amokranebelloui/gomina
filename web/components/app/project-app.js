import React from "react";
import axios from "axios/index";
import {AppLayout, PrimarySecondaryLayout} from "./common/layout";
import {Documentation, ProjectBadge, ProjectSummary, ScmLog} from "../project/project";
import {Container, Well} from "../common/component-library";
import "../project/project.css"

class ProjectApp extends React.Component {
    
    constructor(props) {
        super(props);
        this.state = {
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
        axios.post('/data/scm/project/' + projectId + '/reload')
            .then(response => {
                console.log("project reloaded", response.data);
            })
            .catch(function (error) {
                console.log("project reload error", error.response);
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
    render() {
        //console.info("!render ", this.props.match.params.id);
        console.info("RRR", this.state.search);
        const projects = this.state.projects;
        const project = this.state.project;
        const commits = project.commitLog || [];
        const instances = this.state.instances.filter(instance => instance.project == project.id);
        const title = (<span>Projects &nbsp;&nbsp;&nbsp; <input type="text" name="search" onChange={e => this.setState({search: e.target.value})}/></span>);
        const docId = this.props.match.params.docId;
        return (
            <AppLayout title={title}>

                <PrimarySecondaryLayout>
                    <Container>
                        {this.state.search && <span>Search: {this.state.search}</span>}
                        {!this.state.search && <span>All</span>}
                        <div className='project-list'>
                            <div className='project-row'>
                                <div className='summary'><b>Project</b></div>
                                <div className='released'><b>Released</b></div>
                                <div className='latest'><b>Latest</b></div>
                                <div className='loc'><b>LOC</b></div>
                                <div className='coverage'><b>Coverage</b></div>
                                <div className='scm'><b>SCM</b></div>
                                <div className='build'><b>Build</b></div>
                            </div>
                            {projects
                                .map(project => {console.info(project.label, this.state.search); return project})
                                .filter(project => this.matchesSearch(project))
                                .map(project => <ProjectSummary key={project.id} project={project}/>)
                            }
                        </div>
                    </Container>
                    <Well block>
                        <ProjectBadge project={project} onReloadProject={id => this.reloadProject(id)} />
                    </Well>
                    <Container>
                        {docId
                            ? <Documentation doc={this.state.doc} />
                            : <ScmLog project={project} commits={commits} instances={instances}/>
                        }
                    </Container>
                </PrimarySecondaryLayout>

            </AppLayout>
        );
    }

    matchesSearch(project) {
        return project.label && project.label.match(new RegExp(this.state.search, "i"));
        //return project.label && project.label.indexOf(this.state.search) !== -1;
    }
}

export {ProjectApp};