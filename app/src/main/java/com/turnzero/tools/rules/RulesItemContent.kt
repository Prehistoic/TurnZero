package com.turnzero.tools.rules

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.turnzero.core.ui.components.parseBoldText
import java.io.IOException

/** Renders one [RuleItem]: optional title, paragraphs, bullets, then an optional asset image. */
@Composable
fun RulesItemContent(item: RuleItem, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        item.title?.let { title ->
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }
        item.text.forEach { paragraph ->
            Text(
                text = remember(paragraph) { parseBoldText(paragraph) },
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        item.bullets.forEach { bullet ->
            Text(
                text = remember(bullet) { bulletText(bullet) },
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        item.image?.let { assetPath ->
            RulesAssetImage(assetPath = assetPath)
        }
    }
}

/** Renders an image stored in the app assets; a missing or undecodable asset renders nothing. */
@Composable
fun RulesAssetImage(
    assetPath: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    val context = LocalContext.current
    val bitmap = remember(assetPath) {
        try {
            context.assets.open(assetPath).use { BitmapFactory.decodeStream(it) }
        } catch (e: IOException) {
            null
        }
    }
    bitmap?.let {
        Image(
            bitmap = it.asImageBitmap(),
            contentDescription = contentDescription,
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.FillWidth,
        )
    }
}

internal fun bulletText(bullet: String): AnnotatedString = buildAnnotatedString {
    append("\u2022 ")
    append(parseBoldText(bullet))
}
