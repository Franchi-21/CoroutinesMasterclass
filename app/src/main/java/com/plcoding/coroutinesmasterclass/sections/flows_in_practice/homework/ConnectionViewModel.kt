package com.plcoding.coroutinesmasterclass.sections.flows_in_practice.homework

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plcoding.coroutinesmasterclass.sections.flow_fundamentals.LocationObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.zip

data class Location(
    val latitude: Double,
    val longitude: Double
)

data class ConnectionStatusWithLocation(
    val status: ConnectionStatus,
    val location: Location
)

class ConnectionViewModel(
    connectionHelper: ConnectionHelper,
    locationObserver: LocationObserver,
) : ViewModel() {
    val connectionObserver = connectionHelper
        .userHasConnection()
        .distinctUntilChangedBy { it.wasConnected }
        .zip(locationObserver.observeLocation(1_000)) { status, location ->
            ConnectionStatusWithLocation(
                status = status,
                location = Location(
                    latitude = location.latitude,
                    longitude = location.longitude
                )
            )
        }
        .runningFold(initial = listOf<ConnectionStatusWithLocation>()) { list, value ->
            list + value
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )
}