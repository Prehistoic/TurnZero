package com.turnzero.core.tools.api

interface Game {
    val id: String
    val name: String
    val description: String
    val imageRes: Int
    val toolIds: List<String>
}
