package com.turnzero.tools.rules

/**
 * The user's choice for every specificity of a [RulesManifest]. A specificity id appears in
 * exactly one of the three maps, matching its [SpecificityType].
 */
data class Selection(
    val single: Map<String, String> = emptyMap(), // specId -> optionId
    val number: Map<String, Int> = emptyMap(), // specId -> value
    val multi: Map<String, Set<String>> = emptyMap() // specId -> selected optionIds
)

/** Evaluates a content `when` expression against the user's current [selection]. */
fun matches(condition: Condition, selection: Selection): Boolean = when (condition) {
    is Condition.Predicate -> matchesPredicate(condition, selection)
    is Condition.And -> condition.conditions.all { matches(it, selection) }
    is Condition.Or -> condition.conditions.any { matches(it, selection) }
    is Condition.Not -> !matches(condition.condition, selection)
}

/** The selection the rules tool starts with: first option, minimum number, no multi options. */
fun defaultSelection(manifest: RulesManifest): Selection = Selection(
    single = manifest.specificities
        .filter { it.type == SpecificityType.SINGLE }
        .associate { it.id to (it.options.firstOrNull()?.id ?: "") },
    number = manifest.specificities
        .filter { it.type == SpecificityType.NUMBER }
        .associate { it.id to (it.min ?: 0) },
    multi = manifest.specificities
        .filter { it.type == SpecificityType.MULTI }
        .associate { it.id to emptySet() }
)

/**
 * Selects [optionId] for [specId], pulling in the transitive `requires` closure of that option.
 * SINGLE replaces the current value, MULTI adds to it, and NUMBER ignores option selection.
 */
fun selectOption(
    manifest: RulesManifest,
    selection: Selection,
    specId: String,
    optionId: String
): Selection {
    val spec = manifest.specificities.firstOrNull { it.id == specId } ?: return selection
    return when (spec.type) {
        SpecificityType.SINGLE ->
            selection.copy(single = selection.single + (specId to optionId))
        SpecificityType.MULTI -> {
            val selected = selection.multi[specId].orEmpty() + requiredClosure(spec, optionId)
            selection.copy(multi = selection.multi + (specId to selected))
        }
        SpecificityType.NUMBER -> selection
    }
}

/**
 * Deselects [optionId] for [specId] together with every option that transitively requires it.
 * Only MULTI specs hold options, so SINGLE and NUMBER ignore deselection.
 */
fun deselectOption(
    manifest: RulesManifest,
    selection: Selection,
    specId: String,
    optionId: String
): Selection {
    val spec = manifest.specificities.firstOrNull { it.id == specId } ?: return selection
    if (spec.type != SpecificityType.MULTI) return selection

    val selected = selection.multi[specId].orEmpty()
    // Safe to short-circuit: any selected dependent would imply optionId is selected (selectOption
    // closes over requires), so an unselected option can never have selected dependents.
    if (optionId !in selected) return selection

    val removed = dependentClosure(spec, optionId)
    return selection.copy(multi = selection.multi + (specId to (selected - removed)))
}

/** Sets [value] for the NUMBER [specId], clamped to its configured `min..max` range. */
fun withNumber(
    manifest: RulesManifest,
    selection: Selection,
    specId: String,
    value: Int
): Selection {
    val spec = manifest.specificities.firstOrNull { it.id == specId } ?: return selection
    if (spec.type != SpecificityType.NUMBER) return selection

    val clamped = value.coerceIn(spec.min ?: Int.MIN_VALUE, spec.max ?: Int.MAX_VALUE)
    return selection.copy(number = selection.number + (specId to clamped))
}

/**
 * Every selection the rules UI can reach, for dead-rule validation: the cross-product of every
 * SINGLE option, every NUMBER value in `min..max`, and every dependency-closed MULTI subset.
 * Extension counts are small, so the exhaustive enumeration is cheap.
 */
fun validSelections(manifest: RulesManifest): List<Selection> =
    manifest.specificities.fold(listOf(Selection())) { selections, spec ->
        val choices = choicesFor(spec)
        selections.flatMap { selection -> choices.map { selection.mergedWith(it) } }
    }

/** Whether some reachable [validSelections] entry satisfies [condition]; false means a dead rule. */
fun isSatisfiable(condition: Condition, manifest: RulesManifest): Boolean =
    validSelections(manifest).any { matches(condition, it) }

