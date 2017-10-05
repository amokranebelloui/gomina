package org.neo.gomina.model.scm.model;

public class ScmDetails {

    public String url;
    public String latest;
    public String latestRevision;
    public String released;
    public String releasedRevision;
    public Integer changes;

    @Override
    public String toString() {
        return String.format("SvnDetails{id='%s', latest='%s'/'%s', released='%s'/'%s', changes=%d}",
                url, latest, latestRevision, released, releasedRevision, changes);
    }
}
