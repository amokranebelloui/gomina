package org.neo.gomina.api.projects;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.neo.gomina.model.project.Project;
import org.neo.gomina.model.project.Projects;
import org.neo.gomina.model.scm.Commit;
import org.neo.gomina.model.scminfo.ScmConnector;
import org.neo.gomina.model.scminfo.ScmDetails;
import org.neo.gomina.model.sonar.SonarConnector;
import org.neo.gomina.model.sonar.SonarIndicators;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProjectsBuilder {

    private final static Logger logger = LogManager.getLogger(ProjectsBuilder.class);

    @Inject private Projects projects;
    @Inject private ScmConnector scmConnector;
    @Inject private SonarConnector sonarConnector;

    public List<ProjectDetail> getProjects() {
        List<ProjectDetail> result = new ArrayList<>();
        Map<String, SonarIndicators> sonarIndicatorsMap = sonarConnector.getMetrics();
        for (Project project : projects.getProjects()) {
            ScmDetails scmDetails = scmConnector.getSvnDetails(project.svnRepo, project.svnUrl);
            SonarIndicators sonarIndicators = sonarIndicatorsMap.get(project.maven);
            ProjectDetail projectDetail = build(project, scmDetails, null, sonarIndicators);
            result.add(projectDetail);
        }
        return result;
    }

    public ProjectDetail getProject(String projectId) throws Exception {
        Project project = projects.getProject(projectId);
        if (project != null) {
            ScmDetails scmDetails = scmConnector.getSvnDetails(project.svnRepo, project.svnUrl);
            SonarIndicators sonarIndicators = sonarConnector.getMetrics(project.maven).get(project.maven);
            List<CommitLogEntry> commitLog = map(scmConnector.getCommitLog(project.svnRepo, project.svnUrl));
            return build(project, scmDetails, commitLog, sonarIndicators);
        }
        return null;
    }

    private List<CommitLogEntry> map(List<Commit> commitLog) {
        List<CommitLogEntry> result = new ArrayList<>();
        for (Commit commit : commitLog) {
            CommitLogEntry commitLogEntry = new CommitLogEntry();
            commitLogEntry.revision = commit.revision;
            commitLogEntry.date = commit.date;
            commitLogEntry.author = commit.author;
            commitLogEntry.message = commit.message;
            result.add(commitLogEntry);
        }
        return result;
    }

    private ProjectDetail build(Project project, ScmDetails scmDetails, List<CommitLogEntry> commitLog, SonarIndicators sonarIndicators) {
        ProjectDetail projectDetail = new ProjectDetail();

        projectDetail.id = project.id;
        projectDetail.label = StringUtils.isNotBlank(project.label)
                ? project.label
                : project.id;
        projectDetail.type = project.type;
        projectDetail.repo = project.svnRepo;
        projectDetail.svn = project.svnUrl;
        projectDetail.mvn = project.maven;
        projectDetail.jenkins = project.jenkinsJob;

        if (scmDetails != null) {
            projectDetail.changes = scmDetails.changes;
            projectDetail.latest = scmDetails.latest;
            projectDetail.released = scmDetails.released;
        }

        if (sonarIndicators != null) {
            projectDetail.loc = sonarIndicators.loc;
            projectDetail.coverage = sonarIndicators.coverage;
        }

        projectDetail.commitLog = commitLog;

        return projectDetail;
    }

}
