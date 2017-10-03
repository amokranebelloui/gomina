package org.neo.gomina.model.sonar;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class SonarIndicators {

    public String code;

    public Double loc;
    public Double coverage;

    public SonarIndicators() {
    }

    public SonarIndicators(String code, Double loc, Double coverage) {
        this.code = code;
        this.loc = loc;
        this.coverage = coverage;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("code", code)
                .append("loc", loc)
                .append("coverage", coverage)
                .toString();
    }
}
