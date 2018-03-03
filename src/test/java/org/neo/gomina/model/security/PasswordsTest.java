package org.neo.gomina.model.security;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;

import java.io.File;
import java.util.Scanner;

import static org.fest.assertions.Assertions.assertThat;

public class PasswordsTest {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Password:");
        String password = scanner.next();
        System.out.println(new String(Base64.encodeBase64(password.getBytes())));
    }

    @Test
    public void getRealPassword() throws Exception {
        Passwords passwords = new Passwords(new File("config/pass.properties.sample"));

        assertThat(new String(Base64.encodeBase64("super$$sec".getBytes()))).isEqualTo("c3VwZXIkJHNlYw==");
        assertThat(passwords.getRealPassword("@amokrane")).isEqualTo("mysecret");
        assertThat(passwords.getRealPassword("@vac")).isEqualTo("super$$sec");
    }

}