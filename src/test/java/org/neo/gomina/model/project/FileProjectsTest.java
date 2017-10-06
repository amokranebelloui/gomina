package org.neo.gomina.model.project;

import org.fest.assertions.Assertions;
import org.junit.Test;
import org.neo.gomina.model.project.file.FileProjects;

import java.util.List;

public class FileProjectsTest {

    @Test
    public void getProjects() throws Exception {
        List<Project> projects = new FileProjects().getProjects();
        Assertions.assertThat(projects.size()).isGreaterThan(3);
    }

}