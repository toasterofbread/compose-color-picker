package com.godaddy.app

sealed class Route(val link: String) {
    data object Picker : Route("picker")
    data object ClassicColorPicker : Route("classic")
    data object HarmonyColorPicker : Route("harmony")
}
