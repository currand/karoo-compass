package com.currand60.karoocompass.data

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import io.hammerhead.karooext.models.DataPoint
import io.hammerhead.karooext.models.DataType
import io.hammerhead.karooext.models.StreamState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber
import kotlin.math.roundToInt

class CompassProvider(
    private val context: Context,
    private val extension: String
) : SensorEventListener {

    private val sensorManager: SensorManager by lazy {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }
    private val accelerometerSensor: Sensor? by lazy {
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }
    private val magneticFieldSensor: Sensor? by lazy {
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    }


    private val gravity: FloatArray = FloatArray(3)
    private val geomagnetic: FloatArray = FloatArray(3)
    private val rotationMatrix: FloatArray = FloatArray(9)
    private val orientationAngles: FloatArray = FloatArray(3)

    private var hasGravityData = false
    private var hasGeomagneticData = false
    private var isStreaming = false
    private var streamCount = 0

    var currentPitch: Float = 0.0f
    var currentHeading: Int = 0

    fun streamCompassData() {
        if (isStreaming) {
            streamCount += 1
            return // Prevent multiple registrations
        }

        // Check if required sensors are available.
        if (accelerometerSensor == null) {
            println("MagneticDirectionStreamer: Accelerometer sensor not available on this device.")
            return
        }
        if (magneticFieldSensor == null) {
            println("MagneticDirectionStreamer: Magnetic field sensor not available on this device.")
            return
        }

        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, magneticFieldSensor, SensorManager.SENSOR_DELAY_NORMAL)
        streamCount += 1

        // Reset flags when starting to ensure a fresh set of data for calculation.
        hasGravityData = false
        hasGeomagneticData = false
    }

    fun stopStreaming() {
        if (isStreaming) {
            streamCount -= 1
            if (streamCount <= 0) {
                isStreaming = false
                sensorManager.unregisterListener(this)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        when (accuracy) {
            SensorManager.SENSOR_STATUS_UNRELIABLE -> println("MagneticDirectionStreamer: Sensor ${sensor?.name} accuracy is unreliable. Direction might be inaccurate.")
            SensorManager.SENSOR_STATUS_ACCURACY_LOW -> println("MagneticDirectionStreamer: Sensor ${sensor?.name} accuracy is low.")
            else -> { /* Accuracy is acceptable */ }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            when (it.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    System.arraycopy(it.values, 0, gravity, 0, gravity.size)
                    hasGravityData = true
                }

                Sensor.TYPE_MAGNETIC_FIELD -> {
                    System.arraycopy(it.values, 0, geomagnetic, 0, geomagnetic.size)
                    hasGeomagneticData = true
                }
            }

            if (hasGravityData && hasGeomagneticData) {
                val success = SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)
                if (success) {
                    SensorManager.getOrientation(rotationMatrix, orientationAngles)
                    var azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                    Timber.d("Raw azimuth: $azimuth")
                    if (azimuth < 0) {
                        azimuth += 360f // Normalize negative angles (e.g., -90 becomes 270)
                    }

                    val pitch = Math.toDegrees(orientationAngles[1].toDouble()).toFloat()

                    val roundedAzimuth = azimuth.roundToInt()


                    currentHeading = roundedAzimuth
                    currentPitch = pitch
                }
            }
        }
    }
    fun getCompassFlow(): Flow<StreamState> = flow {
        while(true) {
            if (hasGravityData && hasGeomagneticData) {
                emit(
                    StreamState.Streaming(
                        DataPoint(
                            DataType.dataTypeId(extension, DegreesDataType.TYPE_ID),
                            mapOf(DataType.Field.SINGLE to currentHeading.toDouble()),
                            extension
                        )
                    )
                )
            } else {
                emit(StreamState.NotAvailable)
            }
                delay(1000)
            }
        }.flowOn(Dispatchers.IO)

    fun getPitchFlow(): Flow<StreamState> = flow {
        while(true) {
            if (hasGravityData && hasGeomagneticData) {
                emit(
                    StreamState.Streaming(
                        DataPoint(
                            DataType.dataTypeId(extension, DegreesDataType.TYPE_ID),
                            mapOf(DataType.Field.SINGLE to currentPitch.toDouble()),
                            extension
                        )
                    )
                )
            } else {
                emit(StreamState.NotAvailable)
            }
            delay(1000)
        }
    }.flowOn(Dispatchers.IO)

    fun getPitchValueFlow(): Flow<Float> = flow {
        while(true) {
            if (hasGravityData && hasGeomagneticData) {
                emit(currentPitch)
            }
            delay(1000)
        }
    }.flowOn(Dispatchers.IO)
}