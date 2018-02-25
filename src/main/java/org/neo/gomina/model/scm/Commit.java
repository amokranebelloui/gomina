package org.neo.gomina.model.scm;

import java.util.Date;

public class Commit {

    public String revision;
    public Date date;
    public String author;
    public String message;

    public String release; // new version: if the commit is a post release version change
    public String newVersion; // version: if the commit is a release

    @Override
    public String toString() {
        return String.format("Commit{rev='%s', date=%s, author='%s', msg='%s', release='%s', newVersion='%s'}",
                revision, date, author, message, release, newVersion);
    }
}
