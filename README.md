# Sheets

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.dokar3/sheets/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.dokar3/sheets)

Another feature-rich bottom sheet in Jetpack Compose.

<a href="images/screenshot_simple.png"><img src="images/screenshot_simple.png" width="32%"/></a>
<a href="images/screenshot_list.png"><img src="images/screenshot_list.png" width="32%"/></a>
<a href="images/screenshot_intent-picker.png"><img src="images/screenshot_intent-picker.png" width="32%"/></a>

# Features

### Easy to use
  Unlike [`ModalBottomSheetLayout`](https://developer.android.com/reference/kotlin/androidx/compose/material/package-summary#ModalBottomSheetLayout(kotlin.Function1,androidx.compose.ui.Modifier,androidx.compose.material.ModalBottomSheetState,androidx.compose.ui.graphics.Shape,androidx.compose.ui.unit.Dp,androidx.compose.ui.graphics.Color,androidx.compose.ui.graphics.Color,androidx.compose.ui.graphics.Color,kotlin.Function0))
  , this bottom sheet will be displayed in a dialog window, which means we can easily create and
  display multiple sheets in the same composable:

  ```kotlin
  @Composable
  fun MyComposable(modifier: Modifier = Modifier) {
      val sheet1 = rememberBottomSheetState()
      val sheet2 = rememberBottomSheetState()
  
      BottomSheet(state = sheet1) { ... }
      BottomSheet(state = sheet2) { ... }
  }
  ```


### Peek support

  ```kotlin
  BottomSheet(
      state = state,
      // PeekHeight.px(Int) and PeekHeight.fraction(Float) are supported as well.
      peekHeight = PeekHeight.dp(300),
      // Set to true you don't want the peeked state.
      skipPeeked = false,
  ) { ...}
  ```


### Customizable animations

  ```kotlin
  // Animation off
  state.expand(animated = false)

  // Default
  state.expand(animationSpec = spring())

  // Slow animation
  state.expand(animationSpec = tween(durationMillis = 2000))
  ```


### Interceptable state

```kotlin
val state = rememberBottomSheetState(
    confirmValueChange = {
        if (it == BottomSheetValue.Collapsed) {
            // Intercept logic
        } else {
            true
        }
    },
)
```

### Whole sheet above the keyboard

```kotlin
BottomSheet(
    state = state,
    showAboveKeyboard = true,
) {
    TextFieldSheetContent()
}
```

### Material 2 and Material 3

Migration is simple, just change the imports.

```kotlin
// Material 2:
import com.dokar.sheets.BottomSheet

// Material 3:
import com.dokar.sheets.m3.BottomSheet
```

### Window Controlling

System bar colors and some dialog window properties can be customized by the `behaviors` parameter.

```kotlin
BottomSheet(
    ...
    behaviors = BottomSheetDefaults.dialogSheetBehaviors(
        dialogSecurePolicy = SecureFlagPolicy.Inherit,
        dialogWindowSoftInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED,
        lightStatusBar = false,
        lightNavigationBar = false,
        statusBarColor = Color.Transparent,
        navigationBarColor: Color = Color.Black,
    ),
) { ... }
```

### Embedded sheet

To embed the sheet in the current layout hierarchy, use the `BottomSheetLayout()`:

```kotlin
Box {
    MainContent()

    val state = rememberBottomSheetState()
    if (state.visible) {
        BottomSheetLayout(state = state) {
            ...
        }
    }
}
```

### Listenable drag progress

It's useful when syncing some transitions with the drag gesture.

```kotlin
fun Modifier.iosBottomSheetTransitions(
    state: BottomSheetState,
    statusBarInsets: WindowInsets,
): Modifier = graphicsLayer {
    val progress = (state.dragProgress - 0.5f) / 0.5f
    if (progress <= 0f) {
        return@graphicsLayer
    }

    val scale = 1f - 0.1f * progress
    scaleX = scale
    scaleY = scale

    val statusBarHeight = statusBarInsets.getTop(this)
    val scaledTopSpacing = size.height * 0.1f / 2f
    translationY = progress * (statusBarHeight +
            16.dp.toPx() - scaledTopSpacing)

    clip = true
    shape = RoundedCornerShape(progress * 16.dp)
}
```

# Usage

### Material 2

```kotlin
implementation("io.github.dokar3:sheets:latest_version")
```

### Material 3

```kotlin
implementation("io.github.dokar3:sheets-m3:latest_version")
```

# License

```
Copyright 2022 dokar3

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
