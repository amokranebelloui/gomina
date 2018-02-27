package org.neo.gomina.model.version

import org.fest.assertions.Assertions.assertThat
import org.junit.Test
import java.util.*

class VersionTest {

    @Test
    fun testVersion() {

        assertThat(Version("2.3.1").compareTo(Version("2.3.7"))).isEqualTo(-1)
        assertThat(Version("2.3.1").compareTo(Version("3"))).isEqualTo(-1)
        assertThat(Version("2.3.1").compareTo(Version("1.1"))).isEqualTo(1)
        assertThat(Version("2.3.1").compareTo(Version("2.3.1"))).isEqualTo(0)
        assertThat(Version("2.3.1").compareTo(Version("2.3.1-SNAPSHOT"))).isEqualTo(1)
        assertThat(Version("2.3.1-SNAPSHOT").compareTo(Version("2.3.1-SNAPSHOT"))).isEqualTo(0)
        assertThat(Version("2.3.2-SNAPSHOT", 2301L).compareTo(Version("2.3.1", 2301L))).isEqualTo(1)
        assertThat(Version("1.2.5", 2301L).compareTo(Version("1.2.5", 0L))).isEqualTo(0)
        assertThat(Version("1.2.5-SNAPSHOT", 2301L).compareTo(Version("1.2.5-SNAPSHOT", 0L))).isEqualTo(1)
        assertThat(Version("1.3.6").compareTo(Version())).isEqualTo(1)
        //assertThat(new Version("1.3.6").compareTo(null)).isEqualTo(1);
    }

    @Test
    fun testOrdering() {
        val versions = Arrays.asList(Version("2.3.1"), Version("1.2.5-SNAPSHOT"), Version("1.2.5", 2301L), Version())
        println(versions)
        Collections.sort(versions)
        assertThat(versions).containsExactly(Version(), Version("1.2.5-SNAPSHOT"), Version("1.2.5", 2301L), Version("2.3.1"))
        println(versions)
    }

}