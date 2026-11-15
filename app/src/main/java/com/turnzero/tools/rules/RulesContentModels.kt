package com.turnzero.tools.rules

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

@Serializable
data class RulesManifest(
    val specificities: List<SpecificityDef> = emptyList(),
    val rulebooks: List<RulebookDef> = emptyList()
)

@Serializable
enum class SpecificityType {
    @SerialName("single") SINGLE,
    @SerialName("number") NUMBER,
    @SerialName("multi") MULTI
}

@Serializable
data class SpecificityDef(
    val id: String,
    val name: String,
    val type: SpecificityType,
    val options: List<OptionDef> = emptyList(),
    val min: Int? = null,
    val max: Int? = null
)

@Serializable
data class OptionDef(
    val id: String,
    val name: String,
    val requires: List<String> = emptyList()
)

@Serializable
data class RulebookDef(
    val id: String,
    val name: String,
    val asset: String,
    @SerialName("when") val `when`: Condition = Condition.ALWAYS
)

@Serializable
data class PageRef(
    val rulebook: String,
    val pages: List<Int> = emptyList(),
    @SerialName("when") val `when`: Condition = Condition.ALWAYS
)

@Serializable
data class RuleItem(
    val title: String? = null,
    val text: List<String> = emptyList(),
    val bullets: List<String> = emptyList(),
    val image: String? = null,
    @SerialName("when") val `when`: Condition = Condition.ALWAYS
)

@Serializable
data class Phase(
    val name: String,
    val steps: List<RuleItem> = emptyList(),
    @SerialName("when") val `when`: Condition = Condition.ALWAYS
)

@Serializable
data class SetupContent(
    val pages: List<PageRef> = emptyList(),
    val steps: List<RuleItem> = emptyList()
)

@Serializable
data class TurnContent(
    val pages: List<PageRef> = emptyList(),
    val phases: List<Phase> = emptyList()
)

@Serializable
data class WinContent(
    val pages: List<PageRef> = emptyList(),
    val winConditions: List<RuleItem> = emptyList()
)

@Serializable
data class KeywordsContent(
    val pages: List<PageRef> = emptyList(),
    val keywords: List<KeywordItem> = emptyList()
)

@Serializable
data class KeywordItem(
    val name: String,
    val category: String? = null,
    val text: List<String> = emptyList(),
    val bullets: List<String> = emptyList(),
    @SerialName("when") val `when`: Condition = Condition.ALWAYS
)

@Serializable
data class GameRules(
    val manifest: RulesManifest,
    val setup: SetupContent,
    val turn: TurnContent,
    val win: WinContent,
    val keywords: KeywordsContent
)

/**
 * Boolean `when` expression. The JSON form is shape-based (see [ConditionSerializer]):
 * `{}` is "always", a bare `{specId: value}` map is an implicit AND, and `and`/`or`/`not`
 * are explicit combinators.
 */
@Serializable(with = ConditionSerializer::class)
sealed interface Condition {
    data class Predicate(val specId: String, val value: SpecValue) : Condition
    data class And(val conditions: List<Condition>) : Condition
    data class Or(val conditions: List<Condition>) : Condition
    data class Not(val condition: Condition) : Condition

    companion object {
        /** Matches every selection — the implicit value of an absent `when`. */
        val ALWAYS: Condition = And(emptyList())
    }
}

/** The value compared against a specificity id inside a [Condition.Predicate]. */
@Serializable(with = SpecValueSerializer::class)
sealed interface SpecValue {
    data class Eq(val value: String) : SpecValue
    data class AllOf(val values: List<String>) : SpecValue
    data class AnyOf(val values: List<String>) : SpecValue
    data class NoneOf(val values: List<String>) : SpecValue
    data object NoneSelected : SpecValue
}

object ConditionSerializer : KSerializer<Condition> {
    override val descriptor: SerialDescriptor = JsonElement.serializer().descriptor

    override fun deserialize(decoder: Decoder): Condition {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw SerializationException("Condition can only be decoded from JSON")
        return jsonDecoder.decodeJsonElement().toCondition()
    }

    override fun serialize(encoder: Encoder, value: Condition) {
        val jsonEncoder = encoder as? JsonEncoder
            ?: throw SerializationException("Condition can only be encoded to JSON")
        jsonEncoder.encodeJsonElement(value.toJson())
    }
}

object SpecValueSerializer : KSerializer<SpecValue> {
    override val descriptor: SerialDescriptor = JsonElement.serializer().descriptor

    override fun deserialize(decoder: Decoder): SpecValue {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw SerializationException("SpecValue can only be decoded from JSON")
        return jsonDecoder.decodeJsonElement().toSpecValue()
    }

