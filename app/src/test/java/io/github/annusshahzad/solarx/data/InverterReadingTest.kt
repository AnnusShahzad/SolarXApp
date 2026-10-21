package io.github.annusshahzad.solarx.data

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

class InverterReadingTest {

    /** The register arithmetic the app shipped with, kept to prove the new version matches it. */
    private fun legacyGridW(low: Int, high: Int): Int {
        val combined = if (high < 32768) low + high * 65536L else low + high * 65536L - 4294967296L
        return (combined.toInt() * 0.1).toInt()
    }

    private fun realTimeData(solar: Int, gridLow: Int, gridHigh: Int) =
        List(100) { 0 }.toMutableList().apply {
            this[9] = solar
            this[29] = gridLow
            this[30] = gridHigh
        }

    @Test
    fun `combines registers into a signed 32-bit value`() {
        assertEquals(0, InverterReading.signed32(low = 0, high = 0))
        assertEquals(12_345, InverterReading.signed32(low = 12_345, high = 0))
        assertEquals(65_536, InverterReading.signed32(low = 0, high = 1))
        assertEquals(-1, InverterReading.signed32(low = 0xFFFF, high = 0xFFFF))
        assertEquals(-5_000, InverterReading.signed32(low = 65_536 - 5_000, high = 0xFFFF))
    }

    @Test
    fun `grid power matches the original conversion for exports and imports`() {
        val samples = (0..65_535 step 97) + listOf(0, 1, 9, 10, 11, 65_535)
        for (high in listOf(0, 1, 2, 65_533, 65_534, 65_535)) {
            for (low in samples) {
                val reading = InverterReading.fromRealTimeData(realTimeData(solar = 0, gridLow = low, gridHigh = high))
                assertEquals("low=$low high=$high", legacyGridW(low, high), reading.gridW)
            }
        }
    }

    @Test
    fun `reads solar power and grid power in watts`() {
        // 0xFFFF:0xEC78 is -5000 tenths of a watt, i.e. importing 500 W.
        val reading = InverterReading.fromRealTimeData(realTimeData(solar = 1_200, gridLow = 0xEC78, gridHigh = 0xFFFF))

        assertEquals(1_200, reading.solarW)
        assertEquals(-500, reading.gridW)
    }

    @Test
    fun `home load is solar minus export`() {
        assertEquals(700, InverterReading(solarW = 1_000, gridW = 300).loadW)
        assertEquals(1_500, InverterReading(solarW = 1_000, gridW = -500).loadW)
        assertEquals(250, InverterReading(solarW = 0, gridW = -250).loadW)
    }

    @Test
    fun `grid import is zero while exporting`() {
        assertEquals(0, InverterReading(solarW = 1_000, gridW = 300).gridImportW)
        assertEquals(0, InverterReading(solarW = 0, gridW = 0).gridImportW)
        assertEquals(500, InverterReading(solarW = 1_000, gridW = -500).gridImportW)
    }

    @Test(expected = IOException::class)
    fun `rejects data that is too short`() {
        InverterReading.fromRealTimeData(List(30) { 0 })
    }
}
