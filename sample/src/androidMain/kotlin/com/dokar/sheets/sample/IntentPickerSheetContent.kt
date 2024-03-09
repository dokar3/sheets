package com.dokar.sheets.sample

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dokar.sheets.BottomSheetState
import com.dokar.sheets.sample.intentpicker.ActivityInfo
import com.dokar.sheets.sample.intentpicker.IntentPicker
import kotlinx.coroutines.launch

@Composable
actual fun IntentPickerSheetContent(
    state: BottomSheetState,
    modifier: Modifier,
) {
    val iconTint = MaterialTheme.colors.onSurface

    class CopyText : ActivityInfo(
        "Copy text",
        icon = R.drawable.ic_baseline_file_copy_24,
        iconTint = iconTint
    )

    val scope = rememberCoroutineScope()

    val context = LocalContext.current

    val presets by remember { mutableStateOf(listOf(CopyText())) }

    var showAsGrid by remember { mutableStateOf(false) }

    val intent = remember {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Hello there")
        }
        intent
    }

    PickerLayoutStyleTab(
        showAsGrid = showAsGrid,
        onRequestShowAsGrid = { showAsGrid = it },
    )

    IntentPicker(
        intent = intent,
        modifier = modifier,
        onIntentPick = {
            val resolveInfo = it.resolveInfo
            scope.launch { state.collapse() }
            when {
                it is CopyText -> {
                }
                resolveInfo != null -> {
                    val pkgName = resolveInfo.activityInfo.packageName
                    val activity = resolveInfo.activityInfo.name
                    intent.run {
                        setClassName(pkgName, activity)
                    }
                    context.startActivitySafely(intent)
                }
            }
        },
        loadingContent = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp, 16.dp, 8.dp, 32.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.width(32.dp))
                Text(
                    "Loading",
                    fontSize = 14.sp
                )
            }
        },
        showAsGrid = showAsGrid,
        presets = presets,
    )
}

@Composable
private fun PickerLayoutStyleTab(
    showAsGrid: Boolean,
    onRequestShowAsGrid: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        val defIconFilter = ColorFilter.tint(MaterialTheme.colors.onSurface)
        val selectedFilter = ColorFilter.tint(MaterialTheme.colors.primary)
        val icons = intArrayOf(
            R.drawable.ic_baseline_list_24,
            R.drawable.ic_baseline_grid_24,
        )
        val selectedIconRes = if (showAsGrid) {
            R.drawable.ic_baseline_grid_24
        } else {
            R.drawable.ic_baseline_list_24
        }
        for (iconRes in icons) {
            Row(
                modifier = Modifier
                    .weight(1f, fill = true)
                    .clickable {
                        onRequestShowAsGrid(iconRes == R.drawable.ic_baseline_grid_24)
                    },
                horizontalArrangement = Arrangement.Center
            ) {
                val isSelected = iconRes == selectedIconRes
                Image(
                    painter = painterResource(iconRes),
                    modifier = Modifier.height(48.dp),
                    contentDescription = "",
                    colorFilter = if (isSelected) selectedFilter else defIconFilter
                )
            }
        }
    }
}

private fun Context.startActivitySafely(intent: Intent) {
    try {
        startActivity(intent)
    } catch (e: Exception) {
        //
    }
}