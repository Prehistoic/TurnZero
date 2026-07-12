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
        FormatRule(
            "Start With 6",
            "Unlike in current Yu-Gi-Oh!, **the player who goes first will draw a card at the start of their first Draw Phase.** This means that they will initially have 6 cards in their starting hand.",
        ),
        FormatRule(
            "Unlimited Fusion Deck and Main Deck",
            "Before Synchro Monsters were introduced, the Extra Deck was called the **Fusion Deck**, and it could contain as many cards as you wanted. Fusion Monsters were always Summoned to Main Monster Zones (Extra Monster Zones did not exist).\n\nAdditionally, the **Main Deck was allowed to be unlimited** — one player famously took a 2,222-card deck to a national tournament. Most online simulators will not allow more than 60 cards, however.\n\nSide Decks were treated slightly differently: players were technically not allowed to use Side Decks with more than 0 but less than 15 cards. This is rarely enforced today.",
        ),
        FormatRule(
            "One Field Spell to Rule Them All",
            "**It is not possible for both players to control a face-up active Field Spell.** If the activation of a Field Spell resolves while the opponent controls a face-up Field Spell, the previous Field Spell is destroyed. Field Spells must remain face-up in order to resolve.",
            subSections = listOf(
                RuleSubSection("Example 1", "Player A controls \"Necrovalley\". Player B activates \"A Legendary Ocean\". Player A has no response → \"Necrovalley\" is destroyed."),
                RuleSubSection("Example 2", "Player A controls \"Necrovalley\". Player B **sets** \"A Legendary Ocean\" face-down. \"Necrovalley\" remains on the field."),
                RuleSubSection("Example 3", "Player A controls \"Necrovalley\". Player B activates \"A Legendary Ocean\". Player A chains \"Mystical Space Typhoon\" targeting \"A Legendary Ocean\". It's destroyed, so it does not resolve → \"Necrovalley\" remains."),
            ),
        ),
        FormatRule(
            "Priority for Ignition Effects",
            "In current Yu-Gi-Oh!, when a Chain or Summon has finished resolving, the turn player can activate a \"fast effect\" (Spell Speed 2+) before the opponent can. **In Goat Format, the turn player can also activate a monster's Ignition Effect** before the opponent is allowed to respond. This does not affect how Trigger Effects work.",
            subSections = listOf(
                RuleSubSection("Example 1", "Player A Summons \"Tribe-Infecting Virus\". Player A can activate its Ignition Effect **before** Player B can activate \"Book of Moon\"."),
                RuleSubSection("Example 2", "Player A controls \"Cannon Soldier\" and Summons \"Sangan\". Player A can activate \"Cannon Soldier\"'s effect, tributing \"Sangan\", **before** Player B can activate \"Torrential Tribute\"."),
                RuleSubSection("Example 3", "Player A Summons \"Breaker the Magical Warrior\" — its Trigger Effect to add a Spell Counter is activated. Player B may Chain \"Ring of Destruction\" to destroy Breaker before Player A can use the Ignition Effect. **However**, if Player B has no response to the Trigger Effect, the Chain resolves, and Player A can activate Breaker's Ignition Effect before Player B can respond."),
            ),
        ),
        FormatRule(
            "Replaying Attacks",
            "A replay occurs when the number of the opponent's monsters changes during the Battle Step. In current Yu-Gi-Oh!, a replay rewinds play to where an attack target is selected — a \"redirection\" of the same attack. **In Goat Format, play rewinds back to the beginning of the Battle Step**, treated as an entirely new attack. If the attacking player decides not to attack, that monster may attack again later.",
            subSections = listOf(
                RuleSubSection("Example", "Player A pays 1000 LP and attacks directly with \"Dark Elf\". Player B activates \"Call of the Haunted\", Special Summoning \"Sangan\". A replay occurs — Player A must **pay another 1000 LP** to attack with \"Dark Elf\" again."),
            ),
        ),
        FormatRule(
            "Continuous Traps with Ignition-Like Effects",
            "**Continuous Traps with Ignition-like effects cannot be flipped face-up and use their Ignition-like effects at the same time.** They must be flipped face-up \"pre-emptively.\"",
            subSections = listOf(
                RuleSubSection("Example 1", "Player A activates \"Scapegoat\" while Player B controls a face-down \"Royal Oppression\". If Player B flips \"Royal Oppression\" as a Chain to \"Scapegoat\", he **cannot** also negate the activation. \"Royal Oppression\" must already be face-up before \"Scapegoat\" is activated."),
                RuleSubSection("Example 2", "Player A Tribute Summons \"Mobius the Frost Monarch\" targeting Player B's face-down \"Skull Lair\". Player B can flip \"Skull Lair\" as a Chain, but **cannot** use its Ignition-like effect on the same Chain."),
            ),
        ),
        FormatRule(
            "Verifying Hands and Decks",
            "**Effects that require hands or decks to be revealed must always be carried out**, even if knowledge of the game's rules or Forbidden/Limited List already provides enough information to verify correct resolution.",
            subSections = listOf(
                RuleSubSection("Example 1", "Player A activates \"Mind Crush\", naming \"D.D. Warrior Lady\". Player B has a copy in his Graveyard. Even though it's Limited to 1, Player B **must still reveal his hand**."),
                RuleSubSection("Example 2", "\"Nobleman of Extermination\" banishes \"Sakuretsu Armor\". Both players banish all copies from their Decks. They still must **reveal their decks** to verify correct resolution."),
                RuleSubSection("Note", "When playing online, it's standard practice to not verify decks/hands for cards like \"Nobleman of Crossout\" and \"Mind Crush\". Instead, use replays at match's end to verify."),
            ),
        ),
        FormatRule(
            "Failure to Find",
            "**Cards that search the Deck can be used without any legal options.** One notable exception: \"The Agent of Creation - Venus\" still requires \"Mystical Shine Balls\" in the Deck to activate.",
            subSections = listOf(
                RuleSubSection("Example 1", "Player A discards \"Thunder Dragon\" to search their Deck for more copies, even though they know none remain. The Deck is still shuffled."),
                RuleSubSection("Example 2", "Player A activates \"Reinforcement of the Army\" and finds no Warrior monsters. The Deck is shuffled and RoTA goes to the Graveyard."),
            ),
        ),
        FormatRule(
            "SEGOC (Simultaneous Effects Go On Chain)",
            "SEGOC works almost exactly like modern Yu-Gi-Oh!, **with one exception: when two effects were triggered at different times, the earlier trigger(s) must be placed on the Chain before the later ones.**",
            subSections = listOf(
                RuleSubSection("Step 1", "Turn Player's Mandatory"),
                RuleSubSection("Step 2", "Non-Turn Player's Mandatory"),
                RuleSubSection("Step 3", "Turn Player's Optional"),
                RuleSubSection("Step 4", "Non-Turn Player's Optional"),
                RuleSubSection("Example", "Player A Tributes \"Sangan\" to Tribute Summon \"Thestalos the Firestorm Monarch\". \"Sangan\"'s mandatory effect triggered first → Chain Link 1. \"Thestalos\"'s mandatory effect triggered second → Chain Link 2."),
            ),
        ),
        FormatRule(
            "The Damage Step",
            "The Damage Step has three key differences from modern Yu-Gi-Oh!:",
            subSections = listOf(
                RuleSubSection("10A: Six Substeps (not five)", "**1 Start** — Some Trigger Effects activate here **before** the attack target is flipped. Examples: \"Ninja Grandmaster Sasuke\", \"Mystic Swordsman LV2\". These resolve \"without flipping the target face-up or applying damage calculation.\" Monster negation effects and Counter Traps can activate.\n\n**2 Before Calc** — Attack target is flipped face-up at the start of this substep. Continuous Effects of the target now apply. ATK/DEF modifiers can activate. Examples: \"Injection Fairy Lily\", \"Rush Recklessly\", \"Mirror Wall\". Monster negation effects and Counter Traps can activate.\n\n**3 Damage Calc** — ATK/DEF values are compared and battle damage is determined. **Only one Chain** can be built here. Monster negation effects and Counter Traps can activate.\n\n**4 After Calc** — Effects triggered by battle damage activate. \"Flip Effects\" activate here. Monster negation effects and Counter Traps can activate.\n\n**5 Resolve** — Monsters that were determined to be destroyed by battle are confirmed. Effects like \"D.D. Warrior Lady\" resolve. Monster negation effects and Counter Traps can activate.\n\n**6 End** — Monsters destroyed by battle are **sent to the Graveyard**. Effects triggered by destruction by battle activate: \"Mystic Tomato\", \"Sangan\", \"Last Will\". Monster negation effects and Counter Traps can activate."),
                RuleSubSection("10B: One Chain in Before Damage Calculation", "In current Yu-Gi-Oh!, unlimited Chains can be activated during \"before damage calculation.\" **In Goat Format, only one Chain can be built** in the \"before damage calculation\" substep (substep 2). After that Chain resolves, the Damage Step moves on."),
                RuleSubSection("10C: No Spell/Trap Negation in Damage Step — Monster Effects Only", "In current Yu-Gi-Oh!, Spell Speed 2 effects that negate activations can be used in the Damage Step. **In Goat Format, only monster effects that negate activations can be used** — not Quick-Play Spells, Normal Traps, or Continuous Trap effects."),
                RuleSubSection("Example", "Player A attacks with \"Battle Ox\" into Player B's set \"Man-Eater Bug\". When \"Man-Eater Bug\"'s Flip Effect activates after damage calculation, Player A **cannot** activate \"My Body as a Shield\" in response."),
            ),
        ),
        FormatRule(
            "The Match Draw is a Lie",
            "Although the rulebook stated a match could end in a draw (1 win + 1 loss + 1 draw = 1 match draw), this was not applied at tournaments — it was literally impossible to enter a match draw into the tournament software. **Matches must always be played until one player has won 2 duels.**\n\nIn timed rounds that reach end-of-match procedures: after determining the winner of the current duel, a player wins the match if they have more game wins than their opponent.",
        ),
        FormatRule(
            "Dude, Where's My Trigger?",
            "**In Goat Format, triggers can be recognized mid-Chain.** Moving monsters to a different location to prevent them from triggering will not work. Also, unlike modern Yu-Gi-Oh!, **Trigger Effects can be met while the monster is in the Deck.**",
            subSections = listOf(
                RuleSubSection("Example 1", "Player A activates \"Compulsory Evacuation Device\" targeting Player B's set \"Magician of Faith\". Player B Chains \"Desert Sunlight\". \"Magician of Faith\" flips up (trigger recognized), then is returned to hand. Its Flip Effect still activates on a new Chain."),
                RuleSubSection("Example 2", "Player A activates \"A Feather of the Phoenix\", discarding \"Night Assailant\" and targeting that same \"Night Assailant\" to place on top of the Deck. \"Night Assailant\"'s effect still triggers (it was recognized in the GY mid-Chain)."),
            ),
        ),
        FormatRule(
            "Monsters Equipping Monsters",
            "In Goat Format, if \"Relinquished\" or \"Thousand-Eyes Restrict\" tries to equip an opponent's monster, and an effect is Chained that removes it from the field or flips it face-down, **the opponent keeps their monster** (it is NOT sent to the GY). In modern Yu-Gi-Oh!, the monster would be sent to the GY.",
            subSections = listOf(
                RuleSubSection("Example", "Player A activates \"Thousand-Eyes Restrict\"'s effect targeting Player B's \"Dark Magician\". Player B Chains \"Book of Moon\" on TER. TER is flipped face-down → its effect resolves without effect → Player B **keeps** \"Dark Magician\"."),
            ),
        ),
        FormatRule(
            "A Cure for the Summoning Sickness",
            "In modern Yu-Gi-Oh!, a monster's battle position can never change the turn it was Summoned. **In Goat Format, if you take control of an opponent's monster, you can always change its battle position** even if it was just Summoned that turn. The same applies to monsters Special Summoned to the turn player's side by the opponent.",
            subSections = listOf(
                RuleSubSection("Example", "Player B activates \"Scapegoat\" during Player A's turn. Player A activates \"Snatch Steal\" on a Sheep Token and switches it to Attack Position."),
            ),
        ),
        FormatRule(
            "The Other Guys (0 ATK Battles)",
            "In modern Yu-Gi-Oh!, when two Attack Position monsters with 0 ATK battle each other, neither is destroyed. **In Goat Format, both monsters are destroyed in battle.** This comes up very rarely.",
        ),
        FormatRule(
            "Killing Yourself to Live (LP Costs)",
            "In modern Yu-Gi-Oh!, you can pay LP costs that reduce you to exactly 0 LP (and immediately lose). **In Goat Format, you must have more LP than the cost requires — you cannot pay a cost that would reduce you to 0.**\n\nIf a card has a mandatory LP maintenance cost and you don't have enough LP, the card is simply destroyed.",
        ),
        FormatRule(
            "Infinity Wars (Infinite Loops)",
            "In modern Yu-Gi-Oh!, when an infinite loop is created, a \"primary cause\" is determined and that card is sent to the GY. **In Goat Format, this only applies to involuntary loops** (initiated by game mechanics). A **voluntary action that would cause an infinite loop is simply illegal.**",
            subSections = listOf(
                RuleSubSection("Example", "Player A has \"Gemini Elf\", \"Luminous Spark\", and \"Pole Position\". \"Gemini Elf\" has the highest ATK → unaffected by Spells → \"Luminous Spark\" doesn't boost it → it no longer has the highest ATK → loop. This is involuntary → \"Pole Position\" (primary cause) is destroyed by game mechanics."),
            ),
        ),
        FormatRule(
            "State of the Unions",
            "In modern Yu-Gi-Oh!, multiple Union Monsters can be equipped to the same target. **In Goat Format, a monster can only be equipped with 1 Union Monster at a time.**\n\nAdditionally, most Union Monsters in Goat Format can prevent destruction **by battle only, not by card effects**, even where modern errata would suggest otherwise. The exception is \"Heavy Mech Support Platform\", which could always prevent either type of destruction.",
        ),
        FormatRule(
            "The End of the Turn (Discard Phase)",
            "In modern Yu-Gi-Oh!, players generally can't respond to the end-of-turn discard. **In Goat Format, players may simply respond to the end-of-turn discard in whatever ways they wish.**",
            subSections = listOf(
                RuleSubSection("Example 1", "Player A has 7 cards in hand during their End Phase. Player A discards \"Thunder Dragon\". Player A then activates their set \"Forced Requisition\". Player B chains \"Raigeki Break\", discarding \"Night Assailant\" for cost."),
                RuleSubSection("Example 2", "Player B has 7 cards and discards \"Manticore of Darkness\". Player B can now activate \"Manticore\"'s effect from the GY."),
            ),
        ),
        FormatRule(
            "It's a Trap Monster",
            "This applies to exactly one card in Goat Format: \"Embodiment of Apophis\". In modern Yu-Gi-Oh!, Trap Monsters Special Summon themselves to a Monster Zone and no longer occupy a Spell/Trap Zone. **In Goat Format, \"Embodiment of Apophis\" occupies both a Monster Zone and a Spell/Trap Zone simultaneously.**",
        ),
        FormatRule(
            "Last Turn — Complete Rulings",
            "**Semi-Limited in Goat Format. One of the most complex cards in the game.**\n\nWhen \"Last Turn\" (the Trap Card) resolves: the activating player selects 1 monster on their side of the field, and **all other cards on the field and in both players' hands are sent to the Graveyard.** That is all that happens when it resolves.\n\nAfter resolution: the turn player Special Summons 1 monster from their Deck. Then a **special Battle Phase** occurs. Then, during the End Phase, the **victory check** is applied.",
            isSpecial = true,
            subSections = listOf(
                RuleSubSection("Activation Condition", "**Activation condition:** Your Life Points must be 1000 or less. You can activate \"Last Turn\" in any part of your opponent's turn, except the Damage Step — even during the End Phase."),
                RuleSubSection("Card Interactions", "• \"Last Turn\" does not target — you can select monsters like \"Tyrant Dragon\", \"Spirit Reaper\", or Dragon-Types while \"Lord of D.\" is on the field. The selected monster can be face-up or face-down, in Attack or Defense Position.\n• \"Last Turn\" is not a card that Special Summons when it resolves. You can activate it while \"Jowgen the Spiritualist\" is face-up. If you select \"Jowgen\", the opponent cannot Special Summon, no special Battle Phase occurs, but the victory check still applies.\n• The **special Battle Phase** is a standard Battle Phase (Start Step, Battle Step, Damage Step, End Step). Only the selected monster and the Special Summoned monster can battle. It happens right after the Special Summon and does not disrupt the normal order of the turn. **The turn player can still have a normal Battle Phase later.**\n• Battle Damage during the special Battle Phase is **reduced to zero**. This does NOT apply to the normal Battle Phase later in the turn.\n• You **cannot** Special Summon a \"Special Summon-only\" monster (\"Chaos Emperor Dragon\", \"Dark Necrofear\") or a Spirit Monster. You **can** Special Summon high-level monsters with no restrictions (\"Blue-Eyes White Dragon\", \"Dark Magician of Chaos\").\n• If the turn player Special Summons \"Dark Magician of Chaos\", they can activate his effect to add a Spell Card from the Graveyard to their hand.\n• The **victory check** in the End Phase: the player who activated \"Last Turn\" resolves this effect. The turn player has priority to resolve other effects first. You cannot chain to the victory check.\n• If your LP increase above 1000 after activation, the effects of \"Last Turn\" still apply.\n• If a player's LP are reduced to zero during the special Battle Phase or afterward, that player loses normally (e.g. \"Giant Germ\" effect damage)."),
                RuleSubSection("Key Interactions", "▸ **\"Thousand-Eyes Restrict\"**: If selected, the opponent cannot attack. Unless they can destroy it, each player has a monster → **DRAW**.\n\n▸ **\"Dark Necrofear\"**: If destroyed in the special Battle Phase, during the End Phase you choose which resolves first. Resolve \"Dark Necrofear\" first → equip to opponent's monster → resolve \"Last Turn\" → only you control a monster → **WIN**.\n\n▸ **\"Mystic Tomato\"**: If Special Summoned by the turn player and destroyed, its effect activates and a new monster is Special Summoned — but that monster **cannot attack** during the special Battle Phase (can attack in the normal Battle Phase).\n\n▸ **\"Sangan\"**: If destroyed, its effect resolves. The turn player can Normal Summon the retrieved monster and attack in the normal Battle Phase → possible **DRAW**.\n\n▸ **\"Call of the Haunted\"**: If your selected monster was Summoned by \"Call of the Haunted\", when CotH is sent to GY by \"Last Turn\", your monster is **destroyed**. But \"Premature Burial\" does **not** destroy the selected monster.\n\n▸ **\"Waboku\"**: Can be activated before or chained to \"Last Turn\" — prevents destruction → likely **DRAW**.\n\n▸ **\"D.D. Warrior Lady\"**: If both monsters are removed from play → **DRAW**."),
            ),
        ),
    )

    val edisonRules = listOf(
        FormatRule(
            "The Starting Player Draws a Card",
            "In Edison Format, a card is drawn during the first Draw Phase. **The player who goes first starts with 6 cards**.",
        ),
        FormatRule(
            "Only 1 Active Field Spell",
            "**Only one Field Spell can be active at a time.** Setting a Field Spell does not destroy an opponent's active Field Spell.",
            subSections = listOf(
                RuleSubSection("Examples", "• Activating \"Mountain\" while \"Umi\" is active destroys \"Umi\" on resolution.\n• Setting \"Mausoleum\" over \"Geartown\" destroys it by game mechanic without starting a Chain — \"Geartown\" CAN activate.\n• Activating \"Mausoleum\" over \"Geartown\" starts a Chain — \"Geartown\" CANNOT activate."),
            ),
        ),
        FormatRule(
            "Union Monster Conditions",
            "**\"A monster can only be equipped with 1 Union Monster at a time.\"**\n\n\"If the equipped monster would be destroyed, destroy this card instead\" only appears on: Armored Cybern, Oilman, Heavy Mech Support Platform, Spirit of the Six Samurai, Machina Gearframe, Machina Peacekeeper.",
        ),
        FormatRule(
            "Phase-Dependent Mandatory Trigger Effects",
            "**Any mandatory Trigger Effect that activates during a certain Phase will keep activating if its activation is negated, until it finally resolves.**",
            subSections = listOf(
                RuleSubSection("Details", "• **Activation** negated (by \"Light and Darkness Dragon\") → effect activates again.\n• **Effect** negated (by \"Skill Drain\") → does NOT activate again."),
            ),
        ),
        FormatRule(
            "Trap Monster Zone Blocking",
            "**Trap Monsters take up 1 Monster Zone and 1 Spell/Trap Zone.**\n\n• Cannot activate if no empty Monster Zones.\n• Negated by \"Jinzo\" → reverts to Trap Card immediately.\n• \"Heavy Storm\" vs Trap Monster: \"Fake Trap\" protects, \"MBAS\" does not.\n• \"Lightning Vortex\" vs Trap Monster: \"MBAS\" protects, \"Fake Trap\" does not.\n• Removed from field = removed as Trap Card.",
        ),
        FormatRule(
            "Ignition Effect Priority",
            "The Turn Player can activate an Ignition Effect as CL1 in the Summon response timing, **if the Summon did not start a Chain**.",
            subSections = listOf(
                RuleSubSection("Has Priority", "Summon \"Chaos Sorcerer\" → activate its effect before \"Bottomless Trap Hole\"."),
                RuleSubSection("No Priority", "Synchro Summon \"Brionac\" using \"Sangan\" → \"Sangan\" starts a Chain, cannot use \"Brionac\"'s Ignition Effect."),
            ),
        ),
        FormatRule(
            "SEGOC (Simultaneous Effects Go On Chain)",
            "Earlier triggers must be placed on chain before later ones in the same step.",
            subSections = listOf(
                RuleSubSection("Step 1", "Turn Player's Mandatory"),
                RuleSubSection("Step 2", "Non-Turn Player's Mandatory"),
                RuleSubSection("Step 3", "Turn Player's Optional"),
                RuleSubSection("Step 4", "Non-Turn Player's Optional"),
                RuleSubSection("Key Example", "Tribute \"Sangan\" for \"Caius\": Sangan triggers first → CL1. Caius on Summon → CL2."),
            ),
        ),
        FormatRule(
            "The 7-Timing Damage Step",
            "**The Damage Step has seven distinct sub-steps.** At any sub-step, **Counter Traps**, **mandatory effects**, and **monster effects that negate activations** can always be activated. **Spell Speed 2 Spell/Trap Cards that negate activations CANNOT be activated during the Damage Step.**",
            subSections = listOf(
                RuleSubSection("1 Start", "Effects that state \"during the Damage Step\" are now applied. Effects that trigger at the start of the Damage Step now activate. **Quick Effects and Spells/Traps that affect ATK/DEF may be activated.**"),
                RuleSubSection("2 Flip Face-Up", "If the attack target is Set, flip it face-up. **Flip Effects are NOT activated yet.** Effects that affect the attack target (if it was Set) now start applying. **Quick Effects and Spells/Traps that affect ATK/DEF may be activated.**"),
                RuleSubSection("3 Before Calc", "Effects that state \"before Damage Calculation\" activate/apply now. **Quick Effects and Spells/Traps that affect ATK/DEF may be activated.**\n\nExamples: \"Injection Fairy Lily\", \"Honest\"."),
                RuleSubSection("4 Damage Calc", "Effects that specifically state \"Damage Calculation\" activate/apply now. Monster effects that can be activated during the Damage Step may activate. **Quick Effects and Spells/Traps that affect ATK/DEF may NOT be activated.** There can only be 1 manual chain in this substep."),
                RuleSubSection("5 After Calc", "Calculate damage and determine if monsters should be destroyed (they are NOT yet sent to GY). Afterwards, apply effects that do not affect ATK/DEF of a monster (e.g. \"Berserk Gorilla\"). Effects that state \"if this card inflicts battle damage\" activate now. **Quick Effects and Spells/Traps that affect ATK/DEF may NOT be activated.** There can only be 1 manual chain in this substep.\n\nBattle damage triggers like \"Gorz the Emissary of Darkness\" and \"Tragoedia\" activate here."),
                RuleSubSection("6 Resolve", "Monsters marked as \"destroyed by battle\" no longer apply their continuous effects. Effects that specifically state \"after Damage Calculation\" activate now. **Flip Effects also activate**, forming a chain with any other effects activating now. **Quick Effects and Spells/Traps that affect ATK/DEF may NOT be activated.** There can only be 1 manual chain in this substep."),
                RuleSubSection("7 End", "Monsters destroyed by battle are now **sent to the GY**. Effects that activate when a monster is destroyed by battle now activate (on 1 chain). **Quick Effects and Spells/Traps that affect ATK/DEF may NOT be activated.** There can only be 1 manual chain in this substep.\n\n\"Mystic Tomato\", \"Sangan\", \"Giant Rat\" trigger here."),
                RuleSubSection("Key Restriction", "Spell/Trap ATK/DEF modifiers can only activate in Substeps 1–3. From Substep 4 onward, only monster Quick Effects (in Substep 4 only) and Counter Traps/mandatory/negation effects are permitted."),
            ),
        ),
        FormatRule(
            "Trigger Location & Recognition",
            "**Triggers recognized mid-chain.** Effects activate from outside their trigger location, even from the Deck.",
            subSections = listOf(
                RuleSubSection("Example", "Discard \"Dandylion\" for \"Monster Reincarnation\", add it back → Fluff Tokens still trigger from GY."),
            ),
        ),
        FormatRule(
            "Life Points Costs",
            "**Cannot pay costs that reduce LP to 0.** Maintenance costs self-destruct instead.",
        ),
        FormatRule(
            "End-of-Turn Discard",
            "**Cannot respond to end-of-turn hand size discard.** Only SS2 negation effects and SS3 can chain to mandatory triggers from this discard.",
        ),
        FormatRule(
            "Infinite Loops",
            "**Voluntary infinite loops are illegal.** Involuntary loops → primary cause destroyed by game mechanics.",
        ),
        FormatRule(
            "0 ATK Monsters",
            "Two ATK-position 0 ATK monsters **destroy each other**. A 0 ATK monster **cannot destroy** a DEF-position 0 DEF monster.",
        ),
    )

    val tenguRules = listOf(
        FormatRule(
            "Starting Player Draws a Card",
            "**The player who goes first draws a card during their Draw Phase.** This means the starting player begins their first turn with 6 cards in hand. This rule was changed on July 10, 2014.",
        ),
        FormatRule(
            "Only 1 Active Field Spell",
            "**Only 1 Field Spell Card can be face-up at a time.** If a new Field Spell resolves while another is face-up, the previous one is destroyed.\n\n• Setting a Field Spell does **not** destroy the opponent's face-up Field Spell.\n• A Field Spell must remain face-up to resolve its activation effect.",
        ),
        FormatRule(
            "Union Monster Conditions",
            "**A monster can only be equipped with 1 Union Monster at a time.** Union Monsters have the old Condition text: *\"If the equipped monster would be destroyed by battle, destroy this card instead.\"*",
        ),
        FormatRule(
            "Ignition Effect Priority",
            "**After successfully Summoning a monster, the turn player has Priority to activate that monster's Ignition Effect before the opponent can respond.** This rule was removed on April 25, 2012.",
            subSections = listOf(
                RuleSubSection("Example", "Player A Normal Summons \"Elemental HERO Stratos\". Before Player B can activate \"Bottomless Trap Hole\", Player A has Priority to activate \"Stratos\"'s search/pop effect."),
                RuleSubSection("Note", "Priority only applies to Ignition Effects, NOT Quick Effects or Trigger Effects. After the Ignition Effect resolves (or the turn player passes Priority), the opponent can respond to the Summon."),
            ),
        ),
        FormatRule(
            "SEGOC (Simultaneous Effects Go On Chain)",
            "Same as Edison: **if two or more effects in the same step were triggered at different times, the earlier trigger(s) must be placed on the Chain first.**",
            subSections = listOf(
                RuleSubSection("Step 1", "Turn Player's Mandatory (in trigger order)"),
                RuleSubSection("Step 2", "Non-Turn Player's Mandatory"),
                RuleSubSection("Step 3", "Turn Player's Optional"),
                RuleSubSection("Step 4", "Non-Turn Player's Optional"),
            ),
        ),
        FormatRule(
            "The 7-Timing Damage Step",
            "**The Damage Step has 7 distinct timings.** Counter Traps, mandatory effects, and monster effects that negate activations can always activate. SS2 Spells/Traps that negate activations **CANNOT** activate.\n\n**Key Restriction:** Spell/Trap ATK/DEF modifiers CANNOT activate in or after Substep 4 (Damage Calculation).",
            subSections = listOf(
                RuleSubSection("1 Start", "Quick Effects & Spells/Traps that modify ATK/DEF can activate."),
                RuleSubSection("2 Flip", "Face-down attack target is flipped. Flip Effects NOT yet activated."),
                RuleSubSection("3 Before Calc", "\"Before Damage Calculation\" effects. Last chance for ATK/DEF Spells/Traps. \"Honest\", \"Injection Fairy Lily\"."),
                RuleSubSection("4 Damage Calc", "Only monster Quick Effects for ATK/DEF. Battle damage inflicted; monsters marked destroyed."),
                RuleSubSection("5 After Calc", "Destroyed monsters lose continuous effects. Battle damage triggers (\"Gorz\", \"Tragoedia\")."),
                RuleSubSection("6 Resolve", "Flip Effects activate. \"After Damage Calculation\" effects resolve."),
                RuleSubSection("7 End", "Destroyed monsters sent to GY. Recruiters trigger: \"Sangan\", \"Mystic Tomato\"."),
            ),
        ),
        FormatRule(
            "Life Points Costs",
            "**Cannot pay costs that would reduce LP to 0.** Maintenance cost cards self-destruct instead.",
        ),
        FormatRule(
            "End-of-Turn Discard",
            "**Players cannot respond to the end-of-turn discard.** Mandatory Trigger Effects from discarded cards build a special Chain. Optional Trigger Effects **cannot** activate.",
        ),
        FormatRule(
            "Xyz Mechanics",
            "**Tengu Format introduces Xyz Monsters.** Key interactions that are often misplayed:\n\n• Xyz Materials are NOT on the field. Effects that say \"on the field\" do not apply to them. \"Macro Cosmos\" does NOT banish detached Xyz Materials.\n• Xyz Summoning does NOT count as \"Tributing.\" Cards like \"Soul Exchange\" cannot be used for Xyz Summons.\n• \"Reborn Tengu\" used as Xyz Material does NOT trigger — it was not \"sent from the field\" in the usual sense (it became Xyz Material, then detached to GY).\n• If the activation of a phase-dependent mandatory Trigger Effect is negated, it will activate again during that same phase.",
        ),
        FormatRule(
            "Infinite Loops",
            "**Voluntary actions that would cause an infinite loop are illegal.** Involuntary loops → primary cause is destroyed by game mechanics.",
        ),
    )

    val reduRules = listOf(
        FormatRule(
            "Starting Player Draws a Card",
            "**The player who goes first draws a card during their Draw Phase** — starting with 6 cards in hand.",
        ),
        FormatRule(
            "Only 1 Active Field Spell",
            "**Only 1 Field Spell Card can be face-up at a time.** If a new Field Spell resolves while another is face-up, the previous one is destroyed.",
        ),
        FormatRule(
            "Union Monster Conditions",
            "**A monster can only be equipped with 1 Union Monster at a time.**",
        ),
        FormatRule(
            "No Ignition Effect Priority",
            "**Ignition Effect Priority was removed on April 25, 2012.** Unlike Tengu Format, in REDU Format the opponent can respond to a Summon before the turn player can activate Ignition Effects.",
            subSections = listOf(
                RuleSubSection("Example", "Player A Normal Summons \"Elemental HERO Stratos\". Player B can immediately activate \"Bottomless Trap Hole\" — Player A does NOT get Priority to activate \"Stratos\"'s search effect first."),
                RuleSubSection("Note", "Trigger Effects still activate normally upon Summon. Only Ignition (Spell Speed 1) Effects lost Priority."),
            ),
        ),
        FormatRule(
            "SEGOC",
            "**Same ordering as other retro formats:** earlier triggers are placed on the Chain first when multiple effects trigger simultaneously.",
        ),
        FormatRule(
            "The 7-Timing Damage Step",
            "**Same 7 substeps as other retro formats.** Counter Traps, mandatory effects, and monster negation always allowed. SS2 Spell/Trap negation CANNOT activate.",
        ),
        FormatRule(
            "Life Points Costs",
            "**Cannot pay costs that would reduce LP to 0.**",
        ),
        FormatRule(
            "End-of-Turn Discard",
            "**Players cannot respond to the end-of-turn discard.** Optional Trigger Effects **cannot** activate from discarded cards.",
        ),
        FormatRule(
            "Xyz Mechanics",
            "**Same as Tengu Format.** Xyz Materials are NOT on the field. Xyz Summoning is NOT Tributing. Phase-dependent mandatory Trigger Effects re-activate if negated.",
        ),
        FormatRule(
            "Infinite Loops",
            "**Voluntary infinite loops are illegal.** Involuntary loops → primary cause is destroyed.",
        ),
    )

    val hatRules = listOf(
        FormatRule(
            "The Starting Player Draws a Card",
            "In HAT Format, a card is drawn during the first Draw Phase. **The player who goes first starts the duel with 6 cards.**",
        ),
        FormatRule(
            "Only 1 Active Field Spell",
            "**Only one Field Spell may remain active at a time.** Both players may Set Field Spells — doing so does not destroy the opponent's. If you Set a Field Spell over your own Set/Active Field Spell, that card is destroyed immediately without starting a Chain. A Field Spell must remain face-up to resolve.",
            subSections = listOf(
                RuleSubSection("Example 1", "Player A activates \"Mountain\" while Player B's \"Umi\" is active. When \"Mountain\" resolves, \"Umi\" is destroyed by game mechanic."),
                RuleSubSection("Example 2", "Player A activates \"Mountain\". Player B chains \"Dust Tornado\" targeting \"Mountain\". \"Dust Tornado\" destroys \"Mountain\" → \"Umi\" is NOT destroyed."),
                RuleSubSection("Example 3", "Player A Sets \"Mountain\" while \"Umi\" is active. \"Umi\" is NOT destroyed."),
            ),
        ),
        FormatRule(
            "Union Monster Conditions",
            "**A monster can only be equipped with 1 Union Monster at a time.** Union Monsters in HAT Format have the old Condition text: *\"If the equipped monster would be destroyed by battle, destroy this card instead.\"*\n\nThis Condition was removed in 2016 with Structure Deck: Seto Kaiba. Check individual card rulings for any Union Monsters you play.",
        ),
        FormatRule(
            "Trap Monsters",
            "**Trap Monsters occupy BOTH a Trap Card Zone AND a Monster Card Zone.**\n\n• Cannot activate a Trap Monster if no empty Monster Card Zones.\n• To gain control of one, you need both an open Monster Zone and an open S/T Zone.\n• Negated by \"Jinzo\" → reverts to Trap Card immediately. A Trap Monster that attacks a Set \"Jinzo\" reverts when \"Jinzo\" flips — Damage Calculation is NOT applied.\n• Flipped face-down by \"Book of Moon\" → becomes Set in its Spell/Trap Zone.\n• \"Heavy Storm\" vs Trap Monster: \"Fake Trap\" protects, \"My Body as a Shield\" does NOT.\n• \"Lightning Vortex\" vs Trap Monster: \"My Body as a Shield\" protects, \"Fake Trap\" does NOT.\n• \"Raigeki Break\" vs Trap Monster: Both can protect.\n• Removed from field → removed as a Trap Card. Cannot revive with \"Time Machine\", no \"Michizure\" trigger.",
        ),
        FormatRule(
            "SEGOC (Simultaneous Effects Go On Chain)",
            "SEGOC works with one exception vs modern rules: **if two or more effects in the same step were triggered at different times, the earlier trigger(s) must be placed on the Chain first.** This was changed in 2017.",
            subSections = listOf(
                RuleSubSection("Step 1", "Turn Player's Mandatory (in trigger order)"),
                RuleSubSection("Step 2", "Non-Turn Player's Mandatory"),
                RuleSubSection("Step 3", "Turn Player's Optional"),
                RuleSubSection("Step 4", "Non-Turn Player's Optional"),
                RuleSubSection("Example 1", "Player A Tributes \"Sangan\" to Summon \"Caius the Shadow Monarch\". \"Sangan\" triggers when sent to GY → Chain Link 1. \"Caius\" triggers on Summon → Chain Link 2."),
                RuleSubSection("Example 2", "Player A uses \"Soul Exchange\" to Tribute Player B's \"Sangan\" for \"Caius\". Turn player's \"Caius\" (Step 1) → CL1. Non-turn player's \"Sangan\" (Step 2) → CL2."),
            ),
        ),
        FormatRule(
            "The 7-Timing Damage Step",
            "**The Damage Step has 7 distinct timings.** At any point: Counter Traps, mandatory effects, and monster effects that negate activations can activate. SS2 Spells/Traps that negate activations **CANNOT** activate in the Damage Step.\n\n**Key Restriction:** Spell/Trap ATK/DEF modifiers CANNOT activate in or after Substep 4 (Damage Calculation).",
            subSections = listOf(
                RuleSubSection("1 Start", "\"During the Damage Step\" effects apply. Quick Effects & Spells/Traps that modify ATK/DEF can activate."),
                RuleSubSection("2 Flip Face-Up", "Face-down attack target is flipped. **Flip Effects NOT activated yet.** Effects affecting the target now apply. ATK/DEF modifiers can still activate."),
                RuleSubSection("3 Before Calc", "\"Before Damage Calculation\" effects activate. Last chance for ATK/DEF Spells/Traps. Examples: \"Injection Fairy Lily\", \"Honest\"."),
                RuleSubSection("4 Damage Calc", "**One manual Chain only.** Only monster Quick Effects that modify ATK/DEF can activate. **Spell/Trap ATK/DEF modifiers CANNOT activate.** Battle damage is inflicted; monsters marked \"destroyed by battle.\""),
                RuleSubSection("5 After Calc", "Destroyed monsters no longer apply continuous effects. Self-destruct effects apply (\"Berserk Gorilla\", \"Gozen Match\"). Battle damage triggers activate (\"Gorz\")."),
                RuleSubSection("6 Resolve", "**Flip Effects activate.** \"After Damage Calculation\" effects resolve. Additional chains built if effects trigger during resolution (e.g., \"Ryko\" mills \"Peten the Dark Clown\")."),
                RuleSubSection("7 End", "Monsters destroyed by battle sent to GY. \"Destroyed by battle\" triggers activate: \"Mystic Tomato\", \"Sangan\". Additional chains if recruiter summons trigger more effects."),
            ),
        ),
        FormatRule(
            "Life Points Costs",
            "**Cannot pay costs that would reduce LP to 0.** Maintenance cost cards self-destruct instead.",
            subSections = listOf(
                RuleSubSection("Example 1", "LP ≤ 2000: Cannot pay \"Mirror Wall\"'s optional maintenance cost → self-destructs."),
                RuleSubSection("Example 2", "LP ≤ 500: Cannot pay \"Degenerate Circuit\"'s mandatory cost → self-destructs."),
            ),
        ),
        FormatRule(
            "End-of-Turn Discard",
            "**Players cannot respond to the end-of-turn discard.** It must be the last action in a turn. However, if a mandatory Trigger Effect is triggered by the discard, a special Chain is built:\n\n• SS2 effects can chain **only if they negate activations or effects**.\n• SS3 (Counter Traps) can chain regardless.\n• Optional Trigger Effects from discarded cards **cannot** activate.",
            subSections = listOf(
                RuleSubSection("Example 1", "Player A discards \"The White Stone of Legend\" for hand size. Its mandatory Trigger activates. Player A **cannot** chain \"Super Rejuvenation\". Player B **can** chain \"Divine Wrath\"."),
                RuleSubSection("Example 2", "Player A discards \"Peten the Dark Clown\" and \"Red-Eyes Wyvern\" for hand size. Player A **cannot** activate their optional Trigger Effects."),
            ),
        ),
        FormatRule(
            "Infinite Loops",
            "**Voluntary actions that would cause an infinite loop are illegal.** Involuntary loops (game mechanics) → primary cause is destroyed.",
            subSections = listOf(
                RuleSubSection("Voluntary (Illegal)", "Player A has \"Gemini Elf\", \"Luminous Spark\", and \"Pole Position\". Player B cannot Summon \"X-Head Cannon\" because \"Luminous Spark\" would create an infinite ATK loop."),
                RuleSubSection("Involuntary (Game Mechanic)", "Player B draws a card, raising \"Muka Muka\"'s ATK above \"Opticlops\" + \"Axe of Despair\" with \"Pole Position\" active → unavoidable loop → \"Pole Position\" destroyed by game mechanics."),
            ),
        ),
    )

    val allFormats: List<FormatMeta> = listOf(
        FormatMeta("Goat", "Goat", "Apr. 2005", enabledByDefault = true, rules = goatRules),
        FormatMeta("Stein", "Stein", "Sep. 2006"),
        FormatMeta("Trooper", "Trooper", "Jun. 2007"),
        FormatMeta("Circle", "Circle", "Sep. 2007"),
        FormatMeta("ReturnDAD", "ReturnDAD", "Mar. 2008"),
        FormatMeta("Gladiator", "Gladiator", "May 2008"),
        FormatMeta("TeleDAD", "TeleDAD", "Sep. 2008"),
        FormatMeta("Cat", "Cat", "Mar. 2009"),
        FormatMeta("Edison", "Edison", "Mar. 2010", enabledByDefault = true, rules = edisonRules),
        FormatMeta("Tengu", "Tengu", "Sep. 2011", enabledByDefault = true, rules = tenguRules),
        FormatMeta("Meadowlands", "Meadowlands", "Mar. 2013"),
        FormatMeta("FireWater", "Fire/Water", "Jan. 2014"),
        FormatMeta("Vegas", "Vegas", "Apr. 2014"),
        FormatMeta("HAT", "HAT", "Apr. 2014", enabledByDefault = true, rules = hatRules),
        FormatMeta("Base", "Modern", "Current", enabledByDefault = true),
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
