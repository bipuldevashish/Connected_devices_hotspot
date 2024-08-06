package com.example.jetpackcomposedemo

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.jetpackcomposedemo.ui.theme.JetPackComposeDemoTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.FileReader

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JetPackComposeDemoTheme {
                HomeScreen()
            }
        }
    }
}

private fun getConnectedDevices(): List<String> {
    val connectedDevices = mutableListOf<String>()
    try {
        val bufferedReader = BufferedReader(FileReader("/proc/net/arp"))
        var line: String?

        bufferedReader.readLine() // Skip the header line
        while (bufferedReader.readLine().also { line = it } != null) {
            val split = line?.split("\\s+".toRegex())?.toTypedArray()
            if (split != null && split.size >= 4) {
                val ipAddress = split[0]
                val macAddress = split[3]
                if (macAddress != "00:00:00:00:00:00") {
                    connectedDevices.add("IP Address: $ipAddress\nMAC Address: $macAddress")
                    Log.d("TAG", "getConnectedDevices: $ipAddress  $macAddress")
                }
            }
        }
        bufferedReader.close()

    } catch (e: Exception) {
        e.printStackTrace()
    }
    return connectedDevices
}

@Composable
fun HomeScreen() {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.search_devices_button))
    var buttonText by remember { mutableStateOf("Search Devices") }
    var isPlaying by remember { mutableStateOf(false) }
    var connectedDevices by remember { mutableStateOf(listOf<String>()) }
    val coroutineScope = rememberCoroutineScope()
    var job: Job? by remember { mutableStateOf(null) }

    val animationState by animateLottieCompositionAsState(
        composition = composition,
        isPlaying = isPlaying,
        iterations = if (isPlaying) LottieConstants.IterateForever else 1
    )

    if (connectedDevices.isNotEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Connected Devices",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            LazyColumn {
                items(connectedDevices) { device ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.baseline_smartphone_24),
                            contentDescription = "deviceName",
                            modifier = Modifier
                                .size(48.dp)
                                .padding(end = 16.dp)
                        )
                        Text(text = device, style = MaterialTheme.typography.bodyLarge)
                    }
                    HorizontalDivider()
                }
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            LottieAnimation(
                modifier = Modifier.size(400.dp),
                composition = composition,
                progress = { if (isPlaying) animationState else 0f }
            )

            androidx.compose.material3.Button(onClick = {
                if (!isPlaying) {
                    buttonText = "Stop Searching"
                    isPlaying = true
                    job = coroutineScope.launch {
                        delay(2000)
                        connectedDevices = getConnectedDevices()
                    }

                } else {
                    buttonText = "Search Devices"
                    isPlaying = false
                    if (job != null && job!!.isActive){
                        job?.cancel()
                    }
                }
            }) {
                Text(text = buttonText)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainPreview() {
    JetPackComposeDemoTheme {
        HomeScreen()
    }
}