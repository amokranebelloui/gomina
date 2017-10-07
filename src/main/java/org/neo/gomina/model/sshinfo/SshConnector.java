package org.neo.gomina.model.sshinfo;

public interface SshConnector {

    SshDetails getDetails(String host, String folder);

}
