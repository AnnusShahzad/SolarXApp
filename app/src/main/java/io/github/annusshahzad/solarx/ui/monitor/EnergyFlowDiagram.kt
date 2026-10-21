package io.github.annusshahzad.solarx.ui.monitor

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.annusshahzad.solarx.R
import io.github.annusshahzad.solarx.data.InverterReading
import io.github.annusshahzad.solarx.ui.theme.ExportFlow
import io.github.annusshahzad.solarx.ui.theme.ImportFlow
import io.github.annusshahzad.solarx.ui.theme.SolarFlow
import kotlin.math.abs

/**
 * Solar panel on top, grid on the right and the house at the bottom, with animated dots showing
 * which way power is flowing.
 *
 * @param animate false renders the dots at a fixed position, for screenshots.
 */
@Composable
fun EnergyFlowDiagram(reading: InverterReading, modifier: Modifier = Modifier, animate: Boolean = true) {
    val progress = if (animate) {
        rememberInfiniteTransition(label = "energy flow").animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(durationMillis = 2_000, easing = LinearEasing), RepeatMode.Restart),
            label = "flow progress",
        )
    } else {
        FixedProgress
    }
    val lineColor = MaterialTheme.colorScheme.outline
    val description = stringResource(
        R.string.energy_flow_description, reading.solarW, reading.gridW, reading.loadW
    )

    Box(
        modifier = modifier
            .clearAndSetSemantics { contentDescription = description }
            .padding(24.dp)
            .drawWithCache {
                val radius = 32.dp.toPx()
                val gap = 16.dp.toPx()
                val cx = size.width / 2
                val cy = size.height / 2
                // Lines start and end at the node icons' centres so they never poke out behind them.
                val solarY = NodeIconSize.toPx() / 2
                val gridX = size.width - NodeWidth.toPx() / 2
                val solarToLoad = Path().apply {
                    moveTo(cx - gap, solarY)
                    lineTo(cx - gap, size.height)
                }
                val solarToGrid = Path().apply {
                    moveTo(cx + gap, solarY)
                    lineTo(cx + gap, cy - gap - radius)
                    quadraticBezierTo(cx + gap, cy - gap, cx + gap + radius, cy - gap)
                    lineTo(gridX, cy - gap)
                }
                val gridToLoad = Path().apply {
                    moveTo(gridX, cy + gap)
                    lineTo(cx + gap + radius, cy + gap)
                    quadraticBezierTo(cx + gap, cy + gap, cx + gap, cy + gap + radius)
                    lineTo(cx + gap, size.height)
                }
                val solarMeasure = PathMeasure().apply { setPath(solarToLoad, false) }
                val gridMeasure = PathMeasure().apply {
                    setPath(if (reading.gridW < 0) gridToLoad else solarToGrid, false)
                }
                val stroke = Stroke(width = 3.dp.toPx())
                val dotRadius = 6.dp.toPx()

                onDrawBehind {
                    listOf(solarToLoad, solarToGrid, gridToLoad).forEach { drawPath(it, lineColor, style = stroke) }
                    if (reading.solarW > 0) {
                        drawDot(solarMeasure, progress.value, SolarFlow, dotRadius)
                    }
                    if (reading.gridW != 0) {
                        drawDot(gridMeasure, progress.value, if (reading.gridW < 0) ImportFlow else ExportFlow, dotRadius)
                    }
                }
            },
    ) {
        FlowNode(R.mipmap.solaricon, stringResource(R.string.solar), reading.solarW, Modifier.align(Alignment.TopCenter))
        FlowNode(R.mipmap.gridicon, gridLabel(reading), abs(reading.gridW), Modifier.align(Alignment.CenterEnd))
        FlowNode(R.mipmap.loadicon, stringResource(R.string.home_load), reading.loadW, Modifier.align(Alignment.BottomCenter))
    }
}

private val NodeIconSize = 72.dp
private val NodeWidth = 96.dp

private val FixedProgress: State<Float> = object : State<Float> {
    override val value = 0.4f
}

private fun DrawScope.drawDot(measure: PathMeasure, progress: Float, color: Color, radius: Float) {
    drawCircle(color = color, radius = radius, center = measure.getPosition(measure.length * progress))
}

@Composable
private fun FlowNode(@DrawableRes icon: Int, label: String, watts: Int, modifier: Modifier = Modifier) {
    Column(modifier = modifier.width(NodeWidth), horizontalAlignment = Alignment.CenterHorizontally) {
        Image(painter = painterResource(icon), contentDescription = null, modifier = Modifier.size(NodeIconSize))
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(text = label, style = MaterialTheme.typography.labelSmall)
                Text(
                    text = stringResource(R.string.watts, watts),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
