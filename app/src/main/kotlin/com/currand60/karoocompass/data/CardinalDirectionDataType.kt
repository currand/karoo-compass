package com.currand60.karoocompass.data

import android.content.Context
import androidx.compose.ui.unit.DpSize
import androidx.glance.appwidget.ExperimentalGlanceRemoteViewsApi
import androidx.glance.appwidget.GlanceRemoteViews
import com.currand60.karoocompass.screens.CardinalView
import io.hammerhead.karooext.extension.DataTypeImpl
import io.hammerhead.karooext.internal.Emitter
import io.hammerhead.karooext.internal.ViewEmitter
import io.hammerhead.karooext.models.DataPoint
import io.hammerhead.karooext.models.DataType
import io.hammerhead.karooext.models.StreamState
import io.hammerhead.karooext.models.UpdateGraphicConfig
import io.hammerhead.karooext.models.ViewConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber


@OptIn(ExperimentalGlanceRemoteViewsApi::class)
class CardinalDirectionDataType (
    extension: String,
    private val compassProvider: CompassProvider
) : DataTypeImpl(extension, TYPE_ID) {

    companion object {
        const val TYPE_ID = "cardinaldirection"
    }

    private val dataScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val glance = GlanceRemoteViews()

    private fun previewFlow(constantValue: Double? = null): Flow<StreamState> = flow {
        while (true) {
            val value = constantValue ?: ((0..360).random()).toDouble()
            emit(StreamState.Streaming(
                DataPoint(
                    dataTypeId,
                    mapOf(DataType.Field.SPEED to value),
                    extension
                )
            ))
            delay(1000)
        }
    }.flowOn(Dispatchers.IO)

    override fun startView(context: Context, config: ViewConfig, emitter: ViewEmitter) {
        val configJob = dataScope.launch {
            emitter.onNext(
                UpdateGraphicConfig(showHeader = false)
            )
        }
        val displayJob = dataScope.launch {
            val currentHeadingFlow =
                if (!config.preview) compassProvider.getCompassFlow() else previewFlow()
            currentHeadingFlow.collect {
                if (it is StreamState.Streaming) {
                    val data = it.dataPoint.singleValue?.toDouble()
                    if (data != null) {
                        val result = glance.compose(context, DpSize.Unspecified) {
                            CardinalView(
                                context,
                                data,
                                config
                            )
                        }
                        emitter.updateView(result.remoteViews)
                    }
                }
            }
        }
        emitter.setCancellable {
            configJob.cancel()
            displayJob.cancel()
        }
    }

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