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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.annusshahzad.solarx.R
import io.github.annusshahzad.solarx.data.InverterReading
import io.github.annusshahzad.solarx.ui.theme.ExportFlow
import io.github.annusshahzad.solarx.ui.theme.ImportFlow
import io.github.annusshahzad.solarx.ui.theme.SolarFlow
import kotlin.math.abs

/**
 * Solar panel on top, grid on the right and the house at the bottom, each tile showing its own
 * reading, with animated dots on the wires showing which way power is flowing.
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
    BoxWithConstraints(
        modifier = modifier
            .clearAndSetSemantics { contentDescription = description }
            .padding(DiagramPadding),
    ) {
        // Tiles grow with the system font size, but never beyond a third of the space, so they can't
        // overlap. Their text is sized from the tile, so it always fits inside.
        val tileSize = minOf(
            TileSize * LocalDensity.current.fontScale.coerceIn(1f, MaxTileScale),
            (maxWidth - TileSpacing) / 3,
            (maxHeight - TileSpacing) / 3,
        )

        Layout(
            content = {
                FlowTile(R.mipmap.solaricon, stringResource(R.string.solar), reading.solarW, tileSize)
                FlowTile(R.mipmap.gridicon, gridLabel(reading), abs(reading.gridW), tileSize)
                FlowTile(R.mipmap.loadicon, stringResource(R.string.home_load), reading.loadW, tileSize)
            },
            modifier = Modifier
                .fillMaxSize()
                .drawWithCache {
                    val geometry = FlowGeometry(size.width, size.height, tileHalf = tileSize.toPx() / 2)
                    // Wires run from tile edge to tile edge, tucked just under the tiles' rounded rims,
                    // so the dots stay visible for their whole journey.
                    val rim = geometry.tileHalf - TileInset.toPx()
                    val radius = 32.dp.toPx()
                    val gap = 16.dp.toPx()
                    val (cx, cy) = geometry.center
                    val solarBottom = geometry.solar.y + rim
                    val loadTop = geometry.load.y - rim
                    val gridLeft = geometry.grid.x - rim
                    val solarToLoad = Path().apply {
                        moveTo(cx - gap, solarBottom)
                        lineTo(cx - gap, loadTop)
                    }
                    val solarToGrid = Path().apply {
                        moveTo(cx + gap, solarBottom)
                        lineTo(cx + gap, cy - gap - radius)
                        quadraticBezierTo(cx + gap, cy - gap, cx + gap + radius, cy - gap)
                        lineTo(gridLeft, cy - gap)
                    }
                    val gridToLoad = Path().apply {
                        moveTo(gridLeft, cy + gap)
                        lineTo(cx + gap + radius, cy + gap)
                        quadraticBezierTo(cx + gap, cy + gap, cx + gap, cy + gap + radius)
                        lineTo(cx + gap, loadTop)
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
        ) { measurables, constraints ->
            val geometry = FlowGeometry(
                width = constraints.maxWidth.toFloat(),
                height = constraints.maxHeight.toFloat(),
                tileHalf = tileSize.toPx() / 2,
            )
            val tiles = measurables.map { it.measure(Constraints()) }
            layout(constraints.maxWidth, constraints.maxHeight) {
                listOf(geometry.solar, geometry.grid, geometry.load).forEachIndexed { index, centre ->
                    val tile = tiles[index]
                    tile.place(centre.x.toInt() - tile.width / 2, centre.y.toInt() - tile.height / 2)
                }
            }
        }
    }
}

/** Tile centres inside the diagram: solar at the top, grid on the right, home at the bottom. */
private class FlowGeometry(width: Float, height: Float, val tileHalf: Float) {
    val center = Offset(width / 2, height / 2)
    val solar = Offset(width / 2, tileHalf)
    val grid = Offset(width - tileHalf, height / 2)
    val load = Offset(width / 2, height - tileHalf)
}

private val TileSize = 104.dp
private val DiagramPadding = 16.dp
private const val MaxTileScale = 1.4f
private val TileSpacing = 16.dp

/** Distance from the bitmap edge to the tile's visible rim. */
private val TileInset = 10.dp

/**
 * Smallest height, including the 16 dp margin and padding around it, at which the three
 * tiles fit without overlapping, even at the largest tile scale.
 */
val EnergyFlowMinHeight = 380.dp

private val FixedProgress: State<Float> = object : State<Float> {
    override val value = 0.4f
}

private fun DrawScope.drawDot(measure: PathMeasure, progress: Float, color: Color, radius: Float) {
    drawCircle(color = color, radius = radius, center = measure.getPosition(measure.length * progress))
}

/** One of the icon tiles, with its name and reading written in the free band below the pictogram. */
@Composable
private fun FlowTile(@DrawableRes icon: Int, label: String, watts: Int, size: Dp) {
    // Proportional to the tile (and so independent of the font scale, which already sized the tile).
    val (labelSize, valueSize) = with(LocalDensity.current) { (size * 0.096f).toSp() to (size * 0.144f).toSp() }
    Box(Modifier.size(size)) {
        Image(painter = painterResource(icon), contentDescription = null, modifier = Modifier.fillMaxSize())
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = size * 0.12f, end = size * 0.12f, bottom = size * 0.13f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = label,
                color = TileText.copy(alpha = 0.8f),
                fontSize = labelSize,
                lineHeight = labelSize * 1.2f,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.watts, watts),
                color = TileText,
                fontSize = valueSize,
                lineHeight = valueSize * 1.2f,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
        }
    }
}

/** The tile bitmaps are dark in both themes, so their text is always light. */
private val TileText = Color.White
