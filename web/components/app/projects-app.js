import React from "react";
import {AppLayout} from "./common/layout";
import {ProjectSummary} from "../project/project";

class ProjectsApp extends React.Component {
    render() {
        const projects = this.props.projects;
        return (
            <AppLayout title="Projects">
                {projects.map(project =>
                    <ProjectSummary key={project.id} project={project}/>
                )}
            </AppLayout>
        );
    }
}

export {ProjectsApp};
