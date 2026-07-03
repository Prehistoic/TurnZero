package com.turnzero.games.yugioh

import com.turnzero.tools.rules.Keyword
import com.turnzero.tools.rules.RuleStep
import com.turnzero.tools.rules.RulesConfig
import com.turnzero.tools.rules.WinCondition

object YuGiOhRules {

    val advancedTcg = RulesConfig(
        rulesetId = "tcg-advanced",
        rulesetName = "Advanced (TCG)",
        pdfUrl = "https://img.yugioh.com/rulebook/2023-11-17/YGO_Rulebook_EN_v10.pdf",
        setup = listOf(
            RuleStep(
                title = "Prepare Your Deck",
                summary = "Your deck must contain 40 to 60 cards.",
                detail = "Your Main Deck must contain between 40 and 60 cards. You may have up to 15 cards in your Extra Deck and up to 15 cards in your Side Deck. You can have up to 3 copies of any card in your Main Deck, Extra Deck, and Side Deck combined, unless that card is on the Forbidden & Limited List.",
            ),
            RuleStep(
                title = "Shuffle & Cut",
                summary = "Both players thoroughly shuffle their decks.",
                detail = "Each player shuffles their own deck thoroughly. After shuffling, you must present your deck to your opponent. Your opponent may then shuffle or cut your deck.",
            ),
            RuleStep(
                title = "Determine First Turn",
                summary = "Decide who goes first using any random method.",
                detail = "Players may use any random method (rock-paper-scissors, die roll, coin flip) to determine who chooses to go first or second. In tournament play, the winner of the die roll decides. The player who goes first cannot conduct their Battle Phase during their first turn.",
            ),
            RuleStep(
                title = "Draw Starting Hand",
                summary = "Each player draws 5 cards from the top of their deck.",
                detail = "Both players draw 5 cards from the top of their Main Deck. This is your starting hand. If you do not like your hand, you may take a mulligan (shuffle your hand back and draw 5 again) if both players agree. In official tournaments, mulligans are not allowed.",
            ),
            RuleStep(
                title = "Place Field Center Card",
                summary = "(Optional) Place a Field Center Card between both fields.",
                detail = "A Field Center Card marks the center of the playing field and the boundary between both players' fields. It also indicates the Extra Monster Zones. This is optional in casual play but recommended to clearly separate both players' fields.",
            ),
        ),
        winCons = listOf(
            WinCondition(
                title = "Reduce LP to 0",
                description = "Reduce your opponent's Life Points from 8000 to 0 through battle damage and effect damage.",
            ),
            WinCondition(
                title = "Deck Out",
                description = "If a player cannot draw a card from their Main Deck when required, they lose the Duel.",
            ),
            WinCondition(
                title = "Alternative Win Conditions",
                description = "Some cards have effects that declare an automatic win when their conditions are met (e.g., Exodia the Forbidden One, Destiny Board, Final Countdown).",
            ),
        ),
        playSequence = listOf(
            RuleStep(
                title = "Draw Phase",
                summary = "The turn player draws 1 card from their Main Deck.",
                detail = "This is the first phase of every turn. The turn player must draw 1 card from the top of their Main Deck, unless a card effect states otherwise. The player going first does not draw during the Draw Phase of their first turn.",
            ),
            RuleStep(
                title = "Standby Phase",
                summary = "Resolve effects that activate during the Standby Phase.",
                detail = "Certain card effects specify that they activate or can be activated during the Standby Phase. The turn player has priority to activate effects first, then the opponent may respond.",
            ),
            RuleStep(
                title = "Main Phase 1",
                summary = "The main phase to play cards and set up your field.",
                detail = "During Main Phase 1, you can: Normal Summon/Set 1 monster, Special Summon monsters through card effects, activate or Set Spell/Trap Cards, change battle positions of monsters (except those summoned this turn). You can perform these actions in any order as long as you have not yet used your one Normal Summon/Set for the turn.",
            ),
            RuleStep(
                title = "Battle Phase",
                summary = "Declare attacks with your monsters to destroy opponent's monsters and deal damage.",
                detail = "The Battle Phase is divided into four steps per attack: Start Step, Battle Step (declare attack), Damage Step (calculate damage), and End Step. Each of your Attack Position monsters can declare one attack per turn. The player going first cannot conduct a Battle Phase during their first turn.",
            ),
            RuleStep(
                title = "Main Phase 2",
                summary = "A second chance to play cards after battling.",
                detail = "After the Battle Phase, you may enter Main Phase 2. You can perform all the same actions as Main Phase 1 (except you cannot Normal Summon/Set again if you already did so during Main Phase 1). If you did not conduct a Battle Phase, this phase is skipped.",
            ),
            RuleStep(
                title = "End Phase",
                summary = "Conclude your turn and resolve end-of-turn effects.",
                detail = "During the End Phase, you must discard cards from your hand if you have more than 6 (the hand size limit is 6). Any effects that activate during the End Phase resolve here. After the End Phase, it becomes the opponent's turn.",
            ),
        ),
        keywords = listOf(
            Keyword(
                name = "Attack Position",
                definition = "A monster placed vertically on the field. In this position, the monster can declare attacks and its ATK value is used for battle damage calculation.",
                category = "Battle Positions",
            ),
            Keyword(
                name = "Chain",
                definition = "A sequence of card effects that activate in response to each other. Chains resolve backwards (the last card activated resolves first).",
                category = "Core Mechanics",
            ),
            Keyword(
                name = "Defense Position",
                definition = "A monster placed horizontally on the field. In this position, the monster cannot declare attacks and its DEF value is used for battle damage calculation.",
                category = "Battle Positions",
            ),
            Keyword(
                name = "Normal Summon",
                definition = "Once per turn, you can summon a monster from your hand in face-up Attack Position. This does not start a Chain.",
                category = "Summons",
            ),
            Keyword(
                name = "Piercing Battle Damage",
                definition = "When an Attack Position monster attacks a Defense Position monster, if the attacking monster's ATK is higher than the defending monster's DEF, the difference is inflicted as battle damage to the opponent.",
                category = "Damage Calculation",
            ),
            Keyword(
                name = "Quick Effect",
                definition = "A monster effect with Spell Speed 2. It can be activated during either player's turn and can be chained to other effects. (Formerly known as Multi-Trigger Effect.)",
                category = "Effect Types",
            ),
            Keyword(
                name = "Special Summon",
                definition = "Any summon that is not a Normal Summon or Flip Summon. Some Special Summons start a Chain while others do not, depending on the card text.",
                category = "Summons",
            ),
            Keyword(
                name = "Spell Speed",
                definition = "Determines which effects can be chained to which. Spell Speed 1 (most Spell Cards, Ignition/Trigger/Flip Effects), Spell Speed 2 (Quick-Play Spells, Normal/Continuous Traps, Quick Effects), Spell Speed 3 (Counter Traps). Higher Spell Speeds can chain to lower ones, but not vice versa.",
                category = "Core Mechanics",
            ),
            Keyword(
                name = "Tribute Summon",
                definition = "To Normal Summon a Level 5 or 6 monster, you must Tribute (send to the GY) 1 monster you control. Level 7+ requires 2 Tributes. This counts as your one Normal Summon/Set for the turn.",
                category = "Summons",
            ),
        ),
    )

