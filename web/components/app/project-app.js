import React from "react";
import axios from "axios/index";
import {AppLayout, PrimarySecondaryLayout} from "./common/layout";
import {Coverage, LinesOfCode, ProjectSummary, ScmLog} from "../project/project";
import {Container, Well} from "../common/component-library";
import "../project/project.css"

class ProjectApp extends React.Component {
    
    constructor(props) {
        super(props);
        this.state = {projects: [], projectId: this.props.match.params.id, project: {}, instances: [], search: ""};
        this.retrieveProjects = this.retrieveProjects.bind(this);
        this.retrieveProject = this.retrieveProject.bind(this);
        this.retrieveInstances= this.retrieveInstances.bind(this);
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
        axios.post('/data/projects/' + projectId + '/reload')
            .then(response => {
                console.log("project reloaded", response.data);
            })
            .catch(function (error) {
                console.log("project reload error", error.response);
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
        }
    }
    componentWillReceiveProps(nextProps) {
        const newProject = nextProps.match.params.id;
        console.info("projectApp !props-chg ", this.props.match.params.id, newProject);
        this.setState({projectId: newProject});
        if (this.props.match.params.id != newProject && newProject) {
            this.retrieveProject(newProject);
            this.retrieveInstances(newProject);
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
        const cellStyle = {display: 'inline'};
        return (
            <AppLayout title={title}>

                <PrimarySecondaryLayout>
                    <Container>
                        {this.state.search && <span>Search: {this.state.search}</span>}
                        {!this.state.search && <span>All</span>}
                        <div className='project-list'>
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
                        <ScmLog project={project} commits={commits} instances={instances}/>
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

function ProjectBadge(props) {
    const project = props.project
    if (project && project.id) {
        return (
            <div>
                <span title={project.id}>
                    <b>{project.label}</b>
                    <span style={{fontSize: 10, marginLeft: 2}}>({project.type})</span>
                </span>
                <br/>
                <span style={{fontSize: 11}}>{project.mvn}</span>
                <br/>
                <span style={{fontSize: 11}}>{project.repo + ':' + project.svn}</span>
                <button onClick={e => this.props.onReloadProject(project.id)}>RELOAD</button>
                <br/>
                <LinesOfCode loc={project.loc}/>&nbsp;
                <Coverage coverage={project.coverage}/>&nbsp;
            </div>
        )
    }
    else {
        return (<div>Select a project to see details</div>)
    }
}

export {ProjectApp};