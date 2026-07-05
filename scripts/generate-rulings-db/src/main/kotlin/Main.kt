import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.nio.file.Path
import java.sql.Connection
import java.sql.DriverManager
import java.util.concurrent.TimeUnit
import kotlin.io.path.*

val json = Json { ignoreUnknownKeys = true }

@Serializable
data class CardJson(
    val id: Int,
    val name: String,
    val Stats: CardStats? = null,
    val Rulings: Map<String, String> = emptyMap(),
)

@Serializable
data class CardStats(
    val type: String? = null,
    val frameType: String? = null,
    val atk: Long? = null,
    val def: Long? = null,
    val level: Long? = null,
    val race: String? = null,
    val attribute: String? = null,
    val desc: String? = null,
)

@Serializable
data class YgoApiResponse(val data: List<YgoCard>? = null)

@Serializable
data class YgoCard(
    val id: Int,
    @SerialName("card_images")
    val cardImages: List<CardImage>? = null,
)

@Serializable
data class CardImage(
    @SerialName("image_url")
    val imageUrl: String? = null,
)

data class CardRecord(
    val id: Int,
    val name: String,
    val type: String,
    val frameType: String,
    val atk: Int?,
    val def: Int?,
    val level: Int?,
    val race: String,
    val attribute: String?,
    val desc: String,
    val imageUrl: String?,
    val rulings: Map<String, String>,
)

private val FORMATS = setOf(
    "Goat", "Edison", "Tengu", "REDU", "HAT",
    "Vegas", "Meadowlands", "FireWater", "Cat",
    "TeleDAD", "Gladiator", "ReturnDAD", "Trooper",
    "Circle", "Stein", "Base",
)

