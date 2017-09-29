package org.neo.gomina.model.ssh;

public class SshAuth {

    private String username;
    private String password;
    private String sudo;

    public SshAuth(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public SshAuth(String username, String password, String sudo) {
        this.username = username;
        this.password = password;
        this.sudo = sudo;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getSudo() {
        return sudo;
    }
}
