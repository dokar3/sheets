package com.dokar.sheets.sample

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.dokar.sheets.BottomSheet
import com.dokar.sheets.BottomSheetLayout
import com.dokar.sheets.BottomSheetState
import com.dokar.sheets.PeekHeight
import com.dokar.sheets.rememberBottomSheetState
import com.dokar.sheets.sample.theme.ComposeBottomSheetTheme
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App()
        }
    }
}

@Composable
fun App() {
    var dark by remember { mutableStateOf(false) }
    ComposeBottomSheetTheme(darkTheme = dark) {
        val window = (LocalContext.current as Activity).window
        val backgroundColor = MaterialTheme.colors.background
        val view = LocalView.current
        LaunchedEffect(dark, backgroundColor) {
            window.statusBarColor = backgroundColor.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !dark
        }

        Surface(color = MaterialTheme.colors.background) {
            Box(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Dark")
                    Spacer(modifier = Modifier.width(4.dp))
                    Switch(
                        checked = dark,
                        onCheckedChange = { dark = it }
                    )
                }

                Sheets(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
private fun Sheets(modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()

    val simpleSheetState = rememberBottomSheetState()
    val listSheetState = rememberBottomSheetState()
    val intentPickerSheetState = rememberBottomSheetState()
    val editSheetState = rememberBottomSheetState()
    val embeddedSheetState = rememberBottomSheetState()

    Column(modifier = modifier) {
        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { scope.launch { simpleSheetState.expand() } }) {
            Text("Simple")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { scope.launch { listSheetState.peek() } }) {
            Text("List")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { scope.launch { intentPickerSheetState.peek() } }) {
            Text("Intent Picker")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { scope.launch { editSheetState.expand() } }) {
            Text("Text fields")
        }

        Button(onClick = { scope.launch { embeddedSheetState.expand() } }) {
            Text("Embedded")
        }
    }

    SimpleBottomSheet(state = simpleSheetState)

    ListBottomSheet(state = listSheetState)

    IntentPickerBottomSheet(state = intentPickerSheetState)

    TextFieldBottomSheet(state = editSheetState)

    EmbeddedBottomSheet(state = embeddedSheetState)
}

@Composable
fun SimpleBottomSheet(
    state: BottomSheetState,
    modifier: Modifier = Modifier
) {
    BottomSheet(
        state = state,
        modifier = modifier,
        skipPeeked = true,
    ) {
        SimpleSheetContent(state)
    }
}

@Composable
private fun ListBottomSheet(
    state: BottomSheetState,
    modifier: Modifier = Modifier
) {
    var sheetShape by remember {
        mutableStateOf(RoundedCornerShape(0f))
    }

    LaunchedEffect(state) {
        snapshotFlow { state.dragProgress }
            .distinctUntilChanged()
            .collect {
                sheetShape = RoundedCornerShape(16.dp * it)
            }
    }

    BottomSheet(
        state = state,
        modifier = modifier
            .fillMaxHeight(0.8f)
            .heightIn(min = 300.dp),
        peekHeight = PeekHeight.fraction(0.6f),
        shape = sheetShape,
    ) {
        ListSheetContent()
    }
}

@Composable
private fun IntentPickerBottomSheet(
    state: BottomSheetState,
    modifier: Modifier = Modifier,
) {
    BottomSheet(
        state = state,
        modifier = modifier,
        peekHeight = PeekHeight.dp(420),
    ) {
        IntentPickerSheetContent(state)
    }
}

@Composable
private fun TextFieldBottomSheet(
    state: BottomSheetState,
    modifier: Modifier = Modifier,
) {
    BottomSheet(
        state = state,
        modifier = modifier,
        skipPeeked = true,
    ) {
        TextFieldSheetContent(state = state)
    }
}

@Composable
private fun EmbeddedBottomSheet(
    state: BottomSheetState,
    modifier: Modifier = Modifier,
) {
    if (state.visible) {
        BottomSheetLayout(
            state = state,
            modifier = modifier,
            skipPeeked = true,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("Sheet content")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    App()
}