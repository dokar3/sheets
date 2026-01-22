package com.dokar.sheets.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Surface
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.isUnspecified
import androidx.compose.ui.unit.sp
import com.dokar.sheets.BottomSheet
import com.dokar.sheets.rememberBottomSheetState
import com.dokar.sheets.sample.theme.ComposeBottomSheetTheme
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
@OptIn(ExperimentalLayoutApi::class)
internal fun SampleScreen(
    isDarkTheme: Boolean,
    onUpdateDarkTheme: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = if (isDarkTheme) Color(0xff121212) else Color.White

    val scope = rememberCoroutineScope()

    var material3 by remember { mutableStateOf(false) }
    var withAnimation by remember { mutableStateOf(true) }
    var iosLike by remember { mutableStateOf(false) }
    var skipPeeked by remember { mutableStateOf(true) }
    var maxWidth by remember { mutableStateOf(Dp.Unspecified) }

    var showAboveKeyboard by remember { mutableStateOf(false) }

    var contentType by remember { mutableStateOf(SheetContentType.Simple) }

    var containerHeight by remember { mutableIntStateOf(0) }

    val state = rememberBottomSheetState()

    val maxWidths = remember {
        mutableListOf(Dp.Unspecified, 500.dp, 700.dp, 1000.dp)
    }

    val density = LocalDensity.current

    val topInset = with(density) { WindowInsets.statusBars.getTop(this).toDp() }

    Material3Surface(
        isDarkTheme = isDarkTheme,
        backgroundColor = Color.Black,
        modifier = Modifier.onSizeChanged { containerHeight = it.height },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .let {
                    if (iosLike) {
                        it.iosBottomSheetTransitions(state = state, WindowInsets.statusBars)
                    } else {
                        it
                    }
                }
                .background(backgroundColor)
                .verticalScroll(state = rememberScrollState())
                .windowInsetsPadding(WindowInsets.systemBars),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = modifier
                    .padding(16.dp)
                    .widthIn(max = 1000.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Sheets",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Dark", color = if (isDarkTheme) Color.White else Color.Black)
                        Spacer(modifier = Modifier.width(4.dp))
                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = onUpdateDarkTheme,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Options", fontWeight = FontWeight.Bold)

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SheetOptionChip(
                        selected = material3,
                        onClick = { material3 = !material3 },
                        label = { Text("Material 3") },
                    )

                    SheetOptionChip(
                        selected = withAnimation,
                        onClick = { withAnimation = !withAnimation },
                        label = { Text("With animation") },
                    )

                    SheetOptionChip(
                        selected = iosLike,
                        onClick = { iosLike = !iosLike },
                        label = { Text("iOS like") },
                    )

                    SheetOptionChip(
                        selected = skipPeeked,
                        onClick = { skipPeeked = !skipPeeked },
                        label = { Text("Skip peeked") },
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(text = "Max width", fontWeight = FontWeight.Bold)

                Spacer(Modifier.height(8.dp))

                DpSlider(
                    values = maxWidths,
                    selected = maxWidths.indexOf(maxWidth),
                    onSelect = { maxWidth = maxWidths[it] },
                )

                Spacer(Modifier.height(16.dp))

                Text(text = "Content", fontWeight = FontWeight.Bold)

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    for (type in SheetContentType.entries) {
                        SheetOptionChip(
                            selected = type == contentType,
                            onClick = { contentType = type },
                            label = { Text(type.name) }
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))

                Text(text = "Actions", fontWeight = FontWeight.Bold)

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(onClick = { scope.launch { state.peek(animate = withAnimation) } }) {
                        Text("Peek")
                    }

                    Button(onClick = { scope.launch { state.expand(animate = withAnimation) } }) {
                        Text("Expand")
                    }
                }

                Spacer(Modifier.height(16.dp))

                DashedDivider()

                Spacer(Modifier.height(16.dp))

                Text(text = "Code", fontWeight = FontWeight.Bold)

                Spacer(Modifier.height(8.dp))

                SampleCode(
                    code = rememberSampleCode(
                        material3 = material3,
                        maxWidth = maxWidth,
                        withAnimation = withAnimation,
                        iosTransition = iosLike,
                        skipPeeked = skipPeeked,
                    ),
                    modifier = Modifier.height(500.dp),
                )
            }
        }
    }

    val content = movableContentOf {
        when (contentType) {
            SheetContentType.Simple -> SimpleSheetContent(state)
            SheetContentType.List -> ListSheetContent()
            SheetContentType.IntentPicker -> IntentPickerSheetContent(state)
            SheetContentType.Inputs -> TextFieldSheetContent(
                state = state,
                showAboveKeyboard = showAboveKeyboard,
                onShowAboveKeyboardChange = { showAboveKeyboard = it },
                modifier = Modifier.verticalScroll(rememberScrollState()),
            )
        }
    }

    val sheetModifier = Modifier.widthIn(max = maxWidth)
        .let {
            if (iosLike) {
                it.height(with(density) { containerHeight.toDp() - topInset - 32.dp })
            } else {
                it
            }
        }

    if (material3) {
        Material3Surface(
            isDarkTheme = isDarkTheme,
            backgroundColor = backgroundColor,
        ) {
            com.dokar.sheets.m3.BottomSheet(
                state = state,
                modifier = sheetModifier,
                skipPeeked = skipPeeked,
                showAboveKeyboard = showAboveKeyboard
            ) {
                content()
            }
        }
    } else {
        ComposeBottomSheetTheme(darkTheme = isDarkTheme) {
            Surface(color = backgroundColor) {
                BottomSheet(
                    state = state,
                    modifier = sheetModifier,
                    skipPeeked = skipPeeked,
                    showAboveKeyboard = showAboveKeyboard,
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun Material3Surface(
    isDarkTheme: Boolean,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (isDarkTheme) {
            darkColorScheme()
        } else {
            lightColorScheme()
        },
    ) {
        androidx.compose.material3.Surface(
            color = backgroundColor,
            contentColor = contentColorFor(
                MaterialTheme.colorScheme.background
            ),
            modifier = modifier,
        ) {
            content()
        }
    }
}

enum class SheetContentType {
    Simple,
    List,
    IntentPicker,
    Inputs,
}

@Composable
private fun SheetOptionChip(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = label,
        modifier = modifier,
        leadingIcon = {
            if (selected) {
                Text("✓")
            }
        },
    )
}

@Composable
private fun DpSlider(
    values: List<Dp>,
    selected: Int,
    onSelect: (index: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            for ((index, dp) in values.withIndex()) {
                Text(
                    text = if (dp.isUnspecified) "Unspecified" else "${dp.value.toInt()}dp",
                    fontSize = 15.sp,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onSelect(index) }
                    )
                )
            }
        }

        Slider(
            value = selected.toFloat() / values.lastIndex,
            onValueChange = {
                val perStep = 1f / (values.size - 1)
                val index = (it / perStep).roundToInt()
                if (index != selected) {
                    onSelect(index)
                }
            },
            steps = values.size - 2,
        )
    }
}

