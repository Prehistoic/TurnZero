package com.turnzero.tools.rules

import com.turnzero.core.ui.components.ContentBlock

fun p(text: String) = ContentBlock.Paragraph(text)
fun sh(text: String) = ContentBlock.SubHeading(text)
fun bl(items: List<String>) = ContentBlock.BulletList(items)
fun img(path: String) = ContentBlock.Image("asset:$path")
