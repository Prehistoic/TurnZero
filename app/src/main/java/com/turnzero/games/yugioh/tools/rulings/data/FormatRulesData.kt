package com.turnzero.games.yugioh.tools.rulings.data

data class FormatMeta(
    val key: String,
    val displayName: String,
    val dateLabel: String,
    val enabledByDefault: Boolean = false,
    val rules: List<FormatRule> = emptyList(),
) {
    val labelWithDate: String get() = "$displayName - $dateLabel"
}

object FormatRulesData {

    val goatRules = listOf(
        FormatRule("Start With 6", "The player who goes first draws a card at the start of their first Draw Phase. This means they will initially have 6 cards in their starting hand."),
        FormatRule("Unlimited Fusion Deck and Main Deck", "The Extra Deck was called the Fusion Deck and could contain as many cards as you wanted. The Main Deck was allowed to be unlimited."),
        FormatRule("One Field Spell to Rule Them All", "It is not possible for both players to control a face-up active Field Spell. If the activation of a Field Spell resolves while the opponent controls a face-up Field Spell, the previous Field Spell is destroyed. Field Spells must remain face-up in order to resolve."),
        FormatRule("Priority for Ignition Effects", "The turn player can activate a monster's Ignition Effect before the opponent is allowed to respond. This does not affect how Trigger Effects work."),
        FormatRule("Replaying Attacks", "A replay occurs when the number of the opponent's monsters changes during the Battle Step. In Goat Format, play rewinds back to the beginning of the Battle Step, treated as an entirely new attack."),
        FormatRule("Continuous Traps with Ignition-Like Effects", "Continuous Traps with Ignition-like effects cannot be flipped face-up and use their Ignition-like effects at the same time. They must be flipped face-up pre-emptively."),
        FormatRule("Verifying Hands and Decks", "Effects that require hands or decks to be revealed must always be carried out, even if knowledge of the game's rules already provides enough information."),
        FormatRule("Failure to Find", "Cards that search the Deck can be used without any legal options. Exception: 'The Agent of Creation - Venus' requires 'Mystical Shine Balls' in Deck to activate."),
        FormatRule("SEGOC (Simultaneous Effects Go On Chain)", "When two effects were triggered at different times, the earlier trigger(s) must be placed on the Chain before later ones. Order: Turn Player Mandatory, Non-Turn Player Mandatory, Turn Player Optional, Non-Turn Player Optional."),
        FormatRule("The Damage Step", "Six substeps instead of five. Only one Chain in 'before damage calculation'. Only monster effects that negate activations can be used in the Damage Step, no Quick-Play Spells or Traps."),
        FormatRule("The Match Draw is a Lie", "Although the rulebook stated a match could end in a draw, this was not applied at tournaments. Matches must always be played until one player has won 2 duels."),
        FormatRule("Dude, Where's My Trigger?", "Triggers can be recognized mid-Chain. Moving monsters to a different location to prevent them from triggering will not work. Trigger Effects can be met while the monster is in the Deck."),
        FormatRule("Monsters Equipping Monsters", "If 'Relinquished' or 'Thousand-Eyes Restrict' tries to equip an opponent's monster, and an effect is Chained that removes it from the field or flips it face-down, the opponent keeps their monster."),
        FormatRule("A Cure for the Summoning Sickness", "If you take control of an opponent's monster, you can always change its battle position even if it was just Summoned that turn."),
        FormatRule("The Other Guys (0 ATK Battles)", "When two Attack Position monsters with 0 ATK battle each other, both monsters are destroyed in battle (unlike modern Yu-Gi-Oh!)."),
        FormatRule("Killing Yourself to Live (LP Costs)", "You cannot pay a cost that would reduce you to 0 LP. You must have more LP than the cost requires. Mandatory LP maintenance costs cause self-destruction if you cannot pay."),
        FormatRule("Infinity Wars (Infinite Loops)", "A voluntary action that would cause an infinite loop is simply illegal. Involuntary loops cause the primary cause to be destroyed by game mechanics."),
        FormatRule("State of the Unions", "A monster can only be equipped with 1 Union Monster at a time. Most Union Monsters prevent destruction by battle only, not card effects. Exception: 'Heavy Mech Support Platform'."),
        FormatRule("The End of the Turn (Discard Phase)", "Players may respond to the end-of-turn discard in whatever ways they wish. This differs from modern Yu-Gi-Oh!."),
        FormatRule("It's a Trap Monster", "Trap Monsters occupy both a Monster Zone and a Spell/Trap Zone simultaneously. Only 'Embodiment of Apophis' in Goat Format."),
    )