@Composable
private fun DashedDivider(
    height: Dp = 2.dp,
    dashWidth: Dp = 8.dp,
    gap: Dp = 4.dp,
    color: Color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.36f),
) {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .drawWithCache {
                val pathEffect = PathEffect.dashPathEffect(
                    intervals = floatArrayOf(dashWidth.toPx(), gap.toPx())
                )
                onDrawBehind {
                    drawLine(
                        color = color,
                        start = center.copy(x = 0f),
                        end = center.copy(x = size.width),
                        strokeWidth = height.toPx(),
                        pathEffect = pathEffect,
                    )
                }
            },
    )
}

@Composable
private fun SampleCode(
    code: String,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    Box(modifier = modifier.clip(MaterialTheme.shapes.medium)) {
        SelectionContainer {
            Text(
                text = code,
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xff313131))
                    .padding(8.dp)
                    .verticalScroll(state = scrollState),
            )
        }
    }
}

@Composable
private fun rememberSampleCode(
    material3: Boolean,
    maxWidth: Dp,
    withAnimation: Boolean,
    iosTransition: Boolean,
    skipPeeked: Boolean,
): String {
    return remember(material3, maxWidth, withAnimation, iosTransition, skipPeeked) {
        val animateParam = if (withAnimation) "" else "animate = false"
        val maxWidthModifier = if (maxWidth.isSpecified) {
            ".widthIn(max = $maxWidth)"
        } else {
            ""
        }
        val iosContainerModifier = if (iosTransition) {
            val indent = "                        "
            "\n$indent.iosBottomSheetTransitions(state, WindowInsets.statusBar)"
        } else {
            ""
        }
        val iosSheetModifier = if (iosTransition) {
            val indent = "                            "
            "\n$indent.height(with(LocalDensity.current) { containerHeight.toDp() - 32.dp })"
        } else {
            ""
        }
        """
            // Other imports
            import com.dokar.sheets${if (material3) ".m3" else ""}.BottomSheet
            import com.dokar.sheets.rememberBottomSheetState
            
            @Composable
            fun SampleScreen(modifier: Modifier = Modifier) {
                val scope = rememberCoroutineScope()
                val state = rememberBottomSheetState()
                
                fun show() = scope.launch { state.expand($animateParam) }
                
                fun hide() = scope.launch { state.collapse($animateParam}) }
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()$iosContainerModifier,
                ) {
                    // Screen content

                    BottomSheet(
                        state = state,
                        modifier = Modifier$maxWidthModifier$iosSheetModifier,
                        skipPeeked = $skipPeeked,
                    ) {
                        // Sheet content
                    }
                }
            }
            ${if (iosTransition) IOS_BOTTOM_SHEET_TRANSITION_CODE else ""}
        """.trimIndent()
    }
}

private const val IOS_BOTTOM_SHEET_TRANSITION_CODE = """
            fun Modifier.iosBottomSheetTransitions(
                state: BottomSheetState,
                statusBarInsets: WindowInsets,
            ): Modifier = graphicsLayer {
                val progress = (state.dragProgress - 0.5f) / 0.5f
                if (progress <= 0f) {
                    return@graphicsLayer
                }
            
                val minScale = if (size.width > size.height) 0.95f else 0.92f
            
                val scale = 1f - (1f - minScale) * progress
                scaleX = scale
                scaleY = scale
            
                val statusBarHeight = statusBarInsets.getTop(this)
                val scaledTopSpacing = size.height * (1f - minScale) / 2f
                translationY = progress * (statusBarHeight +
                        16.dp.toPx() - scaledTopSpacing)
            
                clip = true
                shape = RoundedCornerShape(progress * 16.dp)
            }
"""
