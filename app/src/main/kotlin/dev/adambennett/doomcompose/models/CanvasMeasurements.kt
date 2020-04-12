package dev.adambennett.doomcompose.models

import kotlin.math.ceil

data class CanvasMeasurements(
    val width: Int,
    val height: Int
)

val CanvasMeasurements.tallerThanWide: Boolean
    get() = width < height

val CanvasMeasurements.pixelSize: Int
    get() {
        val longestLength = if (tallerThanWide) width else height
        return ceil(longestLength.toDouble() / 50).toInt()
    }

val CanvasMeasurements.widthPixel: Int
    get() = when {
        tallerThanWide -> 50
        else -> ceil(width.toDouble() / pixelSize).toInt()
    }

val CanvasMeasurements.heightPixel: Int
    get() = when {
        !tallerThanWide -> 50
        else -> ceil(height.toDouble() / pixelSize).toInt()
    }
