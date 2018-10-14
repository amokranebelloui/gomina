package org.neo.gomina.model.version

import org.fest.assertions.Assertions.assertThat
import org.junit.Test
import java.util.*

class VersionTest {

    @Test
    fun testVersion() {

        assertThat(Version("2.3.1").compareTo(Version("2.3.7"))).isEqualTo(-1)
        assertThat(Version("2.3.1") < Version("2.3.7")).isTrue()
        assertThat(Version("2.3.1") > Version("2.3.7")).isFalse()
        assertThat(Version("2.3.1") == Version("2.3.7")).isFalse()

        assertThat(Version("2.3.1").compareTo(Version("3"))).isEqualTo(-1)
        assertThat(Version("2.3.1").compareTo(Version("1.1"))).isEqualTo(1)
        assertThat(Version("2.3.1").compareTo(Version("2.3.1"))).isEqualTo(0)
        assertThat(Version("2.3.1").compareTo(Version("2.3.1-SNAPSHOT"))).isEqualTo(1)
        assertThat(Version("2.3.1-SNAPSHOT").compareTo(Version("2.3.1-SNAPSHOT"))).isEqualTo(0)
        assertThat(Version("2.3.2-SNAPSHOT", "2301").compareTo(Version("2.3.1", "2301"))).isEqualTo(1)
        assertThat(Version("1.2.5", "2301").compareTo(Version("1.2.5", ""))).isEqualTo(0)
        assertThat(Version("1.2.5-SNAPSHOT", "2301").compareTo(Version("1.2.5-SNAPSHOT", ""))).isEqualTo(1)
        assertThat(Version("1.3.6").compareTo(Version())).isEqualTo(1)
        //assertThat(new Version("1.3.6").compareTo(null)).isEqualTo(1);
    }

    @Test
    fun testOrdering() {
        val versions = Arrays.asList(Version("2.3.1"), Version("1.2.5-SNAPSHOT"), Version("1.2.5", "2301"), Version())
        println(versions)
        Collections.sort(versions)
        assertThat(versions).containsExactly(Version(), Version("1.2.5-SNAPSHOT"), Version("1.2.5", "2301"), Version("2.3.1"))
        println(versions)
    }

}