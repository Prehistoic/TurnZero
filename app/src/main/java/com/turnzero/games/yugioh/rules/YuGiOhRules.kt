package com.turnzero.games.yugioh.rules

import com.turnzero.core.ui.components.ContentBlock
import com.turnzero.tools.rules.Keyword
import com.turnzero.tools.rules.PdfAsset
import com.turnzero.tools.rules.RuleStep
import com.turnzero.tools.rules.RulesConfig
import com.turnzero.tools.rules.InfoCard
import com.turnzero.tools.rules.bl
import com.turnzero.tools.rules.img
import com.turnzero.tools.rules.p
import com.turnzero.tools.rules.sh

object YuGiOhRules {

    private val victory = listOf(
        InfoCard("Reduce LP to 0", listOf(p("Each player starts with 8000 Life Points. Reduce your opponent's LP to 0 through battle damage and effect damage to win."))),
        InfoCard("Deck Out", listOf(p("If a player is unable to draw a card from their Main Deck when required, they lose the Duel."))),
        InfoCard("Alternative Win Conditions", listOf(p("Some cards have effects that declare an automatic win when their conditions are met (e.g., Exodia the Forbidden One, Final Countdown)."))),
    )

    private val coreKeywords = listOf(
        Keyword("Normal Summon", listOf(p("Once per turn, play a monster from your hand in face-up Attack Position. Levels 1\u20134 require no Tributes, 5\u20136 require 1, and 7+ require 2. This does not start a Chain.")), "Summons"),
        Keyword("Set", listOf(p("Play a monster from your hand in face-down Defense Position. Counts as your one Normal Summon/Set per turn. A Set monster is not considered Summoned until flipped face-up.")), "Summons"),
        Keyword("Tribute Summon", listOf(p("A Normal Summon of a Level 5+ monster requiring Tributes. Send the required number of monsters you control to the Graveyard before Summoning.")), "Summons"),
        Keyword("Flip Summon", listOf(p("Change a face-down Defense Position monster to face-up Attack Position. Cannot be done the same turn the monster was Set. No limit per turn.")), "Summons"),
        Keyword("Special Summon", listOf(p("Any summon that is not a Normal Summon or Flip Summon. Includes Fusion, Ritual, and card effects. No limit per turn unless specified.")), "Summons"),
        Keyword("Fusion Summon", listOf(p("Special Summon a Fusion Monster from your Fusion Deck using the Fusion Material Monsters listed on the card, combined with a card like 'Polymerization'. Send materials to the Graveyard.")), "Summons"),
        Keyword("Ritual Summon", listOf(p("Special Summon a Ritual Monster from your hand by activating its matching Ritual Spell Card and Tributing monsters whose total Levels equal or exceed the Ritual Monster's Level.")), "Summons"),
        Keyword("Attack Position", listOf(p("A monster placed vertically on the field. It can declare attacks and its ATK is used for battle damage calculation.")), "Battle Positions"),
        Keyword("Defense Position", listOf(p("A monster placed horizontally on the field. It cannot declare attacks and its DEF is used for battle damage calculation.")), "Battle Positions"),
        Keyword("Battle Damage", listOf(p("Damage inflicted to a player by an attacking monster or by a battle between two monsters. Different from effect damage.")), "Damage Calculation"),
        Keyword("Piercing Battle Damage", listOf(p("When attacking a Defense Position monster, if ATK exceeds DEF, the difference is dealt to the opponent as battle damage.")), "Damage Calculation"),
        Keyword("Direct Attack", listOf(p("If your opponent controls no monsters, you can attack them directly, dealing damage equal to your monster's ATK.")), "Damage Calculation"),
        Keyword("Chain", listOf(p("When a card is activated, the opponent may respond with their own card, creating a Chain. Effects resolve backwards \u2014 the last card activated resolves first.")), "Core Mechanics"),
        Keyword("Spell Speed", listOf(p("Determines which effects can chain to which."), bl(listOf("**Speed 1:** Normal/Equip/Continuous/Field/Ritual Spells, Ignition/Trigger/Flip effects", "**Speed 2:** Quick-Play Spells, Normal/Continuous Traps, Multi-Trigger effects", "**Speed 3:** Counter Traps (only another Speed 3 can respond)"))), "Core Mechanics"),
        Keyword("Turn Player Priority", listOf(p("The turn player always has Priority to activate a card first in each phase or step. The opponent cannot activate cards until Priority is passed.")), "Core Mechanics"),
        Keyword("Field Spell Card", listOf(p("Placed in the Field Card Zone. Only 1 can be active at a time. A new Field Spell automatically destroys the previous one. Does not count toward the 5-card Spell & Trap Zone limit.")), "Spell Types"),
        Keyword("Equip Spell Card", listOf(p("Equips to 1 face-up monster on the field. Remains on the field. If the equipped monster is destroyed or flipped face-down, the Equip Card is destroyed.")), "Spell Types"),
        Keyword("Quick-Play Spell Card", listOf(p("Can be activated during any Phase of your turn. Can also be activated during your opponent's turn if you Set it face-down first, but not in the same turn it was Set.")), "Spell Types"),
        Keyword("Continuous Spell Card", listOf(p("Remains on the field after activation. Its effect continues as long as it remains face-up.")), "Spell Types"),
        Keyword("Counter Trap Card", listOf(p("The fastest card type (Spell Speed 3). Normally activated in response to another card's activation. Only another Counter Trap can respond. Often negates effects.")), "Trap Types"),
        Keyword("Continuous Trap Card", listOf(p("Remains on the field after activation. Its effect continues as long as it remains face-up. May have Ignition-like or Trigger-like effects.")), "Trap Types"),
        Keyword("Graveyard", listOf(p("Destroyed monsters and used Spell/Trap Cards are sent here face-up. Both players' Graveyards are public knowledge. The order of cards must not be changed.")), "Field Zones"),
        Keyword("Remove from Play", listOf(p("A card removed from play is separated from the field instead of going to the Graveyard. Normally cannot be used again during the current Duel.")), "Core Mechanics"),
        Keyword("Destroy", listOf(p("A card is destroyed when sent to the Graveyard due to battle or by a card effect that specifically uses the word 'destroy'. Returning to hand/Deck or sending as cost/Tribute is NOT destroying.")), "Core Mechanics"),
        Keyword("Tribute", listOf(p("Send a monster you control to the Graveyard. A cost for Summoning Level 5+ monsters or activating certain effects. A Tributed monster is NOT treated as destroyed.")), "Core Mechanics"),
        Keyword("Discard", listOf(p("Send a card from your hand to the Graveyard, either by a card effect or by the End Phase hand size limit (maximum 6 cards).")), "Core Mechanics"),
        Keyword("Control", listOf(p("Cards on your side of the field are under your control. Your opponent can take control of your cards through card effects. If a controlled card is sent to the Graveyard, it goes to the owner's Graveyard.")), "Core Mechanics"),
        Keyword("Target", listOf(p("For effects that target, you select the target when activating the card. Effects that affect all cards, or where you select at resolution, do not target.")), "Core Mechanics"),
        Keyword("Monster Tokens", listOf(p("Monsters created by card effects. Not included in your Deck. Cannot be sent to the hand or Graveyard \u2014 when destroyed, they are simply removed from the field. Occupy a Monster Zone.")), "Core Mechanics"),
        Keyword("Forbidden & Limited", listOf(p("Normally up to 3 copies of a card. Forbidden = 0, Limited = 1, Semi-Limited = 2. The list is updated regularly for official tournaments.")), "Core Mechanics"),
        Keyword("Flip Effect", listOf(p("An effect that activates when a face-down card is flipped face-up (Flip Summoned, attacked, or flipped by a card effect). Marked by 'FLIP:' on the card.")), "Effect Types"),
        Keyword("Continuous Effect", listOf(p("An effect that is active while the monster is face-up on the field. No activation trigger \u2014 starts when the monster appears, ends when it leaves.")), "Effect Types"),
        Keyword("Ignition Effect", listOf(p("An effect you manually activate during your Main Phase by declaring it. May have costs like discarding or Tributing.")), "Effect Types"),
        Keyword("Trigger Effect", listOf(p("An effect that activates automatically at specific times, such as 'during the Standby Phase' or 'when this monster is destroyed.'")), "Effect Types"),
        Keyword("Multi-Trigger Effect", listOf(p("A monster effect that can be activated during either player's turn (Spell Speed 2), as long as specific requirements are met.")), "Effect Types"),
        Keyword("Match", listOf(p("A Match is a best 2-out-of-3 Duels. Between Duels, you may swap cards between your Side Deck and Main Deck.")), "Core Mechanics"),
        Keyword("Original ATK/DEF", listOf(p("The ATK or DEF value printed on the Monster Card, not including modifiers from Equip Spells or other effects. If '?' is printed, the original value is treated as 0.")), "Core Mechanics"),
        Keyword("Pay a Cost", listOf(p("An action required before activating a card or Summoning a monster (discarding, paying LP, Tributing). Costs are NOT refunded if the activation is negated.")), "Core Mechanics"),
    )

