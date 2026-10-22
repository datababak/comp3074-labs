
package ca.gbc.comp3074.lab_ex5

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper

import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

import androidx.core.content.ContextCompat

import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

import ca.gbc.comp3074.lab_ex5.ui.theme.Lab_Ex5Theme
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            Lab_Ex5Theme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->

                    LocationComponent(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun LocationComponent(modifier: Modifier = Modifier) {

    // Get the Android context
    val context = LocalContext.current

    // Initialize Google's location provider
    val fusedClient = remember(context) {
        LocationServices.getFusedLocationProviderClient(context)
    }

    // Store the current location and status
    val location = remember {
        mutableStateOf<Location?>(null)
    }

    val status = remember {
        mutableStateOf("Location not available")
    }

    // Remember the same callback across recompositions
    val locationCallback = remember {
        mutableStateOf<LocationCallback?>(null)
    }

    // Check whether fine or coarse location permission exists
    fun hasPermission(): Boolean {

        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocation || coarseLocation
    }

    // Get the current location once
    @SuppressLint("MissingPermission")
    fun fetchLocation() {

        if (!hasPermission()) {
            status.value = "Location permission not granted"
            return
        }

        status.value = "Fetching location..."

        fusedClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token
        )
            .addOnSuccessListener { result ->

                if (result != null) {

                    location.value = result

                    status.value =
                        "Latitude: %.5f\nLongitude: %.5f".format(
                            result.latitude,
                            result.longitude
                        )

                } else {
                    status.value = "Location not found"
                }
            }
            .addOnFailureListener { error ->
                status.value =
                    error.localizedMessage ?: "Unknown error"
            }
    }

    // Start receiving continuous location updates
    @SuppressLint("MissingPermission")
    fun startTracking() {

        if (!hasPermission()) {
            status.value = "Location permission not granted"
            return
        }

        if (locationCallback.value != null) {
            status.value = "Already tracking"
            return
        }

        // Request updates approximately every 5 seconds
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000L
        )
            .setMinUpdateIntervalMillis(2000L)
            .build()

        // Create a callback to receive location updates
        val callback = object : LocationCallback() {

            override fun onLocationResult(result: LocationResult) {

                result.lastLocation?.let { currentLocation ->

                    location.value = currentLocation

                    status.value =
                        "Tracking...\nLatitude: %.5f\nLongitude: %.5f".format(
                            currentLocation.latitude,
                            currentLocation.longitude
                        )
                }
            }
        }

        // Save the callback so Stop Tracking can remove it
        locationCallback.value = callback

        status.value = "Starting tracking..."

        fusedClient.requestLocationUpdates(
            request,
            callback,
            Looper.getMainLooper()
        )
            .addOnFailureListener { error ->
                locationCallback.value = null
                status.value =
                    error.localizedMessage ?: "Tracking failed"
            }
    }

    // Stop receiving location updates
    fun stopTracking() {

        val callback = locationCallback.value

        if (callback != null) {

            fusedClient.removeLocationUpdates(callback)

            locationCallback.value = null
            status.value = "Tracking stopped"

        } else {
            status.value = "Tracking is not active"
        }
    }

    // Remove the listener when the composable leaves the screen
    DisposableEffect(fusedClient) {
        onDispose {
            locationCallback.value?.let { callback ->
                fusedClient.removeLocationUpdates(callback)
            }
        }
    }

    // Request runtime permissions
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->

        if (results.values.any { it }) {
            fetchLocation()
        } else {
            status.value = "Location permission denied"
        }
    }

    // User interface

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // Location status and Get Location button - centered
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = status.value,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Get Location button
            Button(
                onClick = {
                    if (!hasPermission()) {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    } else {
                        fetchLocation()
                    }
                }
            ) {
                Text(
                    text = "Get Location",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        }

        // Start Tracking button - 70% down the screen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.70f),
            contentAlignment = Alignment.BottomCenter
        ) {
            Button(
                onClick = {
                    if (hasPermission()) {
                        startTracking()
                    } else {
                        status.value =
                            "Grant location permission using Get Location first"
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Green,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Start Tracking",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        }

        // Stop Tracking button - 80% down the screen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.80f),
            contentAlignment = Alignment.BottomCenter
        ) {
            Button(
                onClick = {
                    stopTracking()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Stop Tracking",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        }
    }
}