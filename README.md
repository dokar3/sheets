# Sheets

![Maven Central](https://img.shields.io/maven-central/v/io.github.dokar3/sheets?style=flat-square&color=%23ea197e)

[Online Demo](https://dokar3.github.io/sheets/)

Another feature-rich bottom sheet for Compose Multiplatform. The following targets are supported.

- Android
- Desktop
- Web (wasmJs)

Seeking contributions for the iOS target since I'm a Windows-only user. If you want to help please feel free to make a pull request.

<a href="images/screenshot_simple.png"><img src="images/screenshot_simple.png" width="32%"/></a>
<a href="images/screenshot_list.png"><img src="images/screenshot_list.png" width="32%"/></a>
<a href="images/screenshot_intent-picker.png"><img src="images/screenshot_intent-picker.png" width="32%"/></a>

Why this is needed when we already have `ModalBottomSheetLayout` and `ModalBottomSheet`? See the [Comparisons](#Comparisons) table. 

# Features


### Peek support

  ```kotlin
  BottomSheet(
      state = state,
      // PeekHeight.px(Int) and PeekHeight.fraction(Float) are supported as well.
      peekHeight = PeekHeight.dp(300),
      // Set to true to the peeked state, default to false.
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

May be useful when the bottom sheet contains some text fields.

> *Please note after setting this, your sheet content be squashed if the bottom sheet is too long, so make your content scrollable by default.*

```kotlin
BottomSheet(
    state = state,
    showAboveKeyboard = true,
) {
    TextFieldSheetContent(
        modifier = Modifier.verticalScroll(rememberScrollState()),
    )
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

# Comparisons

|   Feature\Component    | sheets | ModalBottomSheet | ModalBottomSheetLayout |
|:----------------------:|:------:|:----------------:|:----------------------:|
|       Material 2       |   ✅    |        ❌         |           ✅            |
|       Material 3       |   ✅    |        ✅         |           ❌            |
|        Embedded        |   ✅    |        ❌         |           ✅            |
|   In modal (Dialog)    |   ✅    |        ✅         |           ❌            |
|   Dialog properties    |   ✅    |        ❌         |           /            |
|      Drag handle       |   ✅    |        ✅         |           ❌            |
| Half expanded (Peeked) |   ✅    |        ✅         |           ✅            |
|      Peek height       |   ✅    |        ❌         |           ❌            |
| Custom animation spec  |   ✅    |        ❌         |           ✅            |
|     Drag progress      |   ✅    |        ❌         |           ✅            |
|   confirmValueChange   |   ✅    |        ✅         |           ✅            |
|   Dim color (scrim)    |   ✅    |        ✅         |           ✅            |

*Compose Material is still evolving, so this table may no longer be accurate after some versions.*

# Contribution

Need some features? Found a bug?

Open an issue, or just create a pull request if you want! This project is open for contributions.

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
