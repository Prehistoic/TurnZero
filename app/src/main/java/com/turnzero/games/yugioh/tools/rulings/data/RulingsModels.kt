package com.turnzero.games.yugioh.tools.rulings.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "cards")
data class CardEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val type: String,
    @ColumnInfo(name = "frameType") val frameType: String,
    val atk: Int?,
    val def: Int?,
    val level: Int?,
    val race: String,
    val attribute: String?,
    val desc: String,
    @ColumnInfo(name = "image_url") val imageUrl: String?,
)

@Entity(
    tableName = "rulings",
    primaryKeys = ["card_id", "format"]
)
data class RulingEntity(
    @ColumnInfo(name = "card_id") val cardId: Int,
    val format: String,
    @ColumnInfo(name = "ruling_text") val rulingText: String,
)

@Entity(
    tableName = "card_descriptions",
    primaryKeys = ["card_id", "format"],
    foreignKeys = [ForeignKey(
        entity = CardEntity::class,
        parentColumns = ["id"],
        childColumns = ["card_id"],
        onDelete = ForeignKey.NO_ACTION
    )],
    indices = [Index("card_id")]
)
data class CardDescriptionEntity(
    @ColumnInfo(name = "card_id") val cardId: Int,
    val format: String,
    val description: String,
)

data class CardSearchResult(
    val id: Int,
    val name: String,
    val type: String,
    val imageUrl: String?,
)

data class CardRulingsDetail(
    val id: Int,
    val name: String,
    val type: String,
    val desc: String,
    val imageUrl: String?,
    val rulingsText: String,
)

data class FormatRule(
    val title: String,
    val description: String,
    val isSpecial: Boolean = false,
    val subSections: List<RuleSubSection> = emptyList(),
)

data class RuleSubSection(
    val title: String,
    val body: String,
)
