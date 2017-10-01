package org.neo.gomina.api.projects;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.neo.gomina.model.project.Project;
import org.neo.gomina.model.project.ProjectRepository;
import org.neo.gomina.model.svn.SvnDetails;
import org.neo.gomina.model.svn.SvnRepository;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ProjectDetailRepository {

    private final static Logger logger = LogManager.getLogger(ProjectDetailRepository.class);

    @Inject private ProjectRepository projectRepository;
    @Inject private SvnRepository svnRepository;

    public List<ProjectDetail> getProjects() {
        List<ProjectDetail> result = new ArrayList<>();
        for (Project project : projectRepository.getProjects()) {
            SvnDetails svnDetails = svnRepository.getSvnDetails(project.id);
            svnDetails = svnDetails != null ? svnDetails : new SvnDetails(); // TODO Null object pattern
            ProjectDetail projectDetail = build(project, svnDetails);
            result.add(projectDetail);
        }
        return result;
    }

    private ProjectDetail build(Project project, SvnDetails svnDetails) {
        ProjectDetail projectDetail = new ProjectDetail();

        projectDetail.id = project.id;
        projectDetail.label = StringUtils.isNotBlank(project.label)
                ? project.label
                : project.id;
        projectDetail.type = project.type;
        projectDetail.svn = project.svnUrl;
        projectDetail.mvn = project.maven;
        projectDetail.jenkins = project.jenkinsJob;

        projectDetail.changes = svnDetails.changes;
        projectDetail.latest = svnDetails.latest;
        projectDetail.released = svnDetails.released;

        Random random = new Random(); // FIXME Random ?? seriously
        projectDetail.loc = random.nextInt(20000);
        projectDetail.coverage = Math.round(random.nextDouble() * 6000.0) / 100.0;

        return projectDetail;
    }
}
