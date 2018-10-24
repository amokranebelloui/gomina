import React from "react";
import {groupBy} from "../common/utils";
import {AppLayout} from "./common/layout";
import {ProjectPipeline} from "../project/ProjectPipeline";
import axios from "axios";

class PipelineApp extends React.Component {
    constructor(props) {
        super(props);
        this.state = {components: [], instances: []};
        this.retrieveComponents = this.retrieveComponents.bind(this);
        this.retrieveInstances = this.retrieveInstances.bind(this);

        console.info("pipeApp !constructor");
    }
    retrieveComponents() {
        console.log("pipeApp Retr components ... ");
        const thisComponent = this;
        axios.get('/data/components')
            .then(response => {
                console.log("pipeApp data components", response.data);
                thisComponent.setState({components: response.data});
            })
            .catch(function (error) {
                console.log("pipeApp error components", error);
                thisComponent.setState({components: []});
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
        this.retrieveComponents();
        this.retrieveInstances();
    }

    render() {
        const instancesByComponent = groupBy(this.state.instances, 'componentId');
        console.info('pipeApp render! ', instancesByComponent);
        const components = this.state.components;
        return (
            <AppLayout title="Pipeline">
                {components.map(component =>
                    <ProjectPipeline key={component.id} project={component} instances={instancesByComponent[component.id]}/>
                )}
            </AppLayout>
        );
    }
}

export {PipelineApp};

