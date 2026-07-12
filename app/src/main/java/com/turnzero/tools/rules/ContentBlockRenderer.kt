package com.turnzero.tools.rules

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
fun ContentBlockRenderer(
    blocks: List<ContentBlock>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        blocks.forEachIndexed { index, block ->
            if (index > 0) {
                Spacer(
                    Modifier.height(
                        when (block) {
                            is ContentBlock.SubHeading -> 12.dp
                            is ContentBlock.Image -> 12.dp
                            else -> 8.dp
                        }
                    )
                )
            }
            when (block) {
                is ContentBlock.Paragraph -> Text(
                    text = parseBoldText(block.text),
                    style = MaterialTheme.typography.bodyMedium,
                )
                is ContentBlock.SubHeading -> Text(
                    text = parseBoldText(block.text),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                is ContentBlock.BulletList -> Column {
                    block.items.forEach { item ->
                        Text(
                            text = buildAnnotatedString {
                                append("\u2022 ")
                                append(parseBoldText(item))
                            },
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
                is ContentBlock.Image -> {
                    val context = LocalContext.current
                    if (block.uri.startsWith("inline:")) {
                        val actualUrl = block.uri.removePrefix("inline:")
                        AsyncImage(
                            model = actualUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(20.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            contentScale = ContentScale.Fit,
                        )
                    } else if (block.uri.startsWith("asset:")) {
                        val assetPath = block.uri.removePrefix("asset:")
                        val bmp = remember(assetPath) {
                            loadAssetImage(context, assetPath)
                        }
                        bmp?.let {
                            Image(
                                bitmap = it.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.FillWidth,
                            )
                        }
                    } else {
                        AsyncImage(
                            model = block.uri,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.FillWidth,
                        )
                    }
                }
            }
        }
    }
}

internal fun parseBoldText(text: String) = buildAnnotatedString {
    var remaining = text
    while (remaining.isNotEmpty()) {
        val start = remaining.indexOf("**")
        if (start == -1) {
            append(remaining)
            break
        }
        if (start > 0) {
            append(remaining.substring(0, start))
        }
        val afterOpen = remaining.substring(start + 2)
        val end = afterOpen.indexOf("**")
        if (end == -1) {
            append(remaining.substring(start))
            break
        }
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
            append(afterOpen.substring(0, end))
        }
        remaining = afterOpen.substring(end + 2)
    }
}

private fun loadAssetImage(context: Context, assetPath: String): android.graphics.Bitmap? {
    return try {
        context.assets.open(assetPath).use { BitmapFactory.decodeStream(it) }
    } catch (_: Exception) {
        null
    }
}
