package org.neo.gomina.api.projects;

import java.util.List;

public class ProjectDetail {

    public String id;
    public String label;
    public String type;
    public String svn;
    public String mvn;
    public String jenkins;

    public Integer changes;
    public String latest;
    public String released;

    public Double loc;
    public Double coverage;

    public List<CommitLogEntry> commitLog;

}
