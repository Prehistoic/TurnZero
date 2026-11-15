package com.turnzero.tools.rules

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import java.io.File
import java.io.FileOutputStream
import kotlin.math.abs

/**
 * Page-aware PDF viewer started at [initialPage]. Pinch to zoom, swipe or use the arrows to
 * change pages, tap the page indicator to jump to a page.
 */
@Composable
fun RulebookPdfViewer(
    assetPath: String,
    modifier: Modifier = Modifier,
    initialPage: Int = 0,
) {
    val context = LocalContext.current
    val cachedPath by remember(assetPath) { mutableStateOf(copyRulebookAssetToCache(context, assetPath)) }
    val pdfState = remember { mutableStateOf<RulebookPdfState?>(null) }
    var pageCount by remember { mutableIntStateOf(0) }
    var currentPage by remember(assetPath) { mutableIntStateOf(initialPage.coerceAtLeast(0)) }

    DisposableEffect(cachedPath) {
        val path = cachedPath ?: return@DisposableEffect onDispose {}
        try {
            val fd = ParcelFileDescriptor.open(File(path), ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(fd)
            pdfState.value = RulebookPdfState(renderer, fd)
            pageCount = renderer.pageCount
        } catch (_: Exception) { }
        onDispose {
            pdfState.value?.renderer?.close()
            pdfState.value?.fd?.close()
            pdfState.value = null
        }
    }

    LaunchedEffect(cachedPath, pageCount, initialPage) {
        if (pageCount > 0) currentPage = initialPage.coerceIn(0, pageCount - 1)
    }

    val state = pdfState.value
    val bitmap = remember(state, currentPage) {
        if (state == null || currentPage >= pageCount) return@remember null
        try {
            val pdfPage = state.renderer.openPage(currentPage)
            val sf = 2.0
            val w = (pdfPage.width * sf).toInt()
            val h = (pdfPage.height * sf).toInt()
            val bmp = createBitmap(w, h, Bitmap.Config.ARGB_8888)
            Canvas(bmp).drawColor(Color.WHITE)
            pdfPage.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            pdfPage.close()
            bmp
        } catch (_: Exception) { null }
    }

    var scale by remember { mutableFloatStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var wasPinching by remember { mutableStateOf(false) }
    var swipeAccum by remember { mutableFloatStateOf(0f) }

    var showPageDialog by remember { mutableStateOf(false) }
    var pageInput by remember { mutableStateOf("") }
    var pageInputError by remember { mutableStateOf(false) }

    val goToPage = {
        val num = pageInput.toIntOrNull()
        if (num != null && num in 1..pageCount) {
            currentPage = num - 1
            scale = 1f
            panOffset = Offset.Zero
            showPageDialog = false
        } else {
            pageInputError = true
        }
    }

    if (showPageDialog) {
        AlertDialog(
            onDismissRequest = { showPageDialog = false },
            title = { Text("Go to page") },
            text = {
                OutlinedTextField(
                    value = pageInput,
                    onValueChange = {
                        pageInput = it
                        pageInputError = false
                    },
                    label = { Text("Page number (1-$pageCount)") },
                    isError = pageInputError,
                    supportingText = if (pageInputError) {
                        { Text("Enter a number between 1 and $pageCount") }
                    } else null,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = { goToPage() }),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(onClick = goToPage) { Text("Go") }
            },
            dismissButton = {
                TextButton(onClick = { showPageDialog = false }) { Text("Cancel") }
            },
        )
    }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (pageCount > 1) {
                Text(
                    "${currentPage + 1} / $pageCount",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.clickable {
                        pageInput = "${currentPage + 1}"
                        pageInputError = false
                        showPageDialog = true
                    },
                )
            }
            Row {
                IconButton(
                    onClick = { currentPage--; scale = 1f; panOffset = Offset.Zero },
                    enabled = currentPage > 0,
                ) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Previous page") }
                IconButton(
                    onClick = { currentPage++; scale = 1f; panOffset = Offset.Zero },
                    enabled = currentPage < pageCount - 1,
                ) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Next page") }
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clipToBounds()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            scale = if (scale > 1f) 1f else 2f
                            panOffset = Offset.Zero
                        },
                    )
                }
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        if (zoom != 1f) wasPinching = true
                        if (scale > 1f || zoom != 1f) {
                            val newScale = (scale * zoom).coerceIn(1f, 5f)
                            val scaleChange = newScale / scale
                            panOffset = (panOffset + pan) * scaleChange
                            scale = newScale
                        } else {
                            swipeAccum += pan.x
                        }
                    }
                }
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            if (event.changes.all { !it.pressed }) {
                                if (!wasPinching && scale == 1f && abs(swipeAccum) > 80f) {
                                    if (swipeAccum > 0 && currentPage > 0) currentPage--
                                    else if (swipeAccum < 0 && currentPage < pageCount - 1) currentPage++
                                }
                                wasPinching = false
                                swipeAccum = 0f
                            }
                        }
                    }
                },
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Page ${currentPage + 1}",
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = panOffset.x,
                            translationY = panOffset.y,
                        ),
                )
            }
        }
    }
}

private data class RulebookPdfState(
    val renderer: PdfRenderer,
    val fd: ParcelFileDescriptor,
)

private fun copyRulebookAssetToCache(context: Context, assetPath: String): String? {
    return try {
        val cacheDir = File(context.cacheDir, "rulebooks")
        cacheDir.mkdirs()
        val fileName = assetPath.substringAfterLast('/')
        val cachedFile = File(cacheDir, fileName)
        if (!cachedFile.exists()) {
            context.assets.open(assetPath).use { input ->
                FileOutputStream(cachedFile).use { output ->
                    input.copyTo(output)
                }
            }
        }
        cachedFile.absolutePath
    } catch (_: Exception) {
        null
    }
}
