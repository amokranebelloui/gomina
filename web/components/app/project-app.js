import React from "react";
import axios from "axios/index";
import {AppLayout} from "./common/layout";
import {Coverage, LinesOfCode, ProjectSummary, ScmLog} from "../project/project";

class ProjectApp extends React.Component {
    
    constructor(props) {
        super(props);
        this.state = {project: {}};
        this.retrieveProject = this.retrieveProject.bind(this);
        this.reloadProject = this.reloadProject.bind(this);
        console.info("projectApp !constructor ", this.props.match.params.id);
    }

    componentWillMount() {
        console.info("projectApp !mount ", this.props.match.params.id);
        //this.retrieveProject(this.props.match.params.id);
    }

    componentWillReceiveProps(nextProps) {
        console.info("projectApp !props-chg ", this.props.match.params.id, nextProps.match.params.id);
        this.retrieveProject(nextProps.match.params.id);
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

    render() {
        //console.info("!render ", this.props.match.params.id);
        const projects = this.props.projects;
        const project = this.state.project;
        const commits = project.commitLog || [];
        const instances = this.props.instances.filter(instance => instance.project == project.id);
        const title = "Project '" + project.label + "'";
        const cellStyle = {display: 'inline'};
        return (
            <AppLayout title={title}>
                <div style={{
                    display: 'grid',
                    gridGap: '2px',
                    gridTemplateColumns: 'minmax(500px, 6fr) minmax(200px, 3fr)'
                }}>
                    <div style={cellStyle}>
                        {projects.map(project =>
                            <ProjectSummary key={project.id} project={project}/>
                        )}
                    </div>
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
                </div>
            </AppLayout>
        );
    }
}

export {ProjectApp};