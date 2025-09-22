package com.currand60.karooexttemplate.data

data class ConfigData(
    val exampleValue: Boolean,
) {
    companion object {
        /**
         * Provides default configuration values.
         * These are used when no settings are found or when resetting to defaults.
         */
        val DEFAULT = ConfigData(
            exampleValue = true,
        )
    }

    fun validate(): Boolean {
        return exampleValue == true
    }
}