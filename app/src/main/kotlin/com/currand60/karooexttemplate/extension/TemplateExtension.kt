package com.currand60.karooexttemplate.extension

import com.currand60.karooexttemplate.KarooSystemServiceProvider
import io.hammerhead.karooext.extension.KarooExtension
import org.koin.android.ext.android.inject
import timber.log.Timber
import kotlin.getValue

class TemplateExtension : KarooExtension("template-id", "1.0") {

    private val karooSystem: KarooSystemServiceProvider by inject()

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
