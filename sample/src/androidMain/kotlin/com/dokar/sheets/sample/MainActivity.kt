package com.dokar.sheets.sample

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            App()
        }
    }
}

@Composable
fun App() {
    var dark by remember { mutableStateOf(false) }

    val window = LocalActivity.current!!.window
    val view = LocalView.current
    val backgroundColor = if (dark) Color(0xff121212) else Color.White
    LaunchedEffect(dark, backgroundColor) {
        window.statusBarColor = backgroundColor.toArgb()
        WindowCompat.getInsetsController(
            window,
            view
        ).isAppearanceLightStatusBars = !dark
    }

    SampleScreen(
        isDarkTheme = dark,
        onUpdateDarkTheme = { dark = it },
    )
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    App()
}