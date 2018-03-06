import React from "react";
import {AppLayout} from "./common/layout";
import {ProjectSummary} from "../project/project";
import axios from "axios/index";

class ProjectsApp extends React.Component {

    constructor(props) {
        super(props);
        this.state = {projects: []};
        this.retrieveProjects = this.retrieveProjects.bind(this);
        console.info("projectApp !constructor ");
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

    componentDidMount() {
        console.info("projectsApp !mount ", this.props.match.params.id);
        //this.retrieveProject(this.props.match.params.id);
        this.retrieveProjects();
    }
    render() {
        const projects = this.state.projects;
        return (
            <AppLayout title="Projects">
                <table style={{padding: '5px'}}>
                    {projects.map(project =>
                        <ProjectSummary key={project.id} project={project}/>
                    )}
                </table>
            </AppLayout>
        );
    }
}

export {ProjectsApp};
