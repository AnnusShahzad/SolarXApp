package io.github.annusshahzad.solarx.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.annusshahzad.solarx.SolarXApplication
import io.github.annusshahzad.solarx.data.InverterClient
import io.github.annusshahzad.solarx.data.InverterConnection
import io.github.annusshahzad.solarx.data.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

enum class LoginError { MissingFields, InvalidAddress, SerialHasSpaces, WrongSerial, Unreachable }

data class LoginUiState(
    val host: String = "",
    val serial: String = "",
    val isLoading: Boolean = false,
    val error: LoginError? = null,
    val isLoggedIn: Boolean = false,
)

/** Returns the first problem with the entered connection details, or null if they can be tried. */
fun validateLogin(host: String, serial: String): LoginError? = when {
    host.isBlank() || serial.isBlank() -> LoginError.MissingFields
    serial.any { it.isWhitespace() } -> LoginError.SerialHasSpaces
    InverterClient.urlFor(host) == null -> LoginError.InvalidAddress
    else -> null
}

class LoginViewModel(
    private val client: InverterClient,
    private val settings: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        settings.connection.let { LoginUiState(host = it.host, serial = it.serial) }
    )
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onHostChange(host: String) = _uiState.update { it.copy(host = host, error = null) }

    fun onSerialChange(serial: String) = _uiState.update { it.copy(serial = serial.uppercase(), error = null) }

    fun login() {
        val state = _uiState.value
        if (state.isLoading) return
        val connection = InverterConnection(host = state.host.trim(), serial = state.serial.trim())
        validateLogin(connection.host, connection.serial)?.let { error ->
            _uiState.update { it.copy(error = error) }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val error = try {
                if (client.verifySerial(connection)) null else LoginError.WrongSerial
            } catch (e: IOException) {
                LoginError.Unreachable
            }
            if (error == null) settings.connection = connection
            _uiState.update { it.copy(isLoading = false, error = error, isLoggedIn = error == null) }
        }
    }

    /** Call once the UI has navigated away after a successful login. */
    fun onLoginHandled() = _uiState.update { it.copy(isLoggedIn = false) }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as SolarXApplication
                LoginViewModel(app.inverterClient, app.settings)
            }
        }
    }
}
