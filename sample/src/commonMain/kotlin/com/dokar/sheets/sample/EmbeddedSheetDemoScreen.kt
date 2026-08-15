package com.dokar.sheets.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dokar.sheets.BottomSheetState
import com.dokar.sheets.BottomSheetValue
import com.dokar.sheets.PeekHeight
import com.dokar.sheets.SheetBehaviors
import com.dokar.sheets.m3.BottomSheetLayout
import com.dokar.sheets.rememberBottomSheetState
import kotlinx.coroutines.launch

@Composable
internal fun EmbeddedSheetDemoScreen(
    isDarkTheme: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = if (isDarkTheme) Color(0xff121212) else Color.White

    MaterialTheme(
        colorScheme = if (isDarkTheme) darkColorScheme() else lightColorScheme(),
    ) {
        Surface(
            color = backgroundColor,
            modifier = modifier.fillMaxSize(),
        ) {
            val scope = rememberCoroutineScope()
            val state = rememberBottomSheetState(initialValue = BottomSheetValue.Peeked)

            var allowOutsideInteraction by rememberSaveable { mutableStateOf(true) }
            var skipPeeked by rememberSaveable { mutableStateOf(false) }
            var backgroundClickCount by rememberSaveable { mutableIntStateOf(0) }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.systemBars),
            ) {
                // Background interactive content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = onBack) {
                            Text("←", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Embedded sheet",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Settings",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text("allowOutsideInteraction")
                                Switch(
                                    checked = allowOutsideInteraction,
                                    onCheckedChange = { allowOutsideInteraction = it },
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text("skipPeeked")
                                Switch(
                                    checked = skipPeeked,
                                    onCheckedChange = { skipPeeked = it },
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Background interaction",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Taps registered: $backgroundClickCount",
                                fontSize = 14.sp,
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = { backgroundClickCount++ },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text("Tap me while sheet is peeked ($backgroundClickCount)")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedButton(
                            onClick = { scope.launch { state.peek() } },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Peek")
                        }

                        OutlinedButton(
                            onClick = { scope.launch { state.expand() } },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Expand")
                        }

                        OutlinedButton(
                            onClick = { scope.launch { state.collapse() } },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Collapse")
                        }
                    }
                }

                // Embedded BottomSheetLayout
                BottomSheetLayout(
                    state = state,
                    skipPeeked = skipPeeked,
                    peekHeight = PeekHeight.fraction(0.3f),
                    behaviors = SheetBehaviors(
                        allowOutsideInteraction = allowOutsideInteraction,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    EmbeddedSheetContent(state = state)
                }
            }
        }
    }
}

@Composable
private fun EmbeddedSheetContent(
    state: BottomSheetState,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .fillMaxWidth(),
    ) {
        Text(
            text = "Sheet Content",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = LoremIpsum.SHORT,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .weight(weight = 1f, fill = false)
                .verticalScroll(rememberScrollState()),
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { scope.launch { state.collapse() } },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Close")
        }
    }
}
