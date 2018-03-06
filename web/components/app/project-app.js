import React from "react";
import axios from "axios/index";
import {AppLayout} from "./common/layout";
import {Coverage, LinesOfCode, ProjectSummary, ScmLog} from "../project/project";
import {Well} from "../common/component-library";

class ProjectApp extends React.Component {
    
    constructor(props) {
        super(props);
        this.state = {projects: [], projectId: this.props.match.params.id, project: {}, instances: []};
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
        this.retrieveProject(this.state.projectId);
        this.retrieveInstances(this.state.projectId);
    }
    componentWillReceiveProps(nextProps) {
        const newProject = nextProps.match.params.id;
        console.info("projectApp !props-chg ", this.props.match.params.id, newProject);
        this.setState({projectId: newProject});
        if (this.props.match.params.id != newProject) {
            this.retrieveProject(newProject);
            this.retrieveInstances(newProject);
        }
    }
    render() {
        //console.info("!render ", this.props.match.params.id);
        const projects = this.state.projects;
        const project = this.state.project;
        const commits = project.commitLog || [];
        const instances = this.state.instances.filter(instance => instance.project == project.id);
        const title = "Project '" + project.label + "'";
        const cellStyle = {display: 'inline'};
        return (
            <AppLayout title={title}>
                <div style={{
                    display: 'grid',
                    gridGap: '2px',
                    gridTemplateColumns: 'minmax(350px, 4fr) minmax(200px, 3fr)'
                }}>
                    <div style={cellStyle}>
                        <table>
                        {projects.map(project =>
                            <ProjectSummary key={project.id} project={project}/>
                        )}
                        </table>
                    </div>
                    <Well block padding="10px 10px" margin="2px">
                    <div style={cellStyle}>
                        <div>
                            <span title={project.id}>
                                <b>{project.label}</b>
                                <span style={{fontSize: 10, marginLeft: 2}}>({project.type})</span>
                            </span>
                            <br/>
                            <span style={{fontSize: 11}}>{project.mvn}</span>
                            <br/>
                            <span style={{fontSize: 11}}>{project.repo + ':' + project.svn}</span>
                            <button onClick={e => this.reloadProject(project.id)}>RELOAD</button>
                            <br/>
                            <LinesOfCode loc={project.loc}/>&nbsp;
                            <Coverage coverage={project.coverage}/>&nbsp;
                        </div>
                        <br/>
                        <ScmLog project={project} commits={commits} instances={instances}/>
                    </div>
                    </Well>
                </div>
            </AppLayout>
        );
    }
}

export {ProjectApp};