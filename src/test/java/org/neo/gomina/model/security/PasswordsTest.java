package org.neo.gomina.model.security;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class PasswordsTest {
    @Test
    public void getRealPassword() throws Exception {
        Passwords passwords = new Passwords();

        assertThat(new String(Base64.encodeBase64("super$$sec".getBytes()))).isEqualTo("c3VwZXIkJHNlYw==");
        assertThat(passwords.getRealPassword("@amokrane")).isEqualTo("mysecret");
        assertThat(passwords.getRealPassword("@vac")).isEqualTo("super$$sec");
    }

}