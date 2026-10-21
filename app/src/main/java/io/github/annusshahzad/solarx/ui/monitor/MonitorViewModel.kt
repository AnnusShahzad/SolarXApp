package io.github.annusshahzad.solarx.ui.monitor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.annusshahzad.solarx.SolarXApplication
import io.github.annusshahzad.solarx.data.GridAlert
import io.github.annusshahzad.solarx.data.InverterClient
import io.github.annusshahzad.solarx.data.InverterReading
import io.github.annusshahzad.solarx.data.SettingsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import java.io.IOException

data class MonitorUiState(
    /** The latest successful reading, kept on screen while the inverter is unreachable. */
    val reading: InverterReading? = null,
    val isOnline: Boolean = true,
    val gridAlert: GridAlert = GridAlert(),
) {
    val isGridAlertActive: Boolean
        get() = gridAlert.enabled && reading != null && reading.gridImportW > gridAlert.thresholdW
}

/** Polls the inverter while the monitor screens are visible and holds the grid alert settings. */
class MonitorViewModel(
    private val client: InverterClient,
    private val settings: SettingsRepository,
) : ViewModel() {

    private val connection = settings.connection
    private val state = MutableStateFlow(MonitorUiState(gridAlert = settings.gridAlert))

    /**
     * Mirrors [state] and polls the inverter while anyone collects it. The state lives outside the
     * polling coroutine, so the last reading and the alert settings survive polling restarts.
     */
    val uiState: StateFlow<MonitorUiState> = channelFlow {
        launch { poll() }
        state.collect { send(it) }
    }.stateIn(
        scope = viewModelScope,
        // Keep polling through configuration changes, but stop soon after the app is backgrounded.
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = state.value,
    )

    private suspend fun poll() {
        while (true) {
            val reading = try {
                client.readRealTimeData(connection)
            } catch (e: IOException) {
                null
            }
            state.update {
                if (reading == null) it.copy(isOnline = false) else it.copy(reading = reading, isOnline = true)
            }
            delay(POLL_INTERVAL_MS)
        }
    }

    fun setGridAlertEnabled(enabled: Boolean) = updateGridAlert { it.copy(enabled = enabled) }

    fun setGridAlertThreshold(thresholdW: Int) = updateGridAlert { it.copy(thresholdW = thresholdW) }

    private fun updateGridAlert(transform: (GridAlert) -> GridAlert) {
        val alert = state.updateAndGet { it.copy(gridAlert = transform(it.gridAlert)) }.gridAlert
        settings.gridAlert = alert
    }

    companion object {
        const val POLL_INTERVAL_MS = 1_000L

        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as SolarXApplication
                MonitorViewModel(app.inverterClient, app.settings)
            }
        }
    }
}