    private val drawPhaseLegacy = RuleStep(
        "Draw Phase",
        "The turn player draws 1 card from their Deck.",
        listOf(
            p("This is the first phase of every turn. You must draw 1 card from the top of your Deck."),
            p("If you cannot draw because your Deck is empty, you lose the Duel."),
            p("**The player who goes first does draw during their first Draw Phase**, giving them a 6-card hand after drawing."),
            p("After drawing, Trap Cards or Quick-Play Spell Cards can be activated before proceeding to the Standby Phase."),
        ),
    )

    private val standbyPhase = RuleStep(
        "Standby Phase",
        "Resolve effects that activate during the Standby Phase.",
        listOf(
            p("If a card says to do something during the Standby Phase, resolve those effects now. This includes maintenance costs that must be paid."),
            p("Trap Cards and Quick-Play Spell Cards can be activated in this phase."),
        ),
    )

    private val mainPhase2 = RuleStep(
        "Main Phase 2",
        "A second chance to play cards after battling.",
        listOf(
            p("After the Battle Phase, you may enter Main Phase 2. You can perform all the same actions as Main Phase 1, but limited actions already used (Normal Summon/Set) cannot be repeated."),
            p("If you did not conduct a Battle Phase, Main Phase 2 is skipped."),
        ),
    )

