package com.plcoding.coroutinesmasterclass

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.getSystemService
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.plcoding.coroutinesmasterclass.sections.flow_fundamentals.LocationObserver
import com.plcoding.coroutinesmasterclass.sections.flows_in_practice.homework.ConnectionHelper
import com.plcoding.coroutinesmasterclass.sections.flows_in_practice.homework.ConnectionStatus
import com.plcoding.coroutinesmasterclass.sections.flows_in_practice.homework.ConnectionStatusWithLocation
import com.plcoding.coroutinesmasterclass.sections.flows_in_practice.homework.ConnectionViewModel
import com.plcoding.coroutinesmasterclass.ui.theme.CoroutinesMasterclassTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            0
        )
        setContent {
            CoroutinesMasterclassTheme {
                val vm = ConnectionViewModel(
                    connectionHelper = ConnectionHelper(this),
                    locationObserver = LocationObserver(this)
                )
                val snackbarHostState = remember { SnackbarHostState() }
                val connectionStatus by vm.connectionObserver.collectAsStateWithLifecycle()

                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    SnackbarScreen(
                        snackbarHostState,
                        connectionStatus,
                        innerPadding,
                    )
                }
            }
        }
    }

    @Composable
    private fun SnackbarScreen(
        snackbarHostState: SnackbarHostState,
        connectionStatus: List<ConnectionStatusWithLocation>,
        innerPadding: PaddingValues
    ) {
        LaunchedEffect(connectionStatus) {
            val last = connectionStatus.lastOrNull()
            println("Last connection was: $last")
            last?.let {
                if (!it.status.wasConnected) {
                    snackbarHostState.showSnackbar(
                        message = "You're not connected to the internet",
                        duration = SnackbarDuration.Indefinite
                    )
                } else {
                    snackbarHostState.currentSnackbarData?.dismiss()
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            items(items = connectionStatus) {
                Text(text = "${it.status.connectionDate} - ${it.status.wasConnected}")
                Text(text = "${it.location.latitude}, ${it.location.longitude}")
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.R)
suspend fun Context.getLocation(): Location {
    return suspendCancellableCoroutine { continuation ->
        val locationManager = getSystemService<LocationManager>()!!

        val hasFineLocationPermission = ActivityCompat.checkSelfPermission(
            this@getLocation,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarseLocationPermission = ActivityCompat.checkSelfPermission(
            this@getLocation,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val signal = CancellationSignal()
        if (hasFineLocationPermission && hasCoarseLocationPermission) {
            locationManager.getCurrentLocation(
                LocationManager.NETWORK_PROVIDER,
                signal,
                mainExecutor
            ) { location ->
                println("Got location: $location")
                continuation.resume(location)
            }
        } else {
            continuation.resumeWithException(
                RuntimeException("Missing location permission")
            )
        }

        continuation.invokeOnCancellation {
            signal.cancel()
        }
    }
}