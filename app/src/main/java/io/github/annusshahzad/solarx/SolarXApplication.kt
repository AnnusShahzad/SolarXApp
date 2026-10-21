package io.github.annusshahzad.solarx

import android.app.Application
import io.github.annusshahzad.solarx.data.InverterClient
import io.github.annusshahzad.solarx.data.SettingsRepository

/** Holds the app-wide dependencies that the ViewModel factories hand out. */
class SolarXApplication : Application() {
    val inverterClient by lazy { InverterClient() }
    val settings by lazy { SettingsRepository(this) }
}
