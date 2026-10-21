package io.github.annusshahzad.solarx.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/** Warn when the house draws more than [thresholdW] watts from the grid. */
data class GridAlert(val enabled: Boolean = false, val thresholdW: Int = DEFAULT_THRESHOLD_W) {
    companion object {
        const val DEFAULT_THRESHOLD_W = 100
    }
}

/** Stores the inverter connection and the grid alert in [SharedPreferences]. */
class SettingsRepository(context: Context) {

    private val prefs = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)

    /** The last connection that logged in successfully, or the factory default address. */
    var connection: InverterConnection
        get() = InverterConnection(
            host = prefs.getString(KEY_HOST, null) ?: DEFAULT_HOST,
            serial = prefs.getString(KEY_SERIAL, null).orEmpty(),
        )
        set(value) = prefs.edit {
            putString(KEY_HOST, value.host)
            putString(KEY_SERIAL, value.serial)
        }

    var gridAlert: GridAlert
        get() = GridAlert(
            enabled = prefs.getBoolean(KEY_ALERT_ENABLED, false),
            thresholdW = prefs.getInt(KEY_ALERT_THRESHOLD, GridAlert.DEFAULT_THRESHOLD_W),
        )
        set(value) = prefs.edit {
            putBoolean(KEY_ALERT_ENABLED, value.enabled)
            putInt(KEY_ALERT_THRESHOLD, value.thresholdW)
        }

    private companion object {
        /** Address the Pocket WiFi dongle uses on its own access point. */
        const val DEFAULT_HOST = "192.168.10.10"
        const val KEY_HOST = "IPAddress"
        const val KEY_SERIAL = "Serial"
        const val KEY_ALERT_ENABLED = "GridAlertEnabled"
        const val KEY_ALERT_THRESHOLD = "GridAlertThresholdW"
    }
}
