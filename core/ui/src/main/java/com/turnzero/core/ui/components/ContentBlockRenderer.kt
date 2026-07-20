package com.turnzero.core.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp

@Composable
fun ContentBlockRenderer(
    blocks: List<ContentBlock>,
    modifier: Modifier = Modifier,
    imageContent: @Composable (String) -> Unit = {},
) {
    Column(modifier = modifier) {
        blocks.forEachIndexed { index, block ->
            if (index > 0) {
                val spacerHeight = remember(block) {
                    when (block) {
                        is ContentBlock.SubHeading -> 12.dp
                        is ContentBlock.Image -> 12.dp
                        else -> 8.dp
                    }
                }
                Spacer(Modifier.height(spacerHeight))
            }
            when (block) {
                is ContentBlock.Paragraph -> {
                    val parsed = remember(block.text) { parseBoldText(block.text) }
                    Text(
                        text = parsed,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                is ContentBlock.SubHeading -> {
                    val parsed = remember(block.text) { parseBoldText(block.text) }
                    Text(
                        text = parsed,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                is ContentBlock.BulletList -> Column {
                    block.items.forEach { item ->
                        val parsed = remember(item) { parseBoldText(item) }
                        Text(
                            text = remember(item) {
                                buildAnnotatedString {
                                    append("\u2022 ")
                                    append(parseBoldText(item))
                                }
                            },
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
                is ContentBlock.Image -> {
                    imageContent(block.uri)
                }
            }
        }
    }
}

fun parseBoldText(text: String) = buildAnnotatedString {
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
