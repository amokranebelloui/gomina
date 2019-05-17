package org.neo.gomina.integration.scm.dummy

import org.apache.logging.log4j.LogManager
import org.junit.Test

class DummyScmClientTest {

    @Test
    @Throws(Exception::class)
    fun getLog() {
        val client = DummyScmClient("OMS/Server/tradex-basketmanager")
        val log = client.getLog("trunk", "0", 100)
        log.forEach { logger.info(it) }
    }

    @Test
    @Throws(Exception::class)
    fun getFile() {
        val client = DummyScmClient("OMS/Server/tradex-basketmanager")
        val file = client.getFile("trunk", "doc.md", "-1")
        logger.info(file)
    }

    companion object {
        private val logger = LogManager.getLogger(DummyScmClientTest::class.java)
    }

}