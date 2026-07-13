package com.turnzero.core.tools.api

import androidx.annotation.DrawableRes

interface Game {
    val id: String
    val name: String
    val description: String
    val toolIds: List<String>
    val logoRes: Int?
        @DrawableRes get() = null
}
