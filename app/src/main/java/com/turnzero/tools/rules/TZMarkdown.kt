package com.turnzero.tools.rules

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.mikepenz.markdown.compose.components.markdownComponents
import com.mikepenz.markdown.m3.Markdown

/**
 * Renders markdown content using the [Markdown] composable with custom image handling.
 *
 * ## Image Rendering
 *
 * Two modes supported via URL prefix convention:
 *
 * * **Full-width image:** Use standard markdown `![Alt text](url)`.
 *   Renders as a full-width image with 8dp vertical padding and rounded corners.
 *   Suitable for diagrams, field layouts, card art, etc.
 *
 *   ```markdown
 *   ![Field Layout](https://example.com/field.png)
 *   ```
 *
 * * **Inline icon:** Use `![](inline:url)`.
 *   Renders as a 20dp icon that flows inline with surrounding text.
 *   Suitable for card type icons, attribute symbols, etc.
 *
 *   ```markdown
 *   **Normal Spell** ![](inline:https://example.com/spell-icon.png)
 *   **Quick-Play Spell** ![](inline:https://example.com/quickplay-icon.png)
 *   ```
 *
 * Note: The `inline:` prefix is stripped from the URL before loading.
 *
 * ## Usage in Rules Content
 *
 * All [RuleStep.detail], [WinCondition.description], and [Keyword.definition] fields
 * support full markdown including the image conventions above.
 * [RuleStep.summary] is plain text only (no markdown rendering).
 */
@Composable
fun MarkdownContent(
    content: String,
    modifier: Modifier = Modifier,
) {
    Markdown(
        content = content,
        components = markdownComponents(
            image = { model ->
                val url = model.content
                if (url.startsWith("inline:")) {
                    val actualUrl = url.removePrefix("inline:")
                    AsyncImage(
                        model = actualUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(20.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        contentScale = ContentScale.Fit,
                    )
                } else {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        AsyncImage(
                            model = url,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.FillWidth,
                        )
                    }
                }
            },
        ),
        modifier = modifier,
    )
}
