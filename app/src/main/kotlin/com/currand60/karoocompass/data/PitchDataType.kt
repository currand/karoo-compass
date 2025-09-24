package com.currand60.karoocompass.data

import android.content.Context
import androidx.glance.appwidget.ExperimentalGlanceRemoteViewsApi
import com.currand60.karoocompass.managers.ConfigurationManager
import io.hammerhead.karooext.extension.DataTypeImpl
import io.hammerhead.karooext.internal.Emitter
import io.hammerhead.karooext.internal.ViewEmitter
import io.hammerhead.karooext.models.DataPoint
import io.hammerhead.karooext.models.DataType
import io.hammerhead.karooext.models.DataType.Companion.dataTypeId
import io.hammerhead.karooext.models.StreamState
import io.hammerhead.karooext.models.UpdateNumericConfig
import io.hammerhead.karooext.models.ViewConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.tan

fun convertToSlope(pitch: Float): Double {
    val pitchRadians = (pitch * PI / 180).toFloat()
    return (tan(-pitchRadians) * 100).toDouble()
}

@OptIn(ExperimentalGlanceRemoteViewsApi::class)
class PitchDataType (
    extension: String,
    private val compassProvider: CompassProvider,
    private val configManager: ConfigurationManager
) : DataTypeImpl(extension, TYPE_ID) {

    companion object {
        const val TYPE_ID = "grade"
    }

    private val dataScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun startView(context: Context, config: ViewConfig, emitter: ViewEmitter) {
            emitter.onNext(
                UpdateNumericConfig(formatDataTypeId = DataType.Type.ELEVATION_GRADE)
            )
    }

    override fun startStream(emitter: Emitter<StreamState>) {
        val job = dataScope.launch {
            compassProvider.streamCompassData()
            val configFlow = configManager.getConfigFlow()
            val pitchFlow = compassProvider.getPitchFlow()
            combine(pitchFlow, configFlow){ streamState, config ->
                if (streamState is StreamState.Streaming) {
                    val data = streamState.dataPoint.singleValue
                    if (data != null){
                        val grade = convertToSlope(data.toFloat() - config.pitchOffset)
                        StreamState.Streaming(
                            DataPoint(
                                dataTypeId,
                                values = mapOf(DataType.Field.SINGLE to grade),
                            ),
                        )
                    } else {
                        StreamState.NotAvailable
                    }
                } else {
                    StreamState.NotAvailable
                }
            }
            .collect {
                emitter.onNext(it) }
        }
        emitter.setCancellable {
            job.cancel()
            compassProvider.stopStreaming()
        }
    }
}