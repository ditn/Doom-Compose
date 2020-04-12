package dev.adambennett.doomcompose.models

sealed class WindDirection {
    object Right : WindDirection()
    object Left : WindDirection()
    object None : WindDirection()
}