    override fun serialize(encoder: Encoder, value: SpecValue) {
        val jsonEncoder = encoder as? JsonEncoder
            ?: throw SerializationException("SpecValue can only be encoded to JSON")
        jsonEncoder.encodeJsonElement(value.toJson())
    }
}

private const val AND = "and"
private const val OR = "or"
private const val NOT = "not"

private val CONDITION_OPERATORS = setOf(AND, OR, NOT)

private fun JsonElement.toCondition(): Condition {
    val obj = this as? JsonObject
        ?: throw SerializationException("Condition must be a JSON object, got $this")
    if (obj.isEmpty()) return Condition.ALWAYS

    val operators = obj.keys.filter { it in CONDITION_OPERATORS }
    if (operators.isNotEmpty()) {
        if (obj.size != 1) {
            throw SerializationException(
                "Condition '${operators.first()}' must be the only key of its object, got ${obj.keys}"
            )
        }
        return when (val operator = operators.single()) {
            AND -> Condition.And(obj.getValue(operator).toConditionList("'and'"))
            OR -> Condition.Or(obj.getValue(operator).toConditionList("'or'"))
            NOT -> Condition.Not(obj.getValue(operator).toCondition())
            else -> throw SerializationException("Unknown condition operator '$operator'")
        }
    }

    return Condition.And(
        obj.map { (specId, value) -> Condition.Predicate(specId, value.toSpecValue()) }
    )
}

private fun JsonElement.toConditionList(operator: String): List<Condition> {
    val array = this as? JsonArray
        ?: throw SerializationException("Condition $operator expects a JSON array, got $this")
    return array.map { it.toCondition() }
}

private fun Condition.toJson(): JsonElement = when (this) {
    is Condition.Predicate -> JsonObject(mapOf(specId to value.toJson()))
    is Condition.And -> {
        val predicates = conditions.filterIsInstance<Condition.Predicate>()
        val isMergedMap = predicates.size == conditions.size &&
            predicates.map { it.specId }.toSet().size == predicates.size
        if (isMergedMap) {
            JsonObject(predicates.associate { it.specId to it.value.toJson() })
        } else {
            JsonObject(mapOf(AND to JsonArray(conditions.map { it.toJson() })))
        }
    }
    is Condition.Or -> JsonObject(mapOf(OR to JsonArray(conditions.map { it.toJson() })))
    is Condition.Not -> JsonObject(mapOf(NOT to condition.toJson()))
}

private fun JsonElement.toSpecValue(): SpecValue = when (this) {
    is JsonPrimitive -> SpecValue.Eq(asString("spec value"))
    is JsonArray ->
        if (isEmpty()) SpecValue.NoneSelected else SpecValue.AllOf(asStringList("spec value list"))
    is JsonObject -> {
        if (size != 1) {
            throw SerializationException(
                "Spec value object must have exactly one key, got $this"
            )
        }
        val (operator, operand) = entries.single()
        when (operator) {
            OR -> SpecValue.AnyOf(operand.asStringList("'or' spec value"))
            AND -> SpecValue.AllOf(operand.asStringList("'and' spec value"))
            NOT -> SpecValue.NoneOf(
                if (operand is JsonArray) {
                    operand.asStringList("'not' spec value")
                } else {
                    listOf(operand.asString("'not' spec value"))
                }
            )
            else -> throw SerializationException("Unknown spec value operator '$operator' in $this")
        }
    }
}

private fun SpecValue.toJson(): JsonElement = when (this) {
    is SpecValue.Eq -> JsonPrimitive(value)
    is SpecValue.AllOf -> JsonArray(values.map { JsonPrimitive(it) })
    is SpecValue.AnyOf -> JsonObject(mapOf(OR to JsonArray(values.map { JsonPrimitive(it) })))
    is SpecValue.NoneOf -> JsonObject(
        mapOf(
            NOT to if (values.size == 1) {
                JsonPrimitive(values.single())
            } else {
                JsonArray(values.map { JsonPrimitive(it) })
            }
        )
    )
    SpecValue.NoneSelected -> JsonArray(emptyList())
}

private fun JsonElement.asString(context: String): String {
    val primitive = this as? JsonPrimitive
        ?: throw SerializationException("Expected a JSON string for $context, got $this")
    if (!primitive.isString) {
        throw SerializationException("Expected a JSON string for $context, got $this")
    }
    return primitive.content
}

private fun JsonElement.asStringList(context: String): List<String> {
    val array = this as? JsonArray
        ?: throw SerializationException("Expected a JSON array for $context, got $this")
    return array.map { it.asString(context) }
}
