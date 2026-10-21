package io.github.annusshahzad.solarx.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import io.github.annusshahzad.solarx.R
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

    /** The app logo; the README and the repository banner use this render. */
    @Test
    fun logo() = paparazzi.snapshot {
        Box(Modifier.fillMaxSize().background(Color.White), contentAlignment = Alignment.Center) {
            Image(painterResource(R.drawable.ic_logo), contentDescription = null, modifier = Modifier.size(320.dp))
        }
    }

    @Test
    fun settings() = snapshot {
        SettingsScreen(GridAlert(enabled = true, thresholdW = 1_000), {}, {}, {}, noPadding)
    }
}
