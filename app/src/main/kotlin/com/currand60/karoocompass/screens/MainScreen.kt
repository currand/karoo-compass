package com.currand60.karoocompass.screens

import com.currand60.karoocompass.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.currand60.karoocompass.data.CompassProvider
import com.currand60.karoocompass.data.ConfigData
import com.currand60.karoocompass.managers.ConfigurationManager
import com.currand60.karoocompass.theme.AppTheme
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun MainScreen(
    configManager: ConfigurationManager = koinInject(),
    compassProvider: CompassProvider = koinInject(),
    context: android.content.Context = LocalContext.current
) {
    val coroutineScope = rememberCoroutineScope()
    var pitchOffsetValue by remember { mutableFloatStateOf(ConfigData.DEFAULT.pitchOffset) }
    var currentConfig by remember { mutableStateOf(ConfigData.DEFAULT) }

    DisposableEffect(Unit) {
        compassProvider.streamCompassData()
        onDispose {
            compassProvider.stopStreaming()
        }
    }

    val currentPitch by compassProvider.getPitchValueFlow().collectAsState(initial = 0f)
    val loadedConfig by configManager.getConfigFlow().collectAsState(initial = ConfigData.DEFAULT)


    LaunchedEffect(loadedConfig, currentConfig) {
        currentConfig = loadedConfig
        pitchOffsetValue = currentConfig.pitchOffset
    }

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Once your computer is mounted to your bike, place the bike on level" +
                " ground and tap the save button to correct for handlebar and mount angles",
            color = MaterialTheme.colorScheme.onBackground,
            )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = pitchOffsetValue.toString(),
            onValueChange = { /* Do nothing */ },
            readOnly = true,
            label = { Text("Saved Offset Value") },
            suffix = { Text("°") },
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = currentPitch.toString(),
            onValueChange = { /* Do nothing */ },
            readOnly = true,
            label = { Text("Current Device Pitch") },
            suffix = { Text("°") },
        )
        Spacer(modifier = Modifier.height(8.dp))
        FilledTonalButton(
            onClick = {
                coroutineScope.launch {
                    val currentConfig = loadedConfig.copy(pitchOffset = currentPitch)
                    configManager.saveConfig(currentConfig)
                }

            },
            enabled = true,

        ){
            Icon(Icons.Default.Check, contentDescription = "Save")
            Spacer(modifier = Modifier.width(5.dp))
            Text(context.getString(R.string.save))
        }
    }
}

@Preview(
    widthDp = 256,
    heightDp = 426,
)
@Composable
fun DefaultPreview() {
    val context = LocalContext.current
    AppTheme {
        MainScreen(
            configManager = ConfigurationManager(context),
            compassProvider = CompassProvider(context, "karoocompass")
        )
    }
}
