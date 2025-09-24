package com.currand60.karoocompass.data

import android.content.Context
import androidx.glance.appwidget.ExperimentalGlanceRemoteViewsApi
import androidx.glance.appwidget.GlanceRemoteViews
import com.currand60.karoocompass.KarooSystemServiceProvider
import io.hammerhead.karooext.extension.DataTypeImpl
import io.hammerhead.karooext.internal.Emitter
import io.hammerhead.karooext.internal.ViewEmitter
import io.hammerhead.karooext.models.DataPoint
import io.hammerhead.karooext.models.DataType
import io.hammerhead.karooext.models.StreamState
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
import kotlin.math.PI
import kotlin.math.tan

fun convertToSlope(pitch: Float): Double {
    val pitchRadians = (pitch * PI / 180).toFloat()
    // Calculate tangent and multiply by 100 for percentage
    return (tan(-pitchRadians) * 100).toDouble()
}

@OptIn(ExperimentalGlanceRemoteViewsApi::class)
class PitchDataType (
    private val karooSystem: KarooSystemServiceProvider,
    extension: String,
    private val compassProvider: CompassProvider
) : DataTypeImpl(extension, TYPE_ID) {

    companion object {
        const val TYPE_ID = "grade"
    }

    private val dataScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private fun previewFlow(constantValue: Double? = null): Flow<StreamState> = flow {
        while (true) {
            val value = constantValue ?: ((-12..12).random()).toDouble()
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
    }

    override fun startStream(emitter: Emitter<StreamState>) {
        val job = dataScope.launch {
            compassProvider.streamCompassData()
            val directionFlow = compassProvider.getPitchFlow()
            directionFlow.map { streamState ->
                if (streamState is StreamState.Streaming) {
                    val data = convertToSlope((streamState.dataPoint.singleValue?.toFloat() ?: 0.0).toFloat())
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