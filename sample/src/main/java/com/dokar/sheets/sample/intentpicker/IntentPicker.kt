package com.dokar.sheets.sample.intentpicker

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.DataSource
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.request.Options
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

open class ActivityInfo(
    val label: CharSequence,
    val id: Int = 0,
    var icon: Any? = null,
    var iconTint: Color? = null,
    var resolveInfo: ResolveInfo? = null
)

private class IconRequest(
    val resolveInfo: ResolveInfo,
    val pkgMgr: PackageManager,
)

private class IconFetcher(
    private val data: IconRequest,
) : Fetcher {
    override suspend fun fetch(): FetchResult {
        return DrawableResult(
            drawable = data.resolveInfo.loadIcon(data.pkgMgr),
            isSampled = true,
            dataSource = DataSource.DISK
        )
    }

    class Factory : Fetcher.Factory<IconRequest> {
        override fun create(
            data: IconRequest,
            options: Options,
            imageLoader: ImageLoader
        ): Fetcher {
            return IconFetcher(data)
        }
    }
}

@Composable
fun coilImageLoader(): ImageLoader {
    val context = LocalContext.current
    return remember(context) {
        ImageLoader.Builder(context)
            .components {
                add(IconFetcher.Factory())
            }
            .build()
    }
}

private suspend fun loadActivityInfos(
    context: Context,
    intent: Intent,
): List<ActivityInfo> = withContext(Dispatchers.IO) {
    val pm = context.packageManager
    pm.queryIntentActivities(intent, 0)
        .map { info ->
            ActivityInfo(
                label = info.loadLabel(pm),
                resolveInfo = info
            )
        }
}

@Composable
fun IntentPicker(
    intent: Intent,
    modifier: Modifier = Modifier,
    onIntentPick: ((info: ActivityInfo) -> Unit)? = null,
    loadingContent: (@Composable () -> Unit)? = null,
    emptyContent: (@Composable () -> Unit)? = null,
    showAsGrid: Boolean = false,
    presets: List<ActivityInfo> = emptyList(),
) {
    val context = LocalContext.current

    var isLoading by remember { mutableStateOf(false) }

    var infos: List<ActivityInfo> by remember(intent) {
        mutableStateOf(emptyList())
    }

    LaunchedEffect(intent, presets) {
        isLoading = true
        infos = presets + loadActivityInfos(
            context = context,
            intent = intent,
        )
        isLoading = false
    }

    Box(modifier = modifier) {
        when {
            isLoading -> {
                if (loadingContent != null) {
                    loadingContent()
                }
            }
            infos.isEmpty() -> {
                if (emptyContent != null) {
                    emptyContent()
                }
            }
            showAsGrid -> {
                val density = LocalDensity.current
                val colSizePx = with(density) { 88.dp.toPx() }
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val width = constraints.maxWidth
                    val cols = (width / colSizePx).toInt()
                    val actualColSize = with(density) { (width / cols).toDp() }

                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(infos.chunked(cols)) { colInfos ->
                            IntentItemRows(colInfos, actualColSize) {
                                onIntentPick?.invoke(it)
                            }
                        }
                    }
                }
            }
            else -> {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(infos) { info ->
                        HorizontalIntentItem(info) {
                            onIntentPick?.invoke(info)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IntentItemRows(
    infos: List<ActivityInfo>,
    colSize: Dp,
    onClick: (info: ActivityInfo) -> Unit
) {
    Row {
        for (info in infos) {
            VerticalIntentItem(info, colSize) {
                onClick(info)
            }
        }
    }
}

@Composable
private fun VerticalIntentItem(info: ActivityInfo, colSize: Dp, onClick: () -> Unit) {
    val padding = 8.dp
    Column(
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = false)
            ) {
                onClick()
            }
            .width(colSize)
            .padding(padding)
    ) {
        val iconSize = min(48.dp, colSize * 0.9f)
        ItemIcon(
            info,
            iconSize = iconSize,
            iconTint = info.iconTint,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = info.label.toString(),
            modifier = Modifier.align(Alignment.CenterHorizontally),
            fontSize = 13.sp,
            maxLines = 1,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun HorizontalIntentItem(info: ActivityInfo, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        ItemIcon(info, iconSize = 36.dp, iconTint = info.iconTint)

        Text(
            text = info.label.toString(),
            modifier = Modifier
                .padding(start = 16.dp, end = 8.dp)
                .align(Alignment.CenterVertically),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ItemIcon(
    info: ActivityInfo,
    iconSize: Dp,
    iconTint: Color?,
    modifier: Modifier = Modifier
) {
    val tintFilter = if (iconTint != null) {
        ColorFilter.tint(iconTint)
    } else {
        null
    }
    val icon = info.icon
    val resolveInfo = info.resolveInfo
    when {
        icon is Int -> {
            Image(
                painter = painterResource(icon),
                modifier = modifier.size(iconSize),
                contentDescription = "",
                colorFilter = tintFilter
            )
        }
        resolveInfo != null -> {
            val req = IconRequest(resolveInfo, LocalContext.current.packageManager)
            AsyncImage(
                model = req,
                imageLoader = coilImageLoader(),
                modifier = modifier.size(iconSize),
                contentDescription = "",
                colorFilter = tintFilter
            )
        }
        else -> {
            Spacer(modifier = modifier.size(iconSize))
        }
    }
}
