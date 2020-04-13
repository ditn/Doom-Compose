package dev.adambennett.doomcompose

import android.view.Choreographer
import androidx.compose.Composable
import androidx.compose.Model
import androidx.compose.remember
import androidx.ui.core.Modifier
import androidx.ui.foundation.Canvas
import androidx.ui.foundation.CanvasScope
import androidx.ui.geometry.Rect
import androidx.ui.graphics.Paint
import androidx.ui.layout.fillMaxSize
import dev.adambennett.doomcompose.models.CanvasMeasurements
import dev.adambennett.doomcompose.models.WindDirection
import dev.adambennett.doomcompose.models.heightPixel
import dev.adambennett.doomcompose.models.pixelSize
import dev.adambennett.doomcompose.models.tallerThanWide
import dev.adambennett.doomcompose.models.widthPixel
import kotlin.math.floor
import kotlin.random.Random

@Model
data class DoomState(var pixels: List<Int> = emptyList())

@Composable
fun DoomCompose(
    state: DoomState = DoomState()
) {
    DoomCanvas(state) { canvas ->
        setupFireView(canvas, state)
    }
}

@Composable
fun DoomCanvas(
    state: DoomState,
    measurements: (CanvasMeasurements) -> Unit
) {
    val paint = remember { Paint() }
    var measured = false

    Canvas(modifier = Modifier.fillMaxSize()) {
        val canvasState = CanvasMeasurements(
            size.width.value.toInt(),
            size.height.value.toInt()
        )

        if (!measured) {
            measured = true
            measurements(canvasState)
        }

        if (state.pixels.isNotEmpty()) {
            renderFire(
                paint,
                state.pixels,
                canvasState.heightPixel,
                canvasState.widthPixel,
                canvasState.pixelSize
            )
        }
    }
}

private fun CanvasScope.renderFire(
    paint: Paint,
    firePixels: List<Int>,
    heightPixels: Int,
    widthPixels: Int,
    pixelSize: Int
) {
    for (column in 0 until widthPixels) {
        for (row in 0 until heightPixels - 1) {
            drawRect(
                rect = Rect(
                    (column * pixelSize).toFloat(),
                    (row * pixelSize).toFloat(),
                    ((column + 1) * pixelSize).toFloat(),
                    ((row + 1) * pixelSize).toFloat()
                ),
                paint = paint.apply {
                    val currentPixelIndex = column + (widthPixels * row)
                    val currentPixel = firePixels[currentPixelIndex]
                    color = fireColors[currentPixel]
                }
            )
        }
    }
}

private fun setupFireView(
    canvas: CanvasMeasurements,
    doomState: DoomState,
    windDirection: WindDirection = WindDirection.Left
) {
    val arraySize = canvas.widthPixel * canvas.heightPixel

    val pixelArray = IntArray(arraySize) { 0 }
        .apply { createFireSource(this, canvas) }

    val callback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            calculateFirePropagation(pixelArray, canvas, windDirection)
            doomState.pixels = pixelArray.toList()

            Choreographer.getInstance().postFrameCallback(this)
        }
    }

    Choreographer.getInstance().postFrameCallback(callback)
}

private fun createFireSource(firePixels: IntArray, canvas: CanvasMeasurements) {
    val overFlowFireIndex = canvas.widthPixel * canvas.heightPixel

    for (column in 0 until canvas.widthPixel) {
        val pixelIndex = (overFlowFireIndex - canvas.widthPixel) + column
        firePixels[pixelIndex] = fireColors.size - 1
    }
}

private fun calculateFirePropagation(
    firePixels: IntArray,
    canvasMeasurements: CanvasMeasurements,
    windDirection: WindDirection
) {
    for (column in 0 until canvasMeasurements.widthPixel) {
        for (row in 1 until canvasMeasurements.heightPixel) {
            val currentPixelIndex = column + (canvasMeasurements.widthPixel * row)
            updateFireIntensityPerPixel(
                currentPixelIndex,
                firePixels,
                canvasMeasurements,
                windDirection
            )
        }
    }
}

private fun updateFireIntensityPerPixel(
    currentPixelIndex: Int,
    firePixels: IntArray,
    measurements: CanvasMeasurements,
    windDirection: WindDirection
) {
    val bellowPixelIndex = currentPixelIndex + measurements.widthPixel
    if (bellowPixelIndex >= measurements.widthPixel * measurements.heightPixel) return

    val offset = if (measurements.tallerThanWide) 2 else 3
    val decay = floor(Random.nextDouble() * offset).toInt()
    val bellowPixelFireIntensity = firePixels[bellowPixelIndex]
    val newFireIntensity = when {
        bellowPixelFireIntensity - decay >= 0 -> bellowPixelFireIntensity - decay
        else -> 0
    }

    val newPosition = when (windDirection) {
        WindDirection.Right -> if (currentPixelIndex - decay >= 0) currentPixelIndex - decay else currentPixelIndex
        WindDirection.Left -> if (currentPixelIndex + decay >= 0) currentPixelIndex + decay else currentPixelIndex
        WindDirection.None -> currentPixelIndex
    }

    firePixels[newPosition] = newFireIntensity
}


