import React, {Fragment} from "react";
import {groupBy} from "../common/utils";
import {DeploymentPipeline} from "../pipeline/DeploymentPipeline";
import axios from "axios";
import {ApplicationLayout} from "./common/ApplicationLayout";

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
            <ApplicationLayout title="Pipeline"
                   main={() =>
                       <Fragment>
                           {components.map(component =>
                               <DeploymentPipeline key={component.id} component={component} instances={instancesByComponent[component.id]}/>
                           )}
                       </Fragment>
                   }
            />
        );
    }
}

export {PipelineApp};

