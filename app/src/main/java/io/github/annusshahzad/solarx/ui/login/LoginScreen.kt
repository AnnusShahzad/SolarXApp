package io.github.annusshahzad.solarx.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.annusshahzad.solarx.R
import io.github.annusshahzad.solarx.ui.theme.SolarXTheme

@Composable
fun LoginRoute(
    onLoggedIn: () -> Unit,
    viewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.isLoggedIn) {
        if (state.isLoggedIn) {
            viewModel.onLoginHandled()
            onLoggedIn()
        }
    }
    LoginScreen(
        state = state,
        onHostChange = viewModel::onHostChange,
        onSerialChange = viewModel::onSerialChange,
        onLogin = viewModel::login,
    )
}

@Composable
fun LoginScreen(
    state: LoginUiState,
    onHostChange: (String) -> Unit,
    onSerialChange: (String) -> Unit,
    onLogin: () -> Unit,
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .safeDrawingPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Image(
                painter = painterResource(R.mipmap.ic_launcher),
                contentDescription = null,
                modifier = Modifier.size(96.dp),
            )
            Text(text = stringResource(R.string.app_name), style = MaterialTheme.typography.displaySmall)
            Text(
                text = stringResource(R.string.login_tagline),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = state.host,
                onValueChange = onHostChange,
                label = { Text(stringResource(R.string.login_host_label)) },
                supportingText = { Text(stringResource(R.string.login_host_hint)) },
                singleLine = true,
                enabled = !state.isLoading,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.serial,
                onValueChange = onSerialChange,
                label = { Text(stringResource(R.string.login_serial_label)) },
                supportingText = { Text(stringResource(R.string.login_serial_hint)) },
                singleLine = true,
                enabled = !state.isLoading,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    autoCorrect = false,
                    keyboardType = KeyboardType.Ascii,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { onLogin() }),
                modifier = Modifier.fillMaxWidth(),
            )

            state.error?.let { error ->
                Text(
                    text = error.message(state.host.trim()),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { liveRegion = LiveRegionMode.Polite },
                )
            }

            Button(
                onClick = onLogin,
                enabled = !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text(stringResource(R.string.login_button))
                }
            }
        }
    }
}

@Composable
private fun LoginError.message(host: String): String = when (this) {
    LoginError.MissingFields -> stringResource(R.string.login_error_missing_fields)
    LoginError.InvalidAddress -> stringResource(R.string.login_error_invalid_address)
    LoginError.SerialHasSpaces -> stringResource(R.string.login_error_serial_spaces)
    LoginError.WrongSerial -> stringResource(R.string.login_error_wrong_serial)
    LoginError.Unreachable -> stringResource(R.string.login_error_unreachable, host)
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    SolarXTheme {
        LoginScreen(
            state = LoginUiState(host = "192.168.10.10", serial = "SXXXXXXXXX"),
            onHostChange = {},
            onSerialChange = {},
            onLogin = {},
        )
    }
}