    val edisonRules = listOf(
        FormatRule("The Starting Player Draws a Card", "In Edison Format, a card is drawn during the first Draw Phase. The player who goes first starts with 6 cards."),
        FormatRule("Only 1 Active Field Spell", "Only one Field Spell can be active at a time. Setting a Field Spell does not destroy an opponent's active Field Spell."),
        FormatRule("Union Monster Conditions", "A monster can only be equipped with 1 Union Monster at a time. Only specific Union Monsters have the destruction-prevention effect."),
        FormatRule("Phase-Dependent Mandatory Trigger Effects", "Any mandatory Trigger Effect that activates during a certain Phase will keep activating if its activation is negated, until it finally resolves. Effect negation stops re-activation."),
        FormatRule("Trap Monster Zone Blocking", "Trap Monsters take up 1 Monster Zone and 1 Spell/Trap Zone. Cannot activate if no empty Monster Zones."),
        FormatRule("Ignition Effect Priority", "The Turn Player can activate an Ignition Effect as CL1 in the Summon response timing, if the Summon did not start a Chain."),
        FormatRule("SEGOC (Simultaneous Effects Go On Chain)", "Earlier triggers must be placed on chain before later ones in the same step. Order: Turn Player Mandatory, Non-Turn Player Mandatory, Turn Player Optional, Non-Turn Player Optional."),
        FormatRule("The 7-Timing Damage Step", "Seven sub-steps. ATK/DEF modifiers only in substeps 1-3. From substep 4 onward, only monster Quick Effects and Counter Traps."),
        FormatRule("Trigger Location & Recognition", "Triggers recognized mid-chain. Effects activate from outside their trigger location, even from the Deck."),
        FormatRule("Life Points Costs", "Cannot pay costs that reduce LP to 0. Maintenance costs self-destruct instead."),
        FormatRule("End-of-Turn Discard", "Cannot respond to end-of-turn hand size discard. Only SS2 negation effects and Counter Traps can chain."),
        FormatRule("Infinite Loops", "Voluntary infinite loops are illegal. Involuntary loops: primary cause destroyed by game mechanics."),
        FormatRule("0 ATK Monsters", "Two ATK-position 0 ATK monsters destroy each other. A 0 ATK monster cannot destroy a DEF-position 0 DEF monster."),
    )

    val tenguRules = listOf(
        FormatRule("Starting Player Draws a Card", "The player who goes first draws a card during their Draw Phase, starting with 6 cards. Changed July 10, 2014."),
        FormatRule("Only 1 Active Field Spell", "Only 1 Field Spell Card can be face-up at a time. Setting does not destroy opponent's face-up Field Spell."),
        FormatRule("Union Monster Conditions", "A monster can only be equipped with 1 Union Monster at a time."),
        FormatRule("Ignition Effect Priority", "After successfully Summoning a monster, the turn player has Priority to activate Ignition Effects before the opponent can respond. Removed April 25, 2012."),
        FormatRule("SEGOC", "Earlier triggers placed on Chain first when multiple effects trigger simultaneously."),
        FormatRule("The 7-Timing Damage Step", "Seven substeps. SS2 Spell/Trap negation CANNOT activate. ATK/DEF modifiers CANNOT activate in/after Substep 4."),
        FormatRule("Life Points Costs", "Cannot pay costs that would reduce LP to 0."),
        FormatRule("End-of-Turn Discard", "Cannot respond to end-of-turn discard. Optional Trigger Effects cannot activate."),
        FormatRule("Xyz Mechanics", "Xyz Materials are NOT on the field. Xyz Summoning is NOT Tributing. 'Reborn Tengu' used as Material does NOT trigger."),
        FormatRule("Infinite Loops", "Voluntary infinite loops are illegal. Involuntary: primary cause destroyed."),
    )

