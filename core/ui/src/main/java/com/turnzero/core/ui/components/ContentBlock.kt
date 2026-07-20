package com.turnzero.core.ui.components

sealed interface ContentBlock {
    data class Paragraph(val text: String) : ContentBlock
    data class SubHeading(val text: String) : ContentBlock
    data class BulletList(val items: List<String>) : ContentBlock
    data class Image(val uri: String) : ContentBlock
}
