package io.github.annusshahzad.solarx.data

import kotlinx.coroutines.runBlocking
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import java.io.IOException

class InverterClientTest {

    private lateinit var server: MockWebServer
    private val client = InverterClient()
    private lateinit var connection: InverterConnection

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        connection = InverterConnection(host = "${server.hostName}:${server.port}", serial = "SABC123XYZ")
    }

    @After
    fun tearDown() {
        server.close()
    }

    private fun realTimeBody(solar: Int, gridLow: Int, gridHigh: Int): String {
        val data = List(100) { 0 }.toMutableList().apply {
            this[9] = solar
            this[29] = gridLow
            this[30] = gridHigh
        }
        return """{"sn":"SABC123XYZ","ver":"3.006.04","type":14,"Data":$data,"Information":[]}"""
    }

    private fun assertThrowsIOException(block: suspend () -> Unit) {
        try {
            runBlocking { block() }
            fail("Expected an IOException")
        } catch (expected: IOException) {
        }
    }

    @Test
    fun `verifySerial posts the password check form`() = runBlocking {
        server.enqueue(MockResponse(body = "Y"))

        assertTrue(client.verifySerial(connection))

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/", request.target)
        assertEquals("optType=newParaSetting&subOption=pwd&Value=SABC123XYZ", request.body?.utf8())
    }

    @Test
    fun `verifySerial is false when the dongle rejects the serial`() = runBlocking {
        server.enqueue(MockResponse(body = "N"))
        server.enqueue(MockResponse(body = ""))

        assertFalse(client.verifySerial(connection))
        assertFalse(client.verifySerial(connection))
    }

    @Test
    fun `readRealTimeData posts the read form and parses the reading`() = runBlocking {
        server.enqueue(MockResponse(body = realTimeBody(solar = 2_400, gridLow = 8_000, gridHigh = 0)))

        val reading = client.readRealTimeData(connection)

        assertEquals(InverterReading(solarW = 2_400, gridW = 800), reading)
        assertEquals("optType=ReadRealTimeData&pwd=SABC123XYZ", server.takeRequest().body?.utf8())
    }

    @Test
    fun `HTTP errors become IOExceptions`() {
        server.enqueue(MockResponse(code = 500))
        assertThrowsIOException { client.readRealTimeData(connection) }
    }

    @Test
    fun `malformed responses become IOExceptions`() {
        server.enqueue(MockResponse(body = "not json"))
        server.enqueue(MockResponse(body = """{"Data":[1,2,3]}"""))
        server.enqueue(MockResponse(body = """{"Information":[]}"""))

        repeat(3) { assertThrowsIOException { client.readRealTimeData(connection) } }
    }

    @Test
    fun `an unreachable inverter becomes an IOException`() {
        val port = server.port
        server.close()
        assertThrowsIOException {
            client.readRealTimeData(InverterConnection(host = "127.0.0.1:$port", serial = "SABC123XYZ"))
        }
    }

    @Test
    fun `urlFor accepts IP addresses and host names only`() {
        assertNotNull(InverterClient.urlFor("192.168.10.10"))
        assertNotNull(InverterClient.urlFor("192.168.1.50:8080"))
        assertNotNull(InverterClient.urlFor("inverter.local"))
        assertNull(InverterClient.urlFor(""))
        assertNull(InverterClient.urlFor("192.168.1.50/path"))
        assertNull(InverterClient.urlFor("192.168. 1.50"))
        assertNull(InverterClient.urlFor("http://"))
    }
}
