package org.neo.gomina.model.scm.model;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Date;

public class Commit {

    public String revision;
    public Date date;
    public String author;
    public String message;

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("revision", revision)
                .append("date", date)
                .append("author", author)
                .append("message", message)
                .toString();
    }
}
