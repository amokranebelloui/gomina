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
import java.util.Date;
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

    public ProjectDetail getProject(String projectId) {
        Project project = projectRepository.getProject(projectId);
        if (project != null) {
            SvnDetails svnDetails = svnRepository.getSvnDetails(projectId);
            return build(project, svnDetails);
        }
        return null;
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

        projectDetail.commitLog = sampleCommitLog();

        return projectDetail;
    }

    private List<CommitLogEntry> sampleCommitLog() {
        List<CommitLogEntry> result = new ArrayList<>();
        // FIXME Sample Data

        result.add(buildEntry("35490", new Date(), "amokrane", "refactor"));
        result.add(buildEntry("35488", new Date(), "amokrane", "[JIRA-5409] feature 2"));
        result.add(buildEntry("35487", new Date(), "amokrane", "feature1"));
        result.add(buildEntry("35469", new Date(), "amokrane", "feature 0"));
        result.add(buildEntry("34561", new Date(), "amokrane", "POC, lkhs sdpfousd sdfhpsmdnf sfspdh dk s kjf s jkhsdkjhdfos sjdfjs d sdkmfdf shjehkjdfksbdf  ukejhdkh sjd ksdhfjk qksjvkdv kbqsdbqk"));
        result.add(buildEntry("34560", new Date(), "amokrane", "structure"));
        result.add(buildEntry("33982", new Date(), "amokrane", "initial commit"));
        return result;
    }

    private CommitLogEntry buildEntry(String revision, Date date, String author, String message) {
        CommitLogEntry commitLogEntry = new CommitLogEntry();

        commitLogEntry.revision = revision;
        commitLogEntry.date = date;
        commitLogEntry.author = author;
        commitLogEntry.message = message;

        return commitLogEntry;
    }
}
