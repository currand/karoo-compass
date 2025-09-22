package com.currand60.karooexttemplate.data

import android.content.Context
import androidx.glance.appwidget.ExperimentalGlanceRemoteViewsApi
import androidx.glance.appwidget.GlanceRemoteViews
import com.currand60.karooexttemplate.KarooSystemServiceProvider
import io.hammerhead.karooext.extension.DataTypeImpl
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

@OptIn(ExperimentalGlanceRemoteViewsApi::class)
class TemplateDataType (
    private val karooSystem: KarooSystemServiceProvider,
    extension: String
) : DataTypeImpl(extension, TYPE_ID) {

    companion object {
        const val TYPE_ID = "template-datatype"
    }

    private val glance = GlanceRemoteViews()
    private val dataScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private fun previewFlow(constantValue: Double? = null): Flow<StreamState> = flow {
        while (true) {
            val value = constantValue ?: (((0..17).random() * 10).toDouble() / 10.0)
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

}