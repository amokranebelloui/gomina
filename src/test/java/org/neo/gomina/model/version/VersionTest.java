package org.neo.gomina.model.version;

import org.fest.assertions.Assertions;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

public class VersionTest {

    @Test
    public void testVersion() throws Exception {

        assertThat(new Version("2.3.1").compareTo(new Version("2.3.7"))).isEqualTo(-1);
        assertThat(new Version("2.3.1").compareTo(new Version("3"))).isEqualTo(-1);
        assertThat(new Version("2.3.1").compareTo(new Version("1.1"))).isEqualTo(1);
        assertThat(new Version("2.3.1").compareTo(new Version("2.3.1"))).isEqualTo(0);
        assertThat(new Version("2.3.1").compareTo(new Version("2.3.1-SNAPSHOT"))).isEqualTo(1);
        assertThat(new Version("2.3.1-SNAPSHOT").compareTo(new Version("2.3.1-SNAPSHOT"))).isEqualTo(0);
        assertThat(new Version("2.3.2-SNAPSHOT", 2301l).compareTo(new Version("2.3.1", 2301l))).isEqualTo(1);
        assertThat(new Version("1.2.5", 2301l).compareTo(new Version("1.2.5", 0l))).isEqualTo(0);
        assertThat(new Version("1.2.5-SNAPSHOT", 2301l).compareTo(new Version("1.2.5-SNAPSHOT", 0l))).isEqualTo(1);
        assertThat(new Version("1.3.6").compareTo(new Version(null))).isEqualTo(1);
        assertThat(new Version("1.3.6").compareTo(null)).isEqualTo(1);

        List<Version> versions = Arrays.asList(new Version("2.3.1"), new Version("1.2.5-SNAPSHOT"), new Version("1.2.5", 2301l), new Version(null));
        System.out.println(versions);
        Collections.sort(versions);
        assertThat(versions).containsExactly(new Version(null), new Version("1.2.5-SNAPSHOT"), new Version("1.2.5", 2301l), new Version("2.3.1"));
        System.out.println(versions);
    }

}