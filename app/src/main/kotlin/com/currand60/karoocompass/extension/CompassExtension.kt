package com.currand60.karoocompass.extension

import com.currand60.karoocompass.KarooSystemServiceProvider
import com.currand60.karoocompass.data.CardinalDirectionDataType
import com.currand60.karoocompass.data.CompassProvider
import com.currand60.karoocompass.data.DegreesDataType
import com.currand60.karoocompass.data.PitchDataType
import io.hammerhead.karooext.extension.KarooExtension
import org.koin.android.ext.android.inject
import timber.log.Timber
import kotlin.getValue

class CompassExtension : KarooExtension("karoocompass", "1.0") {

    private val karooSystem: KarooSystemServiceProvider by inject()
    private val compassProvider: CompassProvider by inject()

    override val types by lazy {
        listOf(
            DegreesDataType(karooSystem, extension, compassProvider),
            CardinalDirectionDataType(karooSystem, extension, compassProvider),
            PitchDataType(karooSystem, extension, compassProvider)
        )
    }

    override fun onCreate() {
        super.onCreate()
        karooSystem.karooSystemService.connect { connected ->
            Timber.d("Karoo connected: $connected")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        karooSystem.karooSystemService.disconnect()
        Timber.d("Karoo disconnected")
    }
}
