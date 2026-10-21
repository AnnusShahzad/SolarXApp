package io.github.annusshahzad.solarx.ui.monitor

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.annusshahzad.solarx.R
import io.github.annusshahzad.solarx.data.InverterReading
import kotlin.math.abs

@Composable
fun DashboardScreen(
    state: MonitorUiState,
    contentPadding: PaddingValues,
    animate: Boolean = true,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
    ) {
        StatusBanners(state)
        val reading = state.reading
        if (reading == null) {
            WaitingForData(Modifier.weight(1f))
            return@Column
        }
        // Short screens (landscape, split screen) scroll instead of squeezing the diagram.
        BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
            val roomy = maxHeight >= EnergyFlowMinHeight + SummaryRowHeight
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (roomy) Modifier else Modifier.verticalScroll(rememberScrollState())),
            ) {
                EnergyFlowDiagram(
                    reading = reading,
                    animate = animate,
                    modifier = Modifier
                        .then(if (roomy) Modifier.weight(1f) else Modifier.height(EnergyFlowMinHeight))
                        .fillMaxWidth()
                        .padding(16.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.extraLarge),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SummaryCard(gridLabel(reading), abs(reading.gridW), Modifier.weight(1f))
                    SummaryCard(stringResource(R.string.from_solar), reading.solarW, Modifier.weight(1f))
                }
            }
        }
    }
}

/** Space the two summary cards need below the diagram. */
private val SummaryRowHeight = 140.dp

@Composable
fun DetailsScreen(state: MonitorUiState, contentPadding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
    ) {
        ScreenHeader(stringResource(R.string.details_title))
        StatusBanners(state)
        val reading = state.reading
        if (reading == null) {
            WaitingForData(Modifier.weight(1f))
            return@Column
        }
        Column(Modifier.verticalScroll(rememberScrollState())) {
            DetailRow(gridLabel(reading), abs(reading.gridW), R.drawable.electric_tower)
            DetailRow(stringResource(R.string.from_solar), reading.solarW, R.drawable.solar_panel)
            DetailRow(stringResource(R.string.home_load), reading.loadW, R.drawable.electricity)
        }
    }
}

/** Title bar shared by the Details and Settings tabs. */
@Composable
internal fun ScreenHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 16.dp, vertical = 18.dp)
            .semantics { heading() },
    )
}

/** "To grid" or "From grid"; values next to it are shown without a sign. */
@Composable
internal fun gridLabel(reading: InverterReading): String =
    stringResource(if (reading.gridW < 0) R.string.from_grid else R.string.to_grid)

@Composable
private fun StatusBanners(state: MonitorUiState) {
    if (!state.isOnline) {
        Banner(
            text = stringResource(R.string.offline_banner),
            container = MaterialTheme.colorScheme.errorContainer,
            content = MaterialTheme.colorScheme.onErrorContainer,
        )
    }
    if (state.isGridAlertActive) {
        Banner(
            text = stringResource(
                R.string.grid_alert_banner, state.reading!!.gridImportW, state.gridAlert.thresholdW
            ),
            container = MaterialTheme.colorScheme.error,
            content = MaterialTheme.colorScheme.onError,
        )
    }
}

@Composable
private fun Banner(text: String, container: Color, content: Color) {
    Text(
        text = text,
        color = content,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier
            .fillMaxWidth()
            .background(container)
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
    )
}

@Composable
private fun WaitingForData(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            CircularProgressIndicator()
            Text(stringResource(R.string.waiting_for_data), style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun SummaryCard(title: String, watts: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = stringResource(R.string.watts, watts), style = MaterialTheme.typography.headlineSmall)
        }
    }
}

@Composable
private fun DetailRow(title: String, watts: Int, @DrawableRes icon: Int) {
    ListItem(
        leadingContent = {
            Icon(
                imageVector = ImageVector.vectorResource(icon),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(48.dp),
            )
        },
        headlineContent = { Text(text = title, style = MaterialTheme.typography.titleMedium) },
        trailingContent = {
            Text(
                text = stringResource(R.string.watts, watts),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        modifier = Modifier.padding(vertical = 4.dp),
    )
    HorizontalDivider()
}
