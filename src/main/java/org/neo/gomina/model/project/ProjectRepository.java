package org.neo.gomina.model.project;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ProjectRepository {

    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    public ProjectRepository() {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static void main(String[] args) {
        List<Project> projects = new ProjectRepository().getProjects();
        System.out.println(projects);
    }

    public List<Project> getProjects() {
        try {
            return mapper.readValue(new File("data/projects.yaml"), new TypeReference<List<Project>>(){});
        }
        catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