    val speedDuel = RulesConfig(
        rulesetId = "tcg-speed-duel",
        rulesetName = "Speed Duel",
        setup = listOf(
            RuleStep(
                title = "Deck Construction",
                summary = "20 to 30 cards in the Main Deck, up to 5 cards in the Extra Deck.",
                detail = "[Placeholder — will be filled with actual Speed Duel setup rules]",
            ),
            RuleStep(
                title = "Skill Card Setup",
                summary = "Each player chooses 1 Skill Card to use during the Duel.",
                detail = "[Placeholder — will be filled with actual Skill Card setup rules]",
            ),
        ),
        winCons = listOf(
            WinCondition(
                title = "Reduce LP to 0",
                description = "Each player starts with 4000 LP. Reduce your opponent's LP to 0 to win.",
            ),
            WinCondition(
                title = "Deck Out",
                description = "If a player cannot draw from their Main Deck, they lose.",
            ),
        ),
        playSequence = listOf(
            RuleStep(
                title = "Draw Phase",
                summary = "Draw 1 card from your Main Deck.",
                detail = "[Placeholder — will be filled with actual Speed Duel turn sequence]",
            ),
            RuleStep(
                title = "Main Phase",
                summary = "Summon monsters, Set Spells/Traps, activate Skills.",
                detail = "[Placeholder — will be filled with actual Main Phase rules]",
            ),
            RuleStep(
                title = "Battle Phase",
                summary = "Declare attacks with your monsters.",
                detail = "[Placeholder — will be filled with actual Battle Phase rules]",
            ),
            RuleStep(
                title = "End Phase",
                summary = "Conclude your turn.",
                detail = "[Placeholder — will be filled with actual End Phase rules]",
            ),
        ),
        keywords = listOf(
            Keyword(
                name = "Skill Card",
                definition = "A special card that grants a unique ability to use during the Duel. Each player can activate their Skill Card once per Duel according to its activation condition.",
                category = "Core Mechanics",
            ),
        ),
    )

    val configs: List<RulesConfig> = listOf(advancedTcg, speedDuel)
}