    private val endPhase = RuleStep(
        "End Phase",
        "Conclude your turn and discard to 6 cards if needed.",
        listOf(
            p("Announce the end of your turn. Resolve any card effects that activate during the End Phase."),
            p("If you have more than 6 cards in your hand, discard to the Graveyard until you have exactly 6."),
        ),
    )

    private val battlePhase = RuleStep(
        "Battle Phase",
        "Declare attacks with your monsters and deal battle damage.",
        listOf(
            p("The Battle Phase is divided into 4 steps. The player going first cannot conduct a Battle Phase on their first turn."),
            sh("**Start Step:**"),
            p("Announce you are entering the Battle Phase."),
            sh("**Battle Step:**"),
            p("Select 1 Attack Position monster to attack with and 1 target. Each Attack Position monster can declare 1 attack per turn. If your opponent controls no monsters, you may attack directly."),
            sh("**Damage Step:**"),
            p("Calculate battle damage. Only Counter Traps and ATK/DEF-modifying effects may be activated here, and only before damage calculation."),
            bl(listOf(
                "**vs Attack Position** (ATK vs ATK): Higher ATK wins, difference is battle damage. Equal = both destroyed, no damage.",
                "**vs Defense Position** (ATK vs DEF): ATK > DEF = destroy defender, no damage. ATK \u2264 DEF = neither destroyed, difference dealt to attacker as damage.",
                "**Direct Attack:** Full ATK as battle damage.",
            )),
            sh("**Replay:**"),
            p("If the attack target leaves the field before the Damage Step, you may re-select a target or cancel the attack."),
            sh("**End Step:**"),
            p("Announce the end of your Battle Phase."),
        ),
    )