fun main() {
    val repoPath = Path.of("mikaRulings")
    val outputDb = Path.of("../../app/src/main/assets/databases/rulings.db")
    val imageCacheFile = Path.of("image_urls_cache.json")

    if (repoPath.exists()) {
        println("Pulling latest mikaRulings...")
        ProcessBuilder("git", "-C", repoPath.toString(), "pull")
            .inheritIO().start().waitFor()
    } else {
        println("Cloning mikaRulings...")
        ProcessBuilder("git", "clone", "https://github.com/MikaMikaDE/mikaRulings.git", repoPath.toString())
            .inheritIO().start().waitFor()
    }

    val cardsDir = repoPath.resolve("cards")
    val records = mutableListOf<CardRecord>()

    println("Parsing card JSONs...")
    cardsDir.toFile().listFiles()?.filter { it.extension == "json" }?.forEach { file ->
        try {
            val card = json.decodeFromString<CardJson>(file.readText())
            records.add(
                CardRecord(
                    id = card.id,
                    name = card.name,
                    type = card.Stats?.type ?: "",
                    frameType = card.Stats?.frameType ?: "",
                    atk = card.Stats?.atk?.toInt(),
                    def = card.Stats?.def?.toInt(),
                    level = card.Stats?.level?.toInt(),
                    race = card.Stats?.race ?: "",
                    attribute = card.Stats?.attribute,
                    desc = card.Stats?.desc ?: "",
                    imageUrl = null,
                    rulings = card.Rulings.filterKeys { it in FORMATS },
                )
            )
        } catch (e: Exception) {
            println("Error parsing ${file.name}: ${e.message}")
        }
    }
    println("Parsed ${records.size} cards.")

    val imageCache = mutableMapOf<Int, String>()
    if (imageCacheFile.exists()) {
        try {
            @Suppress("UNCHECKED_CAST")
            val cacheMap = json.decodeFromString<Map<String, String>>(imageCacheFile.readText())
            cacheMap.forEach { (key, value) -> imageCache[key.toInt()] = value }
            println("Loaded ${imageCache.size} cached image URLs.")
        } catch (_: Exception) { }
    }

    println("Fetching image URLs from YGOPRODeck API (20 req/sec)...")
    val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val newUrls = mutableMapOf<Int, String>()
    val batchSize = 50

    records.forEachIndexed { index, record ->
        if (imageCache.containsKey(record.id)) return@forEachIndexed
        try {
            val request = Request.Builder()
                .url("https://db.ygoprodeck.com/api/v7/cardinfo.php?id=${record.id}")
                .build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            val apiResponse = json.decodeFromString<YgoApiResponse>(body)
            val url = apiResponse.data?.firstOrNull()?.cardImages?.firstOrNull()?.imageUrl ?: ""
            newUrls[record.id] = url
            response.close()
        } catch (e: Exception) {
            println("  Error fetching image for ${record.name}: ${e.message}")
            newUrls[record.id] = ""
        }
        if (index % batchSize == 0) {
            Thread.sleep(2500)
            println("  Progress: $index/${records.size}")
        }
    }

    imageCache.putAll(newUrls)
    val cacheJson = buildString {
        append("{")
        imageCache.entries.forEachIndexed { i, (k, v) ->
            if (i > 0) append(",")
            append("\"$k\":\"${v.replace("\"", "\\\"")}\"")
        }
        append("}")
    }
    imageCacheFile.writeText(cacheJson)
    println("Cached ${imageCache.size} image URLs.")

    records.forEachIndexed { index, record ->
        records[index] = record.copy(imageUrl = imageCache[record.id] ?: "")
    }

    println("Building rulings.db...")
    outputDb.parent.toFile().mkdirs()
    outputDb.toFile().delete()

    val conn: Connection = DriverManager.getConnection("jdbc:sqlite:${outputDb.toAbsolutePath()}")
    conn.createStatement().use { stmt ->
        stmt.execute("PRAGMA journal_mode=OFF")
        stmt.execute("PRAGMA synchronous=OFF")
        stmt.execute("""
            CREATE TABLE cards (
                id INTEGER PRIMARY KEY NOT NULL,
                name TEXT NOT NULL,
                type TEXT NOT NULL,
                frameType TEXT NOT NULL,
                atk INTEGER,
                def INTEGER,
                level INTEGER,
                race TEXT NOT NULL,
                attribute TEXT,
                desc TEXT NOT NULL,
                image_url TEXT
            )
        """.trimIndent())

        stmt.execute("""
            CREATE TABLE rulings (
                card_id INTEGER NOT NULL,
                format TEXT NOT NULL,
                ruling_text TEXT NOT NULL,
                PRIMARY KEY (card_id, format)
            )
        """.trimIndent())

        stmt.execute("""
            CREATE VIRTUAL TABLE cards_fts USING fts4(content="cards", name)
        """.trimIndent())
    }

    conn.autoCommit = false

    val insertCardPs = conn.prepareStatement(
        "INSERT INTO cards(id, name, type, frameType, atk, def, level, race, attribute, desc, image_url) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
    )
    val insertRulingPs = conn.prepareStatement(
        "INSERT INTO rulings(card_id, format, ruling_text) VALUES (?, ?, ?)"
    )

    records.forEachIndexed { index, record ->
        insertCardPs.setInt(1, record.id)
        insertCardPs.setString(2, record.name)
        insertCardPs.setString(3, record.type)
        insertCardPs.setString(4, record.frameType)
        if (record.atk != null) insertCardPs.setInt(5, record.atk) else insertCardPs.setNull(5, java.sql.Types.INTEGER)
        if (record.def != null) insertCardPs.setInt(6, record.def) else insertCardPs.setNull(6, java.sql.Types.INTEGER)
        if (record.level != null) insertCardPs.setInt(7, record.level) else insertCardPs.setNull(7, java.sql.Types.INTEGER)
        insertCardPs.setString(8, record.race)
        if (record.attribute != null) insertCardPs.setString(9, record.attribute) else insertCardPs.setNull(9, java.sql.Types.VARCHAR)
        insertCardPs.setString(10, record.desc)
        if (record.imageUrl != null) insertCardPs.setString(11, record.imageUrl) else insertCardPs.setNull(11, java.sql.Types.VARCHAR)
        insertCardPs.executeUpdate()

        record.rulings.forEach { (format, text) ->
            insertRulingPs.setInt(1, record.id)
            insertRulingPs.setString(2, format)
            insertRulingPs.setString(3, text)
            insertRulingPs.executeUpdate()
        }

        if (index % 500 == 0) {
            conn.commit()
            println("  Inserted $index/${records.size} cards...")
        }
    }

    insertCardPs.close()
    insertRulingPs.close()
    conn.commit()

    conn.createStatement().execute("INSERT INTO cards_fts(cards_fts) VALUES('rebuild')")
    conn.autoCommit = true
    conn.close()

    println("Done! rulings.db generated at ${outputDb.toAbsolutePath()}")
    println("Total cards: ${records.size}")
    println("Total rulings: ${records.sumOf { it.rulings.size }}")
}
