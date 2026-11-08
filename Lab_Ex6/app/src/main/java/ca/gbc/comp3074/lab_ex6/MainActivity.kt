
package ca.gbc.comp3074.lab_ex6

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ca.gbc.comp3074.lab_ex6.ui.theme.Lab_Ex6Theme

class MainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager

    private var temperatureSensor: Sensor? = null
    private var gyroscopeSensor: Sensor? = null
    private var rotationSensor: Sensor? = null

    // Sensor readings
    private var temperature by mutableStateOf<Float?>(null)

    private var gyroX by mutableStateOf(0f)
    private var gyroY by mutableStateOf(0f)
    private var gyroZ by mutableStateOf(0f)

    // Relative orientation angles
    private var pitch by mutableStateOf(0f)
    private var roll by mutableStateOf(0f)
    private var azimuth by mutableStateOf(0f)

    // Sensor availability
    private var temperatureAvailable by mutableStateOf(false)
    private var gyroscopeAvailable by mutableStateOf(false)
    private var orientationAvailable by mutableStateOf(false)

    // Calibration status
    private var isCalibrated by mutableStateOf(false)
    private var hasOrientationReading by mutableStateOf(false)

    // Current device rotation matrix
    private val currentRotationMatrix = FloatArray(9)

    // Reference rotation matrix
    private val referenceRotationMatrix = FloatArray(9)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Obtain SensorManager
        sensorManager =
            getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // Discover sensors
        temperatureSensor =
            sensorManager.getDefaultSensor(
                Sensor.TYPE_AMBIENT_TEMPERATURE
            )

        gyroscopeSensor =
            sensorManager.getDefaultSensor(
                Sensor.TYPE_GYROSCOPE
            )

        rotationSensor =
            sensorManager.getDefaultSensor(
                Sensor.TYPE_ROTATION_VECTOR
            )

        temperatureAvailable = temperatureSensor != null
        gyroscopeAvailable = gyroscopeSensor != null
        orientationAvailable = rotationSensor != null

        setContent {
            Lab_Ex6Theme {
                SensorScreen(
                    temperature = temperature,
                    temperatureAvailable = temperatureAvailable,

                    gyroX = gyroX,
                    gyroY = gyroY,
                    gyroZ = gyroZ,
                    gyroscopeAvailable = gyroscopeAvailable,

                    pitch = pitch,
                    roll = roll,
                    azimuth = azimuth,
                    orientationAvailable = orientationAvailable,
                    isCalibrated = isCalibrated,
                    hasOrientationReading = hasOrientationReading,

                    onResetGyroscope = {
                        gyroX = 0f
                        gyroY = 0f
                        gyroZ = 0f
                    },

                    onCalibrateOrientation = {
                        calibrateOrientation()
                    }
                )
            }
        }
    }

    // Register sensors when the activity resumes
    override fun onResume() {
        super.onResume()

        temperatureSensor?.let { sensor ->
            sensorManager.registerListener(
                this,
                sensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }

        gyroscopeSensor?.let { sensor ->
            sensorManager.registerListener(
                this,
                sensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }

        rotationSensor?.let { sensor ->
            sensorManager.registerListener(
                this,
                sensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    // Stop sensor updates when the activity pauses
    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {

            Sensor.TYPE_AMBIENT_TEMPERATURE -> {
                val reading = event.values[0]

                temperature = if (
                    reading.isFinite() &&
                    reading in -100f..100f
                ) {
                    reading
                } else {
                    null
                }
            }

            Sensor.TYPE_GYROSCOPE -> {
                gyroX = event.values[0]
                gyroY = event.values[1]
                gyroZ = event.values[2]
            }

            Sensor.TYPE_ROTATION_VECTOR -> {

                // Convert sensor values to rotation matrix
                SensorManager.getRotationMatrixFromVector(
                    currentRotationMatrix,
                    event.values
                )

                hasOrientationReading = true

                if (!isCalibrated) {
                    // Before calibration, display absolute angles
                    val angles = FloatArray(3)

                    SensorManager.getOrientation(
                        currentRotationMatrix,
                        angles
                    )

                    updateOrientationAngles(angles)

                } else {
                    // Calculate orientation relative to calibration
                    updateRelativeOrientation()
                }
            }
        }
    }

    // Convert orientation radians to degrees
    private fun updateOrientationAngles(angles: FloatArray) {

        azimuth = Math.toDegrees(
            angles[0].toDouble()
        ).toFloat()

        pitch = Math.toDegrees(
            angles[1].toDouble()
        ).toFloat()

        roll = Math.toDegrees(
            angles[2].toDouble()
        ).toFloat()
    }

    // Save the current orientation as reference
    private fun calibrateOrientation() {

        if (!hasOrientationReading) return

        // Copy current rotation matrix into reference
        currentRotationMatrix.copyInto(referenceRotationMatrix)

        isCalibrated = true

        // Calculate orientation relative to new reference
        updateRelativeOrientation()
    }

    // Calculate relative rotation matrix
    private fun updateRelativeOrientation() {

        val relativeMatrix = FloatArray(9)

        // Relative rotation = transpose(reference) * current
        for (row in 0..2) {
            for (col in 0..2) {

                var sum = 0f

                for (k in 0..2) {
                    sum +=
                        referenceRotationMatrix[k * 3 + row] *
                                currentRotationMatrix[k * 3 + col]
                }

                relativeMatrix[row * 3 + col] = sum
            }
        }

        val relativeAngles = FloatArray(3)

        SensorManager.getOrientation(
            relativeMatrix,
            relativeAngles
        )

        updateOrientationAngles(relativeAngles)
    }

    override fun onAccuracyChanged(
        sensor: Sensor?,
        accuracy: Int
    ) {
        // Not required for this lab
    }
}

// Main screen
@Composable
fun SensorScreen(
    temperature: Float?,
    temperatureAvailable: Boolean,

    gyroX: Float,
    gyroY: Float,
    gyroZ: Float,
    gyroscopeAvailable: Boolean,

    pitch: Float,
    roll: Float,
    azimuth: Float,
    orientationAvailable: Boolean,
    isCalibrated: Boolean,
    hasOrientationReading: Boolean,

    onResetGyroscope: () -> Unit,
    onCalibrateOrientation: () -> Unit
) {
    Scaffold { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),

            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = "Android Sensor Monitor",
                style = MaterialTheme.typography.headlineMedium
            )

            TemperatureCard(
                temperature = temperature,
                available = temperatureAvailable
            )

            GyroscopeCard(
                x = gyroX,
                y = gyroY,
                z = gyroZ,
                available = gyroscopeAvailable,
                onReset = onResetGyroscope
            )

            OrientationCard(
                pitch = pitch,
                roll = roll,
                azimuth = azimuth,
                available = orientationAvailable,
                isCalibrated = isCalibrated,
                hasReading = hasOrientationReading,
                onCalibrate = onCalibrateOrientation
            )
        }
    }
}