    private val mainPhase56 = RuleStep(
        "Main Phase 1",
        "Play cards and set up your field before battling.",
        listOf(
            sh("**Once per turn:**"),
            bl(listOf(
                "**Normal Summon** a monster in face-up Attack Position",
                "**Set** a monster in face-down Defense Position",
                "Levels 1\u20134 require no Tributes",
                "Levels 5\u20136 require 1 Tribute",
                "Levels 7+ require 2 Tributes",
            )),
            sh("**As many times as you want:**"),
            bl(listOf(
                "**Special Summon** monsters (Fusion, Ritual, or by card effects)",
                "**Flip Summon** a face-down monster to face-up Attack Position",
                "**Change battle position** of monsters (once each, except: cannot change a monster played this turn)",
                "**Activate** Spell Cards and Trap Cards, or card effects",
                "**Set** Spell and Trap Cards face-down",
            )),
        ),
    )

    private fun makeSetup(firstTurnDraws: Boolean) = listOf(
        RuleStep(
            "Shuffle & Cut",
            "Both players shuffle their decks, then the opponent may cut.",
            listOf(
                p("Each player shuffles their own deck thoroughly. After shuffling, present your deck to your opponent."),
                p("Your opponent may then shuffle or cut your deck (be careful when touching your opponent's cards)."),
            ),
        ),
        RuleStep(
            "Determine First Turn",
            "Decide who goes first using rock-paper-scissors or a coin flip.",
            if (firstTurnDraws) listOf(
                p("The winner chooses to go first or second. For subsequent Duels, the loser of the previous Duel decides who goes first."),
                p("If the previous Duel ended in a tie, determine with another random method."),
                p("**The player who goes first does draw during their first Draw Phase** (starting with 6 cards after drawing)."),
                p("The player who goes first cannot conduct a Battle Phase during their very first turn."),
            ) else listOf(
                p("The winner chooses to go first or second. For subsequent Duels, the loser of the previous Duel decides who goes first."),
                p("If the previous Duel ended in a tie, determine with another random method."),
                p("**The player who goes first does not draw during their first Draw Phase**."),
                p("The player who goes first cannot conduct a Battle Phase during their very first turn."),
            ),
        ),
        RuleStep(
            "Draw Starting Hand",
            "Each player draws 5 cards from the top of their Deck.",
            listOf(p("Both players draw 5 cards from the top of their Main Deck. This is your starting hand before the first Draw Phase.")),
        ),
    )

    private val v56Setup = makeSetup(firstTurnDraws = true)
    private val v56Turn = listOf(drawPhaseLegacy, standbyPhase, mainPhase56, battlePhase, mainPhase2, endPhase)
    private val v56Keywords = coreKeywords

    private val mainPhaseV7 = RuleStep(
        "Main Phase 1",
        "Play cards and set up your field before battling.",
        listOf(
            sh("**Once per turn:**"),
            bl(listOf(
                "**Normal Summon** a monster in face-up Attack Position",
                "**Set** a monster in face-down Defense Position",
                "Levels 1\u20134 require no Tributes",
                "Levels 5\u20136 require 1 Tribute",
                "Levels 7+ require 2 Tributes",
            )),
            sh("**As many times as you want:**"),
            bl(listOf(
                "**Special Summon** monsters (Fusion, Ritual, Synchro, or by card effects)",
                "**Flip Summon** a face-down monster to face-up Attack Position",
                "**Change battle position** of monsters (once each, except: cannot change a monster played this turn)",
                "**Activate** Spell Cards and Trap Cards, or card effects",
                "**Set** Spell and Trap Cards face-down",
            )),
        ),
    )

