package org.neo.gomina.model.sonar;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class SonarIndicators {

    Double loc;
    Double coverage;

    public SonarIndicators() {
    }

    public SonarIndicators(Double loc, Double coverage) {
        this.loc = loc;
        this.coverage = coverage;
    }

    public Double getLoc() {
        return loc;
    }

    public Double getCoverage() {
        return coverage;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("loc", loc)
                .append("coverage", coverage)
                .toString();
    }
}
