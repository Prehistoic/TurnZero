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
