package io.github.annusshahzad.solarx.ui.monitor

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.annusshahzad.solarx.R

private enum class Tab(@StringRes val label: Int) {
    Dashboard(R.string.tab_dashboard),
    Details(R.string.tab_details),
    Settings(R.string.tab_settings),
}

@Composable
fun HomeRoute(
    onChangeInverter: () -> Unit,
    viewModel: MonitorViewModel = viewModel(factory = MonitorViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableStateOf(Tab.Dashboard) }

    // Back from another tab returns to the dashboard before leaving the app.
    BackHandler(enabled = tab != Tab.Dashboard) { tab = Tab.Dashboard }

    Scaffold(
        bottomBar = {
            NavigationBar {
                Tab.entries.forEach { item ->
                    NavigationBarItem(
                        selected = tab == item,
                        onClick = { tab = item },
                        icon = { Icon(item.icon(), contentDescription = null) },
                        label = { Text(stringResource(item.label), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    )
                }
            }
        },
    ) { padding ->
        when (tab) {
            Tab.Dashboard -> DashboardScreen(state, padding)
            Tab.Details -> DetailsScreen(state, padding)
            Tab.Settings -> SettingsScreen(
                gridAlert = state.gridAlert,
                onAlertEnabledChange = viewModel::setGridAlertEnabled,
                onThresholdChange = viewModel::setGridAlertThreshold,
                onChangeInverter = onChangeInverter,
                contentPadding = padding,
            )
        }
    }
}

@Composable
private fun Tab.icon(): ImageVector = when (this) {
    Tab.Dashboard -> ImageVector.vectorResource(R.drawable.baseline_dashboard_24)
    Tab.Details -> Icons.Filled.Info
    Tab.Settings -> ImageVector.vectorResource(R.drawable.baseline_settings_24)
}
