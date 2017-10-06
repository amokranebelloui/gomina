package org.neo.gomina.model.project;

import java.util.List;

public interface Projects {
    List<Project> getProjects();

    Project getProject(String projectId);
}