// Temperature card
@Composable
fun TemperatureCard(
    temperature: Float?,
    available: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = "Ambient Temperature",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (!available) {
                Text("Temperature sensor is not available.")
            } else if (temperature == null) {
                Text("Waiting for a valid temperature reading...")
            } else {
                Text(
                    text = "%.1f °C".format(temperature),
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }
    }
}

// Gyroscope card
@Composable
fun GyroscopeCard(
    x: Float,
    y: Float,
    z: Float,
    available: Boolean,
    onReset: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = "Gyroscope",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (!available) {
                Text("Gyroscope is not available.")
            } else {

                Text("X: %.3f rad/s".format(x))
                Text("Y: %.3f rad/s".format(y))
                Text("Z: %.3f rad/s".format(z))

                Spacer(modifier = Modifier.height(12.dp))

                Button(onClick = onReset) {
                    Text("Reset Gyroscope")
                }
            }
        }
    }
}

// Device orientation with calibration
@Composable
fun OrientationCard(
    pitch: Float,
    roll: Float,
    azimuth: Float,
    available: Boolean,
    isCalibrated: Boolean,
    hasReading: Boolean,
    onCalibrate: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = "Device Orientation",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (!available) {

                Text("Rotation vector sensor is not available.")

            } else {

                Text("Pitch: %.1f°".format(pitch))
                Text("Roll: %.1f°".format(roll))
                Text("Azimuth: %.1f°".format(azimuth))

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isCalibrated) {
                        "Status: Calibrated (relative orientation)"
                    } else if (!hasReading) {
                        "Status: Waiting for orientation reading"
                    } else {
                        "Status: Not calibrated (absolute orientation)"
                    },
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onCalibrate,
                    enabled = hasReading
                ) {
                    Text("Calibrate Orientation")
                }
            }
        }
    }
}