    private val v7Setup = makeSetup(firstTurnDraws = true)
    private val v7Turn = listOf(drawPhaseLegacy, standbyPhase, mainPhaseV7, battlePhase, mainPhase2, endPhase)

    private val v7Keywords = coreKeywords + listOf(
        Keyword("Synchro Summon", listOf(p("Send 1 face-up Tuner monster + 1 or more face-up non-Tuner monsters from your field to the Graveyard. Their combined Levels must exactly equal the Synchro Monster's Level. Special Summon the Synchro Monster from your Extra Deck.")), "Summons"),
        Keyword("Tuner Monster", listOf(p("A monster subtype marked 'Tuner' next to its Type. Required as material for a Synchro Summon.")), "Summons"),
        Keyword("Synchro Monster", listOf(p("A white-framed monster placed in the Extra Deck. Summoned via Synchro Summon. Has a Level (not a Rank).")), "Card Types"),
        Keyword("Quick Effect", listOf(p("A monster effect with Spell Speed 2 that can be activated during either player's turn. Formerly called Multi-Trigger Effects.")), "Effect Types"),
        Keyword("Extra Deck", listOf(p("Replaces the Fusion Deck. Contains Synchro and Fusion Monsters. 0 to 15 cards. You may look at your own Extra Deck during the game.")), "Deck Construction"),
    )

    private val mainPhaseV8 = RuleStep(
        "Main Phase 1",
        "Play cards and set up your field before battling.",
        listOf(
            sh("**Once per turn:**"),
            bl(listOf(
                "**Normal Summon** a monster in face-up Attack Position",
                "**Set** a monster in face-down Defense Position",
                "Levels 1\u20134 require no Tributes",
                "Levels 5\u20136 require 1 Tribute",
                "Levels 7+ require 2 Tributes",
            )),
            sh("**As many times as you want:**"),
            bl(listOf(
                "**Special Summon** monsters (Fusion, Ritual, Synchro, Xyz, or by card effects)",
                "**Flip Summon** a face-down monster to face-up Attack Position",
                "**Change battle position** of monsters (once each, except: cannot change a monster played this turn)",
                "**Activate** Spell Cards and Trap Cards, or card effects",
                "**Set** Spell and Trap Cards face-down",
            )),
        ),
    )

    private val v8Setup = makeSetup(firstTurnDraws = true)
    private val v8Turn = listOf(drawPhaseLegacy, standbyPhase, mainPhaseV8, battlePhase, mainPhase2, endPhase)

    private val v8Keywords = v7Keywords + listOf(
        Keyword("Xyz Summon", listOf(p("Stack 2 or more face-up monsters of the same Level as Xyz Materials. Place the Xyz Monster on top from your Extra Deck. Xyz Monsters have Ranks, not Levels.")), "Summons"),
        Keyword("Xyz Monster", listOf(p("A black-framed monster in the Extra Deck. Has a Rank (stars on the left) instead of a Level. Uses Xyz Materials for effects via 'detaching'.")), "Card Types"),
        Keyword("Xyz Material", listOf(p("Monsters stacked underneath an Xyz Monster. They are NOT treated as cards on the field. Detaching sends 1 material to the Graveyard, usually as a cost. Tokens cannot be used as Xyz Material.")), "Summons"),
        Keyword("Rank", listOf(p("Xyz Monsters have Ranks instead of Levels. Stars appear on the left side of the card. Effects that refer to 'Level' do not apply to Xyz Monsters.")), "Card Types"),
        Keyword("Banished", listOf(p("Cards separated from the field; not in the Graveyard. Previously called 'Remove from Play'. Returned to the owner after the Duel.")), "Core Mechanics"),
    )

