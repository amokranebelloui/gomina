package org.neo.gomina.model.scm.model;

import java.util.Date;

public class Commit {

    public String revision;
    public Date date;
    public String author;
    public String message;

    @Override
    public String toString() {
        return String.format("Commit{rev='%s', date=%s, author='%s', msg='%s'}",
                revision, date, author, message);
    }
}
