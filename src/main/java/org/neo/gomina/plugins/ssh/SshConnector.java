package org.neo.gomina.plugins.ssh;

public interface SshConnector {

    void analyze();

    SshDetails getDetails(String host, String folder);

}