    private val drawPhaseModern = RuleStep(
        "Draw Phase",
        "The turn player draws 1 card from their Deck.",
        listOf(
            p("This is the first phase of every turn. You must draw 1 card from the top of your Deck."),
            p("If you cannot draw because your Deck is empty, you lose the Duel."),
            p("**The player who goes first does NOT draw during their first Draw Phase** \u2014 they start their first turn with only their initial 5-card hand."),
            p("After drawing, Trap Cards or Quick-Play Spell Cards can be activated before proceeding to the Standby Phase."),
        ),
    )

    private val mainPhaseV9 = RuleStep(
        "Main Phase 1",
        "Play cards and set up your field before battling.",
        listOf(
            sh("**Once per turn:**"),
            bl(listOf(
                "**Normal Summon** a monster in face-up Attack Position",
                "**Set** a monster in face-down Defense Position",
                "Levels 1\u20134 require no Tributes",
                "Levels 5\u20136 require 1 Tribute",
                "Levels 7+ require 2 Tributes",
                "**Pendulum Summon** (once per turn)",
            )),
            sh("**As many times as you want:**"),
            bl(listOf(
                "**Special Summon** monsters (Fusion, Ritual, Synchro, Xyz, or by card effects)",
                "**Flip Summon** a face-down monster to face-up Attack Position",
                "**Change battle position** of monsters (once each, except: cannot change a monster played this turn)",
                "**Activate** Spell Cards and Trap Cards, or card effects",
                "**Set** Spell and Trap Cards face-down",
            )),
        ),
    )

    private val v9Setup = makeSetup(firstTurnDraws = false)
    private val v9Turn = listOf(drawPhaseModern, standbyPhase, mainPhaseV9, battlePhase, mainPhase2, endPhase)

    private val v9Keywords = v8Keywords + listOf(
        Keyword("Pendulum Monster", listOf(p("A hybrid card (half-green frame) that can be Normal Summoned/Set as a Monster OR activated as a Spell in a Pendulum Zone. Has ATK/DEF and a Pendulum Scale. When sent from the field to the GY, it goes face-up to the Extra Deck instead.")), "Card Types"),
        Keyword("Pendulum Summon", listOf(p("Once per turn during your Main Phase, if you have a Pendulum Card in each Pendulum Zone, you can Special Summon any number of monsters from your hand and face-up Extra Deck whose Levels are between the two Pendulum Scales (exclusive). Example: Scales 1 & 8 summon Levels 2\u20137.")), "Summons"),
        Keyword("Pendulum Scale", listOf(p("A number on the Pendulum Monster Card that determines the range of Levels you can Pendulum Summon. The range is between the two Scales on your field, not including the Scale numbers themselves.")), "Summons"),
        Keyword("Pendulum Zone", listOf(p("The leftmost and rightmost Spell & Trap Zones. Pendulum Cards activated as Spells go here. They do not count toward the 5-card S/T limit. If a card in this zone would be destroyed, it goes face-up to the Extra Deck.")), "Card Types"),
        Keyword("Face-up Extra Deck", listOf(p("When a Pendulum Monster on the field is sent to the Graveyard, it is placed face-up on the Extra Deck instead. Face-up Pendulum Monsters in the Extra Deck can be Pendulum Summoned again.")), "Core Mechanics"),
    )

