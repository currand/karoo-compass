package com.currand60.karoocompass.data

data class ConfigData(
    val pitchOffset: Float,
) {
    companion object {
        /**
         * Provides default configuration values.
         * These are used when no settings are found or when resetting to defaults.
         */
        val DEFAULT = ConfigData(
            pitchOffset = 0.0f,
        )
    }
}