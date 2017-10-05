package org.neo.gomina.api.projects;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.neo.gomina.model.project.Project;
import org.neo.gomina.model.project.ProjectRepository;
import org.neo.gomina.model.sonar.Sonar;
import org.neo.gomina.model.sonar.SonarIndicators;
import org.neo.gomina.model.scm.ScmConnector;
import org.neo.gomina.model.scm.model.Commit;
import org.neo.gomina.model.scm.model.ScmDetails;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProjectDetailRepository {

    private final static Logger logger = LogManager.getLogger(ProjectDetailRepository.class);

    @Inject private ProjectRepository projectRepository;
    @Inject private ScmConnector scmConnector;
    @Inject private Sonar sonar;

    public List<ProjectDetail> getProjects() {
        List<ProjectDetail> result = new ArrayList<>();
        Map<String, SonarIndicators> sonarIndicatorsMap = sonar.getMetrics();
        for (Project project : projectRepository.getProjects()) {
            ScmDetails scmDetails = scmConnector.getSvnDetails(project.svnUrl);
            scmDetails = scmDetails != null ? scmDetails : new ScmDetails(); // TODO Null object pattern
            SonarIndicators sonarIndicators = sonarIndicatorsMap.get(project.maven);
            ProjectDetail projectDetail = build(project, scmDetails, null, sonarIndicators);
            result.add(projectDetail);
        }
        return result;
    }

    public ProjectDetail getProject(String projectId) throws Exception {
        Project project = projectRepository.getProject(projectId);
        if (project != null) {
            ScmDetails scmDetails = scmConnector.getSvnDetails(project.svnUrl);
            SonarIndicators sonarIndicators = sonar.getMetrics(project.maven).get(project.maven);
            List<CommitLogEntry> commitLog = map(scmConnector.getCommitLog(project.svnUrl));
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