    private val mainPhaseV10 = RuleStep(
        "Main Phase 1",
        "Play cards and set up your field before battling.",
        listOf(
            sh("**Once per turn:**"),
            bl(listOf(
                "**Normal Summon** a monster in face-up Attack Position",
                "**Set** a monster in face-down Defense Position",
                "Levels 1\u20134 require no Tributes",
                "Levels 5\u20136 require 1 Tribute",
                "Levels 7+ require 2 Tributes",
                "**Pendulum Summon** (once per turn)",
            )),
            sh("**As many times as you want:**"),
            bl(listOf(
                "**Special Summon** monsters (Fusion, Ritual, Synchro, Xyz, Link, or by card effects)",
                "**Flip Summon** a face-down monster to face-up Attack Position",
                "**Change battle position** of monsters (once each, except: cannot change a monster played this turn)",
                "**Activate** Spell Cards and Trap Cards, or card effects",
                "**Set** Spell and Trap Cards face-down",
            )),
            p("**Important:** Monsters Summoned from the Extra Deck must go to the Extra Monster Zone or a Main Monster Zone a Link Monster points to. Pendulum Summons from the Extra Deck follow this rule; from the hand, they go to any Main Monster Zone."),
        ),
    )

    private val v10Setup = makeSetup(firstTurnDraws = false)
    private val v10Turn = listOf(drawPhaseModern, standbyPhase, mainPhaseV10, battlePhase, mainPhase2, endPhase)

    private val v10Keywords = v9Keywords + listOf(
        Keyword("Link Monster", listOf(p("A blue-framed monster in the Extra Deck. Has a Link Rating instead of a Level/Rank, and no DEF. Cannot exist in Defense Position. Has red Link Arrows around the card art.")), "Card Types"),
        Keyword("Link Summon", listOf(p("Send face-up monsters from your field to the GY whose total count matches the Link Rating. A Link Monster used as material counts as 1 material OR its own Link Rating. Tokens can be used as Link Material.")), "Summons"),
        Keyword("Link Rating", listOf(p("The number in the bottom-right of a Link Monster card. Equals the number of Link Arrows and the minimum materials needed. Replaces the DEF value.")), "Card Types"),
        Keyword("Link Arrow", listOf(p("Red arrows on a Link Monster's card art pointing to specific zones. Zones pointed to can be used for Extra Deck Summons. A monster pointed to is 'linked'. Two Link Monsters pointing at each other are 'co-linked'.")), "Core Mechanics"),
        Keyword("Extra Monster Zone", listOf(p("A zone shared by both players in the center of the field. Where all Extra Deck monsters must be summoned. Each player controls 1 (normally).")), "Field Zones"),
        Keyword("Simultaneous Effects (SEGOC)", listOf(p("When multiple Trigger effects activate at the same time, they are placed on a Chain in this order:"), bl(listOf("1. Turn player's mandatory effects", "2. Opponent's mandatory effects", "3. Turn player's optional effects", "4. Opponent's optional effects")), p("The Chain resolves backwards, starting with the opponent's optional effects.")), "Core Mechanics"),
        Keyword("PSCT Structure", listOf(p("Problem Solving Card Text uses punctuation to indicate timing:"), bl(listOf("Before colon (:) = activation conditions", "Before semi-colon (;) = actions at activation (costs, targeting)", "After semi-colon = effect at resolution"))), "Core Mechanics"),
    )

    private val v56DeckRules = listOf(
        InfoCard(
            "Main Deck",
            listOf(
                p("Minimum 40 cards (no upper limit)."),
                p("Max 3 copies of any card (Main Deck + Side Deck combined)."),
            ),
        ),
        InfoCard(
            "Fusion Deck",
            listOf(
                p("Contains only Fusion Monsters."),
                p("No size limit — you may include as many Fusion Monsters as you want."),
                p("Cards in the Fusion Deck are not counted toward the 40-card minimum."),
                p("Max 3 copies of the same card."),
            ),
        ),
        InfoCard(
            "Side Deck",
            listOf(
                p("Exactly 15 cards."),
                p("Swap cards between Side Deck and Main Deck between Duels in a Match."),
                p("Must remain exactly 15 cards after swapping."),
            ),
        ),
    )

