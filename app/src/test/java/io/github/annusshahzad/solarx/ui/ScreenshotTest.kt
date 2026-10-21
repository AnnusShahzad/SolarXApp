package io.github.annusshahzad.solarx.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import io.github.annusshahzad.solarx.data.GridAlert
import io.github.annusshahzad.solarx.data.InverterReading
import io.github.annusshahzad.solarx.ui.login.LoginError
import io.github.annusshahzad.solarx.ui.login.LoginScreen
import io.github.annusshahzad.solarx.ui.login.LoginUiState
import io.github.annusshahzad.solarx.ui.monitor.DashboardScreen
import io.github.annusshahzad.solarx.ui.monitor.DetailsScreen
import io.github.annusshahzad.solarx.ui.monitor.MonitorUiState
import io.github.annusshahzad.solarx.ui.monitor.SettingsScreen
import io.github.annusshahzad.solarx.ui.theme.SolarXTheme
import org.junit.Rule
import org.junit.Test

/**
 * Golden screenshots of every screen. Record with `./gradlew recordPaparazziDebug` and check with
 * `./gradlew verifyPaparazziDebug`; the images also illustrate the README.
 */
class ScreenshotTest {

    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_5)

    private val exporting = MonitorUiState(reading = InverterReading(solarW = 3_250, gridW = 1_180))
    private val importing = MonitorUiState(
        reading = InverterReading(solarW = 420, gridW = -1_350),
        gridAlert = GridAlert(enabled = true, thresholdW = 1_000),
    )
    private val noPadding = PaddingValues()

    private fun snapshot(darkTheme: Boolean = false, content: @Composable () -> Unit) {
        paparazzi.snapshot {
            SolarXTheme(darkTheme = darkTheme) {
                Surface { content() }
            }
        }
    }

    @Test
    fun login() = snapshot {
        LoginScreen(LoginUiState(host = "192.168.10.10", serial = "SXXXXXXXXX"), {}, {}, {})
    }

    @Test
    fun loginError() = snapshot {
        LoginScreen(
            LoginUiState(host = "192.168.1.50", serial = "SXXXXXXXXX", error = LoginError.Unreachable),
            {}, {}, {},
        )
    }

    @Test
    fun dashboardExporting() = snapshot { DashboardScreen(exporting, noPadding, animate = false) }

    @Test
    fun dashboardImportingWithAlert() = snapshot { DashboardScreen(importing, noPadding, animate = false) }

    @Test
    fun dashboardOfflineDark() = snapshot(darkTheme = true) {
        DashboardScreen(exporting.copy(isOnline = false), noPadding, animate = false)
    }

    @Test
    fun dashboardWaiting() = snapshot { DashboardScreen(MonitorUiState(), noPadding, animate = false) }

    @Test
    fun details() = snapshot { DetailsScreen(exporting, noPadding) }

    @Test
    fun settings() = snapshot {
        SettingsScreen(GridAlert(enabled = true, thresholdW = 1_000), {}, {}, {}, noPadding)
    }
}
