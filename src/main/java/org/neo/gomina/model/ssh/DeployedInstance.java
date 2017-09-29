package org.neo.gomina.model.ssh;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class DeployedInstance {

    private String name;
    private String folder;

    private String deployedVersion;
    private Boolean confCommitted;

    public DeployedInstance(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append("name", name)
                .append("folder", folder)
                .append("deployedVersion", deployedVersion)
                .append("confCommitted", confCommitted)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        DeployedInstance that = (DeployedInstance) o;

        return new EqualsBuilder()
                .append(name, that.name)
                .append(folder, that.folder)
                .append(deployedVersion, that.deployedVersion)
                .append(confCommitted, that.confCommitted)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(name)
                .append(folder)
                .append(deployedVersion)
                .append(confCommitted)
                .toHashCode();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public String getDeployedVersion() {
        return deployedVersion;
    }

    public void setDeployedVersion(String deployedVersion) {
        this.deployedVersion = deployedVersion;
    }

    public Boolean getConfCommitted() {
        return confCommitted;
    }

    public void setConfCommitted(Boolean confCommitted) {
        this.confCommitted = confCommitted;
    }
}
