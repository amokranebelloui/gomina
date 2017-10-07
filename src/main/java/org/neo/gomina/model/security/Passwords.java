package org.neo.gomina.model.security;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Passwords {

    private final static Logger logger = LogManager.getLogger(Passwords.class);

    Properties props = new Properties();

    public Passwords() {
        try {
            props.load(new FileInputStream(new File("config/passwords.properties")));
        }
        catch (IOException e) {
            logger.error("Cannot load passwords");
        }
    }

    public String getRealPassword(String alias) {
        if (StringUtils.isNotBlank(alias)) {
            String encoded = props.getProperty(alias);
            return new String(Base64.decodeBase64(encoded.getBytes()));
        }
        return null;
    }

}
