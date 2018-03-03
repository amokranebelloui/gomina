import React from "react";
import {groupBy} from "../common/component-library";
import {AppLayout} from "./common/layout";
import {ProjectPipeline} from "../project/pipeline";
import axios from "axios";

class PipelineApp extends React.Component {
    constructor(props) {
        super(props);
        this.state = {projects: [], instances: []};
        this.retrieveProjects = this.retrieveProjects.bind(this);
        this.retrieveInstances = this.retrieveInstances.bind(this);

        console.info("pipeApp !constructor");
    }
    retrieveProjects() {
        console.log("pipeApp Retr Projects ... ");
        const thisComponent = this;
        axios.get('/data/projects')
            .then(response => {
                console.log("pipeApp data projects", response.data);
                thisComponent.setState({projects: response.data});
            })
            .catch(function (error) {
                console.log("pipeApp error", error);
                thisComponent.setState({projects: []});
            });
    }
    retrieveInstances() {
        console.log("pipeApp Retr Instances ... ");
        const thisComponent = this;
        axios.get('/data/instances')
            .then(response => {
                console.log("pipeApp data instances", response.data);
                thisComponent.setState({instances: response.data});
            })
            .catch(function (error) {
                console.log("pipeApp error", error);
                thisComponent.setState({instances: []});
            });
    }

    componentWillReceiveProps(nextProps) {
        console.info("pipeApp !props-chg ", this.props.match.params.id, nextProps.match.params.id);
    }
    componentDidMount() {
        console.info("pipeApp !mount ");
        this.retrieveProjects();
        this.retrieveInstances();
    }

    render() {
        const instancesByProject = groupBy(this.state.instances, 'project');
        console.info('pipeApp render! ', instancesByProject);
        const projects = this.state.projects;
        return (
            <AppLayout title="Pipeline">
                {projects.map(project =>
                    <ProjectPipeline key={project.id} project={project} instances={instancesByProject[project.id]}/>
                )}
            </AppLayout>
        );
    }
}

export {PipelineApp};

