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
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.getSystemService
import com.plcoding.coroutinesmasterclass.sections.homework.LeaderboardListener
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.random.Random

class Leaderboard {
    private val scores = mutableMapOf<String, Int>()
    private var listener: LeaderboardListener? = null

    suspend fun updateScore(playerName: String, score: Int) {
        if (scores.containsKey(playerName)) {
            println("This player already exists in the leaderboard")
            return
        }
        scores[playerName] = score
        listener?.onLeaderboardUpdated(computeTopThreeScores())
    }

    private suspend fun computeTopThreeScores(): String {
        return withContext(Dispatchers.Default) {
            scores.entries
                .sortedByDescending { it.value }
                .take(3)
                .withIndex()
                .joinToString("\n") { (index, entry) ->
                    "#${index + 1} is ${entry.key} with ${entry.value} points"
                }
        }
    }

    fun addListener(listener: LeaderboardListener) {
        this.listener = listener
    }

    fun removeListener() {
        this.listener = null
    }
}

class MainActivity : ComponentActivity() {
    @OptIn(DelicateCoroutinesApi::class)
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val listener = LeaderboardListener { topScores ->
            println("New Top Scores:")
            println(topScores + "\n\n")
        }

        val mutex = Mutex()

        val leaderboard = Leaderboard()
        leaderboard.addListener(listener)
        GlobalScope.launch {
            (1..5_000).map { index ->
                launch {
                    val playerName = "Player $index"
                    val playerScore = Random.nextInt(1, 10_0000)
                    mutex.withLock {
                        leaderboard.updateScore(playerName, playerScore)
                    }
                }
            }.joinAll()
            println("Completed!")
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
}