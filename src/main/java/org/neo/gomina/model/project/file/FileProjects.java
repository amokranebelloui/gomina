package org.neo.gomina.model.project.file;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.neo.gomina.model.project.Project;
import org.neo.gomina.model.project.Projects;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileProjects implements Projects {

    private final static Logger logger = LogManager.getLogger(FileProjects.class);

    private ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    public FileProjects() {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public List<Project> getProjects() {
        try {
            return mapper.readValue(new File("data/projects.yaml"), new TypeReference<List<Project>>(){});
        }
        catch (Exception e) {
            logger.error("", e);
            return new ArrayList<>();
        }
    }

    @Override
    public Project getProject(String projectId) {
        try {
            List<Project> data = mapper.readValue(new File("data/projects.yaml"), new TypeReference<List<Project>>() {});
            for (Project datum : data) {
                if (StringUtils.equals(datum.id, projectId)) {
                    return datum;
                }
            }
        }
        catch (Exception e) {
            logger.error("Cannot get project " + projectId, e);
        }
        return null;
    }
}
