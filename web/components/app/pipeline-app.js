import React from "react";
import {groupBy} from "../gomina";
import {AppLayout} from "../layout";
import {ProjectPipeline} from "../pipeline";

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