private fun matchesPredicate(predicate: Condition.Predicate, selection: Selection): Boolean {
    val specId = predicate.specId
    return when {
        specId in selection.single ->
            matchesScalar(selection.single.getValue(specId), predicate.value)
        // Numbers compare through their string form so every SpecValue variant applies.
        specId in selection.number ->
            matchesScalar(selection.number.getValue(specId).toString(), predicate.value)
        specId in selection.multi ->
            matchesOptions(selection.multi.getValue(specId), predicate.value)
        else -> false
    }
}

/** Matches a SINGLE option id or a stringified NUMBER against [value]. */
private fun matchesScalar(actual: String, value: SpecValue): Boolean = when (value) {
    is SpecValue.Eq -> actual == value.value
    is SpecValue.AllOf -> value.values.all { it == actual }
    is SpecValue.AnyOf -> actual in value.values
    is SpecValue.NoneOf -> actual !in value.values
    SpecValue.NoneSelected -> false
}

/** Matches the option ids selected for a MULTI specificity against [value]. */
private fun matchesOptions(selected: Set<String>, value: SpecValue): Boolean = when (value) {
    is SpecValue.Eq -> value.value in selected
    is SpecValue.AllOf -> selected.containsAll(value.values)
    is SpecValue.AnyOf -> value.values.any { it in selected }
    is SpecValue.NoneOf -> value.values.none { it in selected }
    SpecValue.NoneSelected -> selected.isEmpty()
}

/** [optionId] plus every option it transitively requires. */
private fun requiredClosure(spec: SpecificityDef, optionId: String): Set<String> {
    val byId = spec.options.associateBy { it.id }
    val pending = ArrayDeque(listOf(optionId))
    val closure = mutableSetOf<String>()
    while (pending.isNotEmpty()) {
        val id = pending.removeFirst()
        if (closure.add(id)) pending.addAll(byId[id]?.requires.orEmpty())
    }
    return closure
}

/** [optionId] plus every option that transitively requires it. */
private fun dependentClosure(spec: SpecificityDef, optionId: String): Set<String> {
    val dependents = mutableSetOf(optionId)
    var foundMore = true
    while (foundMore) {
        foundMore = false
        for (option in spec.options) {
            if (option.id !in dependents && option.requires.any { it in dependents }) {
                dependents += option.id
                foundMore = true
            }
        }
    }
    return dependents
}

/** One fragment per legal value of [spec], i.e. one factor of the [validSelections] cross-product. */
private fun choicesFor(spec: SpecificityDef): List<Selection> = when (spec.type) {
    // An optionless SINGLE spec mirrors the `""` default of [defaultSelection].
    SpecificityType.SINGLE -> (spec.options.map { it.id }.ifEmpty { listOf("") })
        .map { Selection(single = mapOf(spec.id to it)) }
    SpecificityType.NUMBER -> numberValues(spec).map { Selection(number = mapOf(spec.id to it)) }
    SpecificityType.MULTI -> dependencyClosedSubsets(spec).map {
        Selection(multi = mapOf(spec.id to it))
    }
}

/**
 * Every value of a NUMBER spec's `min..max`. An open upper bound contributes `min` alone, since
 * there is no finite range to enumerate.
 */
private fun numberValues(spec: SpecificityDef): List<Int> {
    val min = spec.min ?: 0
    val max = spec.max ?: min
    return (min..max).toList()
}

/** All 2ⁿ option subsets in which every selected option's `requires` are selected as well. */
private fun dependencyClosedSubsets(spec: SpecificityDef): List<Set<String>> {
    val ids = spec.options.map { it.id }
    val requires = spec.options.associate { it.id to it.requires.toSet() }
    return (0 until (1 shl ids.size)).mapNotNull { mask ->
        val subset = ids.filterIndexed { index, _ -> mask and (1 shl index) != 0 }.toSet()
        subset.takeIf { selected ->
            selected.all { id -> requires[id].orEmpty().all { required -> required in selected } }
        }
    }
}

/** Combines two disjoint per-spec fragments into a single selection. */
private fun Selection.mergedWith(other: Selection): Selection = Selection(
    single = single + other.single,
    number = number + other.number,
    multi = multi + other.multi
)