    val reduRules = listOf(
        FormatRule("Starting Player Draws a Card", "The player who goes first draws a card during their Draw Phase, starting with 6 cards."),
        FormatRule("Only 1 Active Field Spell", "Only 1 Field Spell Card can be face-up at a time."),
        FormatRule("Union Monster Conditions", "A monster can only be equipped with 1 Union Monster at a time."),
        FormatRule("No Ignition Effect Priority", "Ignition Effect Priority was removed April 25, 2012. Opponent can respond to Summons before Ignition Effects."),
        FormatRule("SEGOC", "Earlier triggers placed on Chain first."),
        FormatRule("The 7-Timing Damage Step", "Seven substeps. SS2 Spell/Trap negation CANNOT activate."),
        FormatRule("Life Points Costs", "Cannot pay costs that would reduce LP to 0."),
        FormatRule("End-of-Turn Discard", "Cannot respond. Optional Trigger Effects cannot activate."),
        FormatRule("Xyz Mechanics", "Materials NOT on field. Summoning is NOT Tributing."),
        FormatRule("Infinite Loops", "Voluntary infinite loops illegal. Involuntary: primary cause destroyed."),
    )

    val hatRules = listOf(
        FormatRule("The Starting Player Draws a Card", "In HAT Format, a card is drawn during the first Draw Phase. Player going first starts with 6 cards."),
        FormatRule("Only 1 Active Field Spell", "Only one Field Spell may remain active. Both players may Set Field Spells without destroying opponent's."),
        FormatRule("Union Monster Conditions", "A monster can only be equipped with 1 Union Monster. Old Condition text. Removed 2016."),
        FormatRule("Trap Monsters", "Trap Monsters occupy BOTH a Trap Card Zone AND a Monster Card Zone. Cannot activate without empty Monster Zone."),
        FormatRule("SEGOC", "Earlier triggers on Chain first. Changed 2017."),
        FormatRule("The 7-Timing Damage Step", "Seven timings. SS2 Spell/Trap negation CANNOT activate. ATK/DEF modifiers CANNOT activate in/after Substep 4."),
        FormatRule("Life Points Costs", "Cannot pay costs reducing LP to 0. Maintenance costs self-destruct."),
        FormatRule("End-of-Turn Discard", "Cannot respond. Optional Trigger Effects cannot activate."),
        FormatRule("Infinite Loops", "Voluntary infinite loops illegal. Involuntary: primary cause destroyed."),
    )

    val allFormats: List<FormatMeta> = listOf(
        FormatMeta("Goat", "Goat", "Apr. 2005", enabledByDefault = true, rules = goatRules),
        FormatMeta("Stein", "Stein", "Jun. 2006"),
        FormatMeta("Trooper", "Trooper", "May 2007"),
        FormatMeta("Circle", "Circle", "Mar. 2008"),
        FormatMeta("ReturnDAD", "ReturnDAD", "Sep. 2008"),
        FormatMeta("TeleDAD", "TeleDAD", "Nov. 2008"),
        FormatMeta("Gladiator", "Gladiator", "Sep. 2009"),
        FormatMeta("Cat", "Cat", "Mar. 2010"),
        FormatMeta("Edison", "Edison", "Mar. 2010", enabledByDefault = true, rules = edisonRules),
        FormatMeta("Tengu", "Tengu", "Sep. 2011", enabledByDefault = true, rules = tenguRules),
        FormatMeta("REDU", "REDU", "Sep. 2012", enabledByDefault = true, rules = reduRules),
        FormatMeta("Vegas", "Vegas", "Mar. 2013"),
        FormatMeta("FireWater", "Fire/Water", "Sep. 2013"),
        FormatMeta("Meadowlands", "Meadowlands", "Mar. 2014"),
        FormatMeta("HAT", "HAT", "Jul. 2014", enabledByDefault = true, rules = hatRules),
        FormatMeta("Modern", "Modern", "Current", enabledByDefault = true),
    )

    val defaultEnabledKeys: Set<String> = allFormats
        .filter { it.enabledByDefault }
        .map { it.key }
        .toSet()

    val keyOrder: Map<String, Int> = allFormats.mapIndexed { i, f -> f.key to i }.toMap()

    fun getMeta(formatKey: String): FormatMeta? =
        allFormats.find { it.key.equals(formatKey, ignoreCase = true) }

    fun getDisplayName(formatKey: String): String = getMeta(formatKey)?.displayName ?: formatKey

    fun getLabelWithDate(formatKey: String): String = getMeta(formatKey)?.labelWithDate ?: formatKey

    fun sortByChronological(keys: List<String>): List<String> =
        keys.sortedBy { keyOrder[it] ?: Int.MAX_VALUE }
}
