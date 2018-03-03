import React from "react";
import {groupBy} from "../common/component-library";
import {AppLayout} from "./common/layout";
import {ProjectPipeline} from "../project/pipeline";

class PipelineApp extends React.Component {
    render() {
        const instancesByProject = groupBy(this.props.instances, 'project');
        console.info('instances by project', instancesByProject);
        const projects = this.props.projects;
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

