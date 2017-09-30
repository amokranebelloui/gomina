package org.neo.gomina.model.project;

import org.fest.assertions.Assertions;
import org.junit.Test;

import java.util.List;

public class ProjectRepositoryTest {

    @Test
    public void getProjects() throws Exception {
        List<Project> projects = new ProjectRepository().getProjects();
        Assertions.assertThat(projects.size()).isGreaterThan(3);
    }

}