package org.neo.gomina.model.ssh;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class SshAuthentication {

    Properties props = new Properties();

    private Map<String, SshAuth> serversAuth = new HashMap<>();

    public SshAuthentication() {
        try {
            //props.load(new FileInputStream(new File("pass.properties")));

            serversAuth.put("Amokranes-MacBook-Pro.local", new SshAuth("Amokrane", "", "Amokrane"));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public SshAuth get(String host) {
        return serversAuth.get(host);
    }

}
