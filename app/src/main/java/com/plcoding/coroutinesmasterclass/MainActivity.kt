package com.plcoding.coroutinesmasterclass

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlin.system.measureTimeMillis

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AnimalHomeworkHard()
        }
    }
}

@OptIn(DelicateCoroutinesApi::class)
fun getAnimalSound(
    sound: String,
    delayMillis: Long,
): Job {
    return GlobalScope.launch {
        delay(delayMillis)
        println(sound)
    }
}

@Composable
fun AnimalHomeworkHard() {
    var currentJob by remember { mutableStateOf<Job?>(null) }
    var currentBirdName by remember { mutableStateOf("") }
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = currentBirdName,
            fontSize = 20.sp
        )
        Button(
            modifier = Modifier.fillMaxWidth(0.5f),
            onClick = {
                currentJob?.cancel()
                currentJob = getAnimalSound(sound = "Coo", delayMillis = 1_000)
                currentBirdName = "Bird 1"
            }
        ) {
            Text("Bird 1")
        }
        Button(
            modifier = Modifier.fillMaxWidth(0.5f),
            onClick = {
                currentJob?.cancel()
                currentJob = getAnimalSound(sound = "Caw", delayMillis = 2_000)
                currentBirdName = "Bird 2"
            }
        ) {
            Text("Bird 2")
        }
        Button(
            modifier = Modifier.fillMaxWidth(0.5f),
            onClick = {
                currentJob?.cancel()
                currentJob = getAnimalSound(sound = "Chirp", delayMillis = 3_000)
                currentBirdName = "Bird 3"
            }
        ) {
            Text("Bird 3")
        }
    }
}

@OptIn(DelicateCoroutinesApi::class)
fun animalHomeworkEasy() {
    // First bird
    GlobalScope.launch(CoroutineName("Tweety")) {
        repeat(4) {
            println("Coo")
            println(coroutineContext[CoroutineName]?.name)
            delay(1_000)
        }
    }
    // Second bird
    GlobalScope.launch(CoroutineName("Zazu")) {
        repeat(4) {
            println("Caw")
            println(coroutineContext[CoroutineName]?.name)
            delay(2_000)
        }
    }
    // Last bird
    GlobalScope.launch(CoroutineName("Woodstock")) {
        repeat(4) {
            println("Chirp")
            println(coroutineContext[CoroutineName]?.name)
            delay(3_000)
        }
    }
}

@OptIn(DelicateCoroutinesApi::class)
fun animalHomeworkMedium() {
    GlobalScope.launch {
        // First bird
        val coo = GlobalScope.launch {
            while (true) {
                println("Coo")
                delay(1_000)
            }
        }
        // Second bird
        val caw = GlobalScope.launch {
            while (true) {
                println("Caw")
                delay(2_000)
            }
        }
        // Last bird
        val chirp = GlobalScope.launch {
            while (true) {
                println("Chirp")
                delay(3_000)
            }
        }
        delay(10_000)
        coo.cancel()
        caw.cancel()
        chirp.cancel()
    }
}