    private val v7DeckRules = listOf(
        InfoCard(
            "Main Deck",
            listOf(
                p("40 to 60 cards."),
                p("Max 3 copies of any card (Main Deck + Extra Deck + Side Deck combined)."),
            ),
        ),
        InfoCard(
            "Extra Deck",
            listOf(
                p("0 to 15 cards."),
                p("Contains Synchro Monsters and Fusion Monsters."),
                p("Not counted toward the 40-card minimum."),
            ),
        ),
        InfoCard(
            "Side Deck",
            listOf(
                p("0 to 15 cards."),
                p("Swap cards between Side Deck, Main Deck, and Extra Deck between Duels."),
            ),
        ),
    )

    private val v8DeckRules = listOf(
        InfoCard(
            "Main Deck",
            listOf(
                p("40 to 60 cards."),
                p("Max 3 copies of any card (Main Deck + Extra Deck + Side Deck combined)."),
            ),
        ),
        InfoCard(
            "Extra Deck",
            listOf(
                p("0 to 15 cards."),
                p("Contains Xyz Monsters, Synchro Monsters, and Fusion Monsters."),
            ),
        ),
        InfoCard(
            "Side Deck",
            listOf(p("0 to 15 cards.")),
        ),
    )

    private val v9DeckRules = listOf(
        InfoCard(
            "Main Deck",
            listOf(
                p("40 to 60 cards."),
                p("Max 3 copies of any card (Main Deck + Extra Deck + Side Deck combined)."),
            ),
        ),
        InfoCard(
            "Extra Deck",
            listOf(
                p("0 to 15 cards."),
                p("Contains Pendulum Monsters (face-up), Xyz, Synchro, and Fusion Monsters."),
            ),
        ),
        InfoCard(
            "Side Deck",
            listOf(p("0 to 15 cards.")),
        ),
    )

    private val v10DeckRules = listOf(
        InfoCard(
            "Main Deck",
            listOf(
                p("40 to 60 cards."),
                p("Max 3 copies of any card (Main Deck + Extra Deck + Side Deck combined)."),
            ),
        ),
        InfoCard(
            "Extra Deck",
            listOf(
                p("0 to 15 cards."),
                p("Contains Link Monsters, Pendulum Monsters (face-up), Xyz, Synchro, and Fusion Monsters."),
            ),
        ),
        InfoCard(
            "Side Deck",
            listOf(p("0 to 15 cards.")),
        ),
    )

    val configs: List<RulesConfig> = listOf(
        RulesConfig("v5-dm", "v5.0 - DM", v56Setup, victory, v56Turn, v56Keywords, listOf(PdfAsset("Official Rulebook", "rulebooks/v6-gx.pdf")), deckRules = v56DeckRules),
        RulesConfig("v6-gx", "v6.0 - GX", v56Setup, victory, v56Turn, v56Keywords, listOf(PdfAsset("Official Rulebook", "rulebooks/v6-gx.pdf")), deckRules = v56DeckRules),
        RulesConfig("v7-5ds", "v7.0 - 5D's", v7Setup, victory, v7Turn, v7Keywords, listOf(PdfAsset("Official Rulebook", "rulebooks/v7-5ds.pdf")), deckRules = v7DeckRules),
        RulesConfig("v8-zexal", "v8.0 - ZEXAL", v8Setup, victory, v8Turn, v8Keywords, listOf(PdfAsset("Official Rulebook", "rulebooks/v8-zexal.pdf")), deckRules = v8DeckRules),
        RulesConfig("v9-arcv", "v9.0 - ARC-V", v9Setup, victory, v9Turn, v9Keywords, listOf(PdfAsset("Official Rulebook", "rulebooks/v9-arcv.pdf")), deckRules = v9DeckRules),
        RulesConfig("v10-vrains", "v10 - VRAINS", v10Setup, victory, v10Turn, v10Keywords, listOf(PdfAsset("Official Rulebook", "rulebooks/v10-vrains.pdf")), deckRules = v10DeckRules),
    )
}
