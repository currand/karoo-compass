package com.currand60.karoocompass.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.currand60.karoocompass.R
import com.currand60.karoocompass.data.ConfigData
import com.currand60.karoocompass.managers.ConfigurationManager
import com.currand60.karoocompass.theme.AppTheme
import org.koin.compose.koinInject

@Composable
fun MainScreen(
    configManager: ConfigurationManager = koinInject()
) {
    var pitchOffsetValue by remember { mutableStateOf(ConfigData.DEFAULT.pitchOffset) }

    val loadedConfig by produceState(initialValue = ConfigData.DEFAULT, key1 = configManager) {
        value = configManager.getConfig()
    }
}

@Preview(
    widthDp = 256,
    heightDp = 426,
)
@Composable
fun DefaultPreview() {
    AppTheme {
        MainScreen()
    }
}
