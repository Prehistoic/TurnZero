package com.turnzero.tools.rules

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
fun ContentBlockRenderer(
    blocks: List<com.turnzero.core.ui.components.ContentBlock>,
    modifier: Modifier = Modifier,
) {
    com.turnzero.core.ui.components.ContentBlockRenderer(
        blocks = blocks,
        modifier = modifier,
        imageContent = { uri -> RulesImageContent(uri) },
    )
}

@Composable
private fun RulesImageContent(uri: String) {
    val context = LocalContext.current
    if (uri.startsWith("inline:")) {
        val actualUrl = uri.removePrefix("inline:")
        AsyncImage(
            model = actualUrl,
            contentDescription = null,
            modifier = Modifier
                .size(20.dp)
                .clip(RoundedCornerShape(2.dp)),
            contentScale = ContentScale.Fit,
        )
    } else if (uri.startsWith("asset:")) {
        val assetPath = uri.removePrefix("asset:")
        val bmp = remember(assetPath) {
            try {
                context.assets.open(assetPath).use { BitmapFactory.decodeStream(it) }
            } catch (_: Exception) {
                null
            }
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
            model = uri,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.FillWidth,
        )
    }
}
