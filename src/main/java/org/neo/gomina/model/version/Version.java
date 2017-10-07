package org.neo.gomina.model.version;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Version implements Comparable<Version> {

    private String version;
    private long revision;

    public Version(String version, long revision) {
        this.version = version;
        this.revision = revision;
    }

    public Version(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    public long getRevision() {
        return revision;
    }

    @Override
    public String toString() {
        return version + "@" + revision;
    }

    @Override
    public int compareTo(Version other) {
        if (other == null) {
            return 1;
        }
        int res = Version.versionCompare(this.version, other.version);
        if (res == 0 && this.isSnapshot()) {
            return Integer.signum((int) (this.revision - other.revision));
        }
        return res;
    }

    public static int versionCompare(String str1, String str2) {
        if (StringUtils.isBlank(str1) && StringUtils.isBlank(str2)) {
            return 0;
        }
        if (StringUtils.isBlank(str2)) {
            return 1;
        }
        if (StringUtils.isBlank(str1)) {
            return -1;
        }
        int str1snapshot = str1 != null && str1.contains("-SNAPSHOT") ? 0 : 1;
        int str2snapshot = str2 != null && str2.contains("-SNAPSHOT") ? 0 : 1;

        str1 = str1 != null ? str1.replace("-SNAPSHOT", "") : "0";
        str2 = str2 != null ? str2.replace("-SNAPSHOT", "") : "0";

        String[] vals1 = str1.split("\\.");
        String[] vals2 = str2.split("\\.");
        int i = 0;
        // set index to first non-equal ordinal or length of shortest version string
        while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i])) {
            i++;
        }
        // compare first non-equal ordinal number
        if (i < vals1.length && i < vals2.length) {
            int diff = Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i]));
            return Integer.signum(diff);
        }
        // the strings are equal or one string is a substring of the other
        // e.g. "1.2.3" = "1.2.3" or "1.2.3" < "1.2.3.4"
        else if (vals1.length == vals2.length) {
            return Integer.signum(str1snapshot - str2snapshot);
        }
        else {
            return Integer.signum(vals1.length - vals2.length);
        }
    }

    public boolean isSnapshot() {
        return version != null && version.contains("SNAPSHOT");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Version version1 = (Version) o;

        return new EqualsBuilder()
                .append(revision, version1.revision)
                .append(version, version1.version)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(version)
                .append(revision)
                .toHashCode();
    }
}
