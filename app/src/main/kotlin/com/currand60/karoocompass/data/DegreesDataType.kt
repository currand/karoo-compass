package com.currand60.karoocompass.data

import androidx.glance.appwidget.ExperimentalGlanceRemoteViewsApi
import io.hammerhead.karooext.extension.DataTypeImpl
import io.hammerhead.karooext.internal.Emitter
import io.hammerhead.karooext.models.DataPoint
import io.hammerhead.karooext.models.DataType
import io.hammerhead.karooext.models.StreamState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(ExperimentalGlanceRemoteViewsApi::class)
class DegreesDataType (
    extension: String,
    private val compassProvider: CompassProvider,
) : DataTypeImpl(extension, TYPE_ID) {

    companion object {
        const val TYPE_ID = "degrees"
    }

    private val dataScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun startStream(emitter: Emitter<StreamState>) {
        val job = dataScope.launch {
            compassProvider.streamCompassData()
            val directionFlow = compassProvider.getCompassFlow()
            directionFlow.map { streamState ->
                if (streamState is StreamState.Streaming) {
                    val data = streamState.dataPoint.singleValue?.toDouble() ?: 0.0
                    Timber.d("Direction: $data")
                    StreamState.Streaming(
                        DataPoint(
                            dataTypeId,
                            values = mapOf(DataType.Field.SINGLE to data),
                        ),
                    )
                } else {
                    StreamState.NotAvailable
                }
            }.collect {
                emitter.onNext(it) }
        }
        emitter.setCancellable {
            job.cancel()
            compassProvider.stopStreaming()
        }
    }
}