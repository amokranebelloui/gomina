package org.neo.gomina.model.sshinfo;

public interface SshConnector {

    void analyze();

    SshDetails getDetails(String host, String folder);

}
