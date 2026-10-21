package io.github.annusshahzad.solarx.ui

import io.github.annusshahzad.solarx.data.GridAlert
import io.github.annusshahzad.solarx.data.InverterReading
import io.github.annusshahzad.solarx.ui.login.LoginError
import io.github.annusshahzad.solarx.ui.login.validateLogin
import io.github.annusshahzad.solarx.ui.monitor.MonitorUiState
import io.github.annusshahzad.solarx.ui.monitor.parseThreshold
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UiLogicTest {

    @Test
    fun `login validation reports the first problem`() {
        assertEquals(LoginError.MissingFields, validateLogin(host = "", serial = "SABC"))
        assertEquals(LoginError.MissingFields, validateLogin(host = "192.168.10.10", serial = " "))
        assertEquals(LoginError.SerialHasSpaces, validateLogin(host = "192.168.10.10", serial = "SAB C"))
        assertEquals(LoginError.InvalidAddress, validateLogin(host = "192.168.10.10/x", serial = "SABC"))
        assertNull(validateLogin(host = "192.168.10.10", serial = "SABC"))
    }

    @Test
    fun `grid alert fires only when enabled and import exceeds the threshold`() {
        val importing600 = InverterReading(solarW = 0, gridW = -600)
        val alert = GridAlert(enabled = true, thresholdW = 500)

        assertTrue(MonitorUiState(reading = importing600, gridAlert = alert).isGridAlertActive)
        assertFalse(MonitorUiState(reading = importing600, gridAlert = alert.copy(enabled = false)).isGridAlertActive)
        assertFalse(MonitorUiState(reading = importing600, gridAlert = alert.copy(thresholdW = 600)).isGridAlertActive)
        assertFalse(MonitorUiState(reading = InverterReading(0, 900), gridAlert = alert).isGridAlertActive)
        assertFalse(MonitorUiState(reading = null, gridAlert = alert).isGridAlertActive)
    }

    @Test
    fun `threshold must be a positive whole number`() {
        assertEquals(250, parseThreshold("250"))
        assertEquals(250, parseThreshold(" 250 "))
        assertNull(parseThreshold("0"))
        assertNull(parseThreshold("-5"))
        assertNull(parseThreshold("2.5"))
        assertNull(parseThreshold(""))
        assertNull(parseThreshold("99999999999"))
    }
}
