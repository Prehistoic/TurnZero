package com.turnzero.games.pokemon.rules

import com.turnzero.tools.rules.ContentBlock
import com.turnzero.tools.rules.Keyword
import com.turnzero.tools.rules.PdfAsset
import com.turnzero.tools.rules.RuleStep
import com.turnzero.tools.rules.RulesConfig
import com.turnzero.tools.rules.WinCondition

object PokemonRules {

    private fun p(text: String) = ContentBlock.Paragraph(text)
    private fun sh(text: String) = ContentBlock.SubHeading(text)
    private fun bl(items: List<String>) = ContentBlock.BulletList(items)
    private fun img(path: String) = ContentBlock.Image("asset:$path")

    private val victory = listOf(
        WinCondition(
            "Take All Prize Cards",
            listOf(
                p("Each player starts with 6 face-down Prize cards. Every time you Knock Out an opponent's Pokemon, you take 1 Prize card into your hand (2 for Pokemon ex/V/GX, 3 for VMAX/TAG TEAM/Mega ex)."),
                p("The first player to take all 6 of their Prize cards wins the game. This is the primary win condition."),
            ),
        ),
        WinCondition(
            "No Pokemon in Play",
            listOf(
                p("If your opponent has no Pokemon left in play (no Active Pokemon and no Benched Pokemon), you immediately win the game."),
                p("This typically happens when their last Pokemon is Knocked Out and they have no Bench replacements."),
            ),
        ),
        WinCondition(
            "Empty Deck at Start of Turn",
            listOf(
                p("If your opponent has no cards left in their deck at the beginning of their turn and cannot draw a card, you win the game."),
                p("Note: losing the ability to draw due to a card effect during your turn does not cause a loss."),
            ),
        ),
        WinCondition(
            "Concession",
            listOf(
                p("Either player may concede at any time. Announce your concession and you lose immediately."),
            ),
        ),
    )

    private val setup = listOf(
        RuleStep(
            "Shuffle & Determine First Player",
            "Shuffle your 60-card deck. Flip a coin — the winner decides who goes first.",
            listOf(
                p("Your deck must have exactly 60 cards. Except for Basic Energy, you can have at most 4 copies of any card with the same name."),
                p("Flip a coin. The winner chooses to go first or second."),
            ),
        ),
        RuleStep(
            "Draw Opening Hand",
            "Each player draws 7 cards from the top of their deck as their starting hand.",
            listOf(
                p("Both players draw the top 7 cards of their deck simultaneously. Keep your hand hidden from your opponent."),
            ),
        ),
        RuleStep(
            "Place Basic Pokemon",
            "Place 1 Basic Pokemon face-down as your Active Pokemon, then place up to 5 more face-down on your Bench.",
            listOf(
                p("If you have no Basic Pokemon in your hand, you must take a mulligan: reveal your hand, shuffle it back into your deck, and draw 7 new cards. Your opponent may draw 1 extra card for each mulligan you take."),
                p("After drawing a hand with at least 1 Basic Pokemon, place one face-down in the Active Spot. You may optionally place up to 5 more Basic Pokemon face-down on your Bench."),
                p("Tip: Placing multiple Basic Pokemon on your Bench gives you backup options if your Active Pokemon is Knocked Out."),
            ),
        ),
        RuleStep(
            "Set Prize Cards",
            "Take the top 6 cards of your deck and place them face-down to the side as your Prize cards.",
            listOf(
                p("Place exactly 6 cards from the top of your deck face-down in your Prize card area. Neither player may look at Prize cards during the game."),
                p("Each time you Knock Out an opponent's Pokemon, you take 1 of your Prize cards and add it to your hand."),
            ),
        ),
        RuleStep(
            "Flip & Begin",
            "Both players flip all their face-down Pokemon face up and the game begins.",
            listOf(
                p("Reveal your Active and Benched Pokemon by flipping them face up. The first player's turn then begins."),
            ),
        ),
    )

    private val turnStructure = listOf(
        RuleStep(
            "Draw a Card",
            "Start your turn by drawing 1 card from the top of your deck. If your deck is empty, you lose the game.",
            listOf(
                p("Draw 1 card from the top of your deck and add it to your hand."),
                p("If you cannot draw because your deck is empty at the beginning of your turn, you lose the game. The player going first still draws a card on their first turn."),
            ),
        ),
        RuleStep(
            "Action Phase",
            "Perform any of the following actions in any order, as many times as each allows.",
            listOf(
                sh("A) Put Basic Pokemon on Your Bench"),
                p("Choose any number of Basic Pokemon from your hand and place them face up on your Bench. Your Bench can hold up to 5 Pokemon at a time."),
                sh("B) Evolve Pokemon"),
                p("If you have a Pokemon in your hand that says \"Evolves from [Name]\" and [Name] is a Pokemon you have had in play since the start of your turn, you may play that card on top of it."),
                p("You can evolve Basic to Stage 1, or Stage 1 to Stage 2. A Pokemon cannot evolve on its first turn in play. Neither player can evolve on their very first turn of the game."),
                p("When a Pokemon evolves, it keeps all attached cards and damage counters, but loses all Special Conditions and other effects."),
                sh("C) Attach an Energy Card"),
                p("Take 1 Energy card from your hand and attach it to one of your Pokemon (Active or Benched). You can only do this once per turn."),
                sh("D) Play Trainer Cards"),
                bl(
                    listOf(
                        "**Item cards** — Play as many as you want per turn. Do what the card says, then discard it.",
                        "**Supporter cards** — Play only 1 per turn. The player going first cannot play a Supporter on their first turn.",
                        "**Stadium cards** — Play only 1 per turn. Stays in play with ongoing effects. Only 1 Stadium can be in play at a time — playing a new one discards the old one.",
                        "**Pokemon Tool cards** — Attach to 1 of your Pokemon (max 1 Tool per Pokemon). Stays attached until removed.",
                    ),
                ),
                sh("E) Retreat Your Active Pokemon"),
                p("Discard Energy from your Active Pokemon equal to its Retreat Cost (printed bottom-right). Then switch it with a Benched Pokemon. You can only retreat once per turn."),
                p("Asleep and Paralyzed Pokemon cannot retreat. When your Active Pokemon moves to the Bench, it recovers from all Special Conditions and attack effects."),
                sh("F) Use Abilities"),
                p("Many Pokemon have Abilities you can use during your turn. Abilities are not attacks — using one does not end your turn. You can use Abilities from both Active and Benched Pokemon."),
            ),
        ),
        RuleStep(
            "Attack",
            "Choose an attack on your Active Pokemon, verify you have the required Energy, and declare your attack. Your turn ends afterward.",
            listOf(
                p("The player going first cannot attack on their first turn."),
                sh("Attack Steps"),
                bl(
                    listOf(
                        "**1. Check Energy** — Ensure your Active Pokemon has the required Energy attached. Colorless symbols mean any type works. Attacks with cost 0 need no Energy.",
                        "**2. Check Weakness & Resistance** — If the defending Pokemon has Weakness to your type (e.g. x2), double the damage. If it has Resistance (e.g. -20), subtract that. Don't apply to Benched Pokemon.",
                        "**3. Place Damage Counters** — Place 1 damage counter (10 HP) for each 10 damage dealt, after modifiers. If a Pokemon's total damage equals or exceeds its HP, it's Knocked Out.",
                    ),
                ),
                p("After attacking, check if any Pokemon were Knocked Out. For each KO, the opponent takes Prize card(s) and promotes a new Active Pokemon from their Bench."),
            ),
        ),
        RuleStep(
            "Pokemon Checkup",
            "Between turns, resolve Special Conditions and any between-turn effects.",
            listOf(
                p("After your turn ends, resolve Pokemon Checkup in this order:"),
                bl(
                    listOf(
                        "**1. Poisoned** — Place 1 damage counter on the Poisoned Pokemon.",
                        "**2. Burned** — Place 2 damage counters, then flip a coin. Heads recovers the Pokemon.",
                        "**3. Asleep** — Flip a coin. Heads recovers; tails stays Asleep.",
                        "**4. Paralyzed** — Recovers automatically if Paralyzed since the end of its owner's last turn.",
                    ),
                ),
                p("Apply any effects from Abilities or Trainer cards that specify they happen during Pokemon Checkup."),
                p("After both players complete checks, any Pokemon reduced to 0 HP is Knocked Out and the opponent takes a Prize card. Then the next turn begins."),
            ),
        ),
    )

    private val fieldContent = listOf(
        img("rulebooks/images/pokemon-field.png"),
        p("Your play area is arranged as shown above. Each player has their own zones within a shared in-play area."),
        sh("The Zones"),
        bl(
            listOf(
                "**1. Active Spot** — The front row. Only the Active Pokemon can attack. You must always have exactly 1 Active Pokemon.",
                "**2. Bench** — Back row for Benched Pokemon (up to 5). They can use Abilities but cannot attack. Damage to Benched Pokemon ignores Weakness and Resistance.",
                "**3. Deck** — 60-card draw pile, face-down. Neither player may look at or rearrange either deck.",
                "**4. Discard Pile** — Used and Knocked Out cards go here face-up. Anyone can look through it at any time.",
                "**5. Prize Cards** — 6 face-down cards set aside during setup. Take 1 when you KO an opponent's Pokemon. First to take all 6 wins.",
                "**6. Lost Zone** — A special out-of-play zone. Cards sent here cannot be recovered for the rest of the game.",
                "**7. Stadium (shared)** — Between both Active Spots. Only 1 Stadium can be in play at a time.",
            ),
        ),
        sh("Damage Counters"),
        p("Each damage counter represents 10 damage. A Pokemon is Knocked Out when its total damage equals or exceeds its HP."),
    )

    private val keywords = listOf(
        Keyword(
            "<Active Pokemon>",
            listOf(
                p("The Pokemon in your Active Spot. Only the Active Pokemon can attack. You must always have exactly 1 Active Pokemon."),
            ),
            "Game Terms",
        ),
        Keyword(
            "<Asleep>",
            listOf(
                p("Special Condition. Turn the Pokemon counterclockwise. It cannot attack or retreat. During Pokemon Checkup, flip a coin — heads recovers, tails stays Asleep."),
            ),
            "Special Conditions",
        ),
        Keyword(
            "<Bench>",
            listOf(
                p("Row for Benched Pokemon (up to 5). They can use Abilities but cannot attack. Damage to Benched Pokemon ignores Weakness and Resistance."),
            ),
            "Game Terms",
        ),
        Keyword(
            "<Burned>",
            listOf(
                p("Special Condition. Place a Burn marker. During Pokemon Checkup, place 2 damage counters, then flip a coin — heads recovers. Only one Burn marker at a time."),
            ),
            "Special Conditions",
        ),
        Keyword(
            "<Confused>",
            listOf(
                p("Special Condition. Turn the card top toward you. Before attacking, flip a coin — heads means normal attack; tails means attack fails and you place 3 damage counters on your Pokemon."),
            ),
            "Special Conditions",
        ),
        Keyword(
            "<Damage Counters>",
            listOf(
                p("Each counter equals 10 damage. A Pokemon is KO'd when total counters equal or exceed HP/10."),
            ),
            "Game Terms",
        ),
        Keyword(
            "<Energy>",
            listOf(
                p("Cards that power attacks. 11 types: Grass, Fire, Water, Lightning, Psychic, Fighting, Darkness, Metal, Fairy, Dragon, Colorless. Attach 1 per turn from hand to a Pokemon."),
            ),
            "Game Terms",
        ),
        Keyword(
            "<Evolution>",
            listOf(
                p("Playing a Stage 1 or 2 card on top of a qualifying Pokemon. Keeps attached cards and damage counters, loses Special Conditions and effects. Cannot evolve on first turn or the turn a Pokemon was played."),
            ),
            "Game Terms",
        ),
        Keyword(
            "<Knocked Out>",
            listOf(
                p("When total damage equals or exceeds HP. The Pokemon and attached cards go to discard. Opponent takes Prize card(s): 1 normally, 2 for ex/V/GX, 3 for VMAX/TAG TEAM/Mega ex."),
            ),
            "Game Terms",
        ),
        Keyword(
            "<Mulligan>",
            listOf(
                p("If no Basic Pokemon in opening hand: reveal, shuffle back, draw 7 new cards. Repeat until you have one. Opponent may draw 1 extra card per mulligan you take beyond shared ones."),
            ),
            "Game Terms",
        ),
        Keyword(
            "<Paralyzed>",
            listOf(
                p("Special Condition. Turn the Pokemon clockwise. It cannot attack or retreat. Recovers automatically during Pokemon Checkup after its owner's next turn."),
            ),
            "Special Conditions",
        ),
        Keyword(
            "<Poisoned>",
            listOf(
                p("Special Condition. Place a Poison marker. During Pokemon Checkup, place 1 damage counter. Only one Poison marker at a time."),
            ),
            "Special Conditions",
        ),
        Keyword(
            "<Prize Cards>",
            listOf(
                p("6 face-down cards set aside during setup. Take 1 when you KO an opponent's Pokemon. Taking your last Prize card wins the game."),
            ),
            "Game Terms",
        ),
        Keyword(
            "<Resistance>",
            listOf(
                p("Reduced damage from certain types (e.g. -20). Printed lower-left. Apply after Weakness. Only affects defending Active Pokemon."),
            ),
            "Game Terms",
        ),
        Keyword(
            "<Retreat>",
            listOf(
                p("Switch Active Pokemon with a Benched one. Discard Energy equal to Retreat Cost. Once per turn. Clears Special Conditions. Asleep/Paralyzed Pokemon cannot retreat."),
            ),
            "Game Terms",
        ),
        Keyword(
            "<Weakness>",
            listOf(
                p("Extra damage from certain types (e.g. x2). Printed lower-left. Apply before Resistance. Only affects defending Active Pokemon."),
            ),
            "Game Terms",
        ),
        Keyword(
            "<Pokemon ex>",
            listOf(
                p("Powerful Pokemon with strong attacks and higher HP. When Knocked Out, opponent takes 2 Prize cards. 'ex' is part of the Pokemon's name."),
            ),
            "Pokemon Types",
        ),
        Keyword(
            "<Pokemon V>",
            listOf(
                p("Powerful Basic Pokemon with high HP and strong attacks. When Knocked Out, opponent takes 2 Prize cards. Pokemon VSTAR and VMAX evolve from Pokemon V."),
            ),
            "Pokemon Types",
        ),
        Keyword(
            "<Pokemon VSTAR>",
            listOf(
                p("Evolves from Pokemon V. Has a VSTAR Power (attack or Ability) that can only be used once per game. When Knocked Out, opponent takes 2 Prize cards. Considered a Pokemon V."),
            ),
            "Pokemon Types",
        ),
        Keyword(
            "<Pokemon VMAX>",
            listOf(
                p("Evolves from Pokemon V. Massive HP (300+) and strong attacks. When Knocked Out, opponent takes 3 Prize cards. Considered a Pokemon V."),
            ),
            "Pokemon Types",
        ),
        Keyword(
            "<Pokemon V-UNION>",
            listOf(
                p("Made of 4 separate cards with the same name. All 4 pieces must be played from discard pile to Bench at the same time. Takes 1 Bench spot. When KO'd, opponent takes 3 Prize cards."),
            ),
            "Pokemon Types",
        ),
        Keyword(
            "<Pokemon-GX>",
            listOf(
                p("Powerful Pokemon with a special GX attack usable once per game. When Knocked Out, opponent takes 2 Prize cards. 'GX' is part of the Pokemon's name."),
            ),
            "Pokemon Types",
        ),
        Keyword(
            "<Pokemon-EX>",
            listOf(
                p("Older powerful Pokemon type. When Knocked Out, opponent takes 2 Prize cards. 'EX' is part of the Pokemon's name. Different from Pokemon ex."),
            ),
            "Pokemon Types",
        ),
        Keyword(
            "<Mega Evolution Pokemon ex>",
            listOf(
                p("Supremely powerful Pokemon ex. When Knocked Out, opponent takes 3 Prize cards. Can appear as Basic, Stage 1, or Stage 2 Pokemon."),
            ),
            "Pokemon Types",
        ),
        Keyword(
            "<Mega Evolution Pokemon-EX>",
            listOf(
                p("Evolves from Pokemon-EX. When you play one, your turn ends immediately. When Knocked Out, opponent takes 2 Prize cards."),
            ),
            "Pokemon Types",
        ),
        Keyword(
            "<TAG TEAM>",
            listOf(
                p("Special Basic Pokemon-GX featuring multiple Pokemon. Very high HP with bonus GX attacks. When Knocked Out, opponent takes 3 Prize cards."),
            ),
            "Pokemon Types",
        ),
        Keyword(
            "<Radiant Pokemon>",
            listOf(
                p("Shiny Basic Pokemon. Only 1 Radiant Pokemon allowed per deck. Always Basic, never evolves."),
            ),
            "Pokemon Types",
        ),
        Keyword(
            "<Tera Pokemon ex>",
            listOf(
                p("Pokemon ex with a crystalline effect that prevents all attack damage while on your Bench. When KO'd, opponent takes 2 Prize cards."),
            ),
            "Pokemon Types",
        ),
        Keyword(
            "<Dual-Type Pokemon>",
            listOf(
                p("A Pokemon with two types simultaneously. Affected by effects targeting either type. Weakness and Resistance apply for both types."),
            ),
            "Pokemon Types",
        ),
        Keyword(
            "<BREAK Evolution>",
            listOf(
                p("Special evolution that keeps the previous Pokemon's attacks and Abilities, then adds more. Changes HP and possibly type. 'BREAK' is part of the name."),
            ),
            "Evolution",
        ),
        Keyword(
            "<Restored Pokemon>",
            listOf(
                p("Fossil Pokemon that can only be played via an Item card effect. Not a Basic Pokemon. Cannot be played from hand normally."),
            ),
            "Evolution",
        ),
        Keyword(
            "<Ancient Trait>",
            listOf(
                p("Special powers on certain Pokemon cards, printed under the name. Not attacks or Abilities — cards that block those won't affect Ancient Traits."),
            ),
            "Misc",
        ),
        Keyword(
            "<ACE SPEC>",
            listOf(
                p("Extremely powerful Trainer cards. Only 1 total ACE SPEC card allowed per deck (not 1 of each — 1 total)."),
            ),
            "Misc",
        ),
        Keyword(
            "<Prism Star>",
            listOf(
                p("Powerful cards limited to 1 copy per name per deck. Sent to Lost Zone instead of discard pile."),
            ),
            "Misc",
        ),
        Keyword(
            "<Lost Zone>",
            listOf(
                p("Cards sent here are permanently removed from the game — they cannot be recovered like the discard pile."),
            ),
            "Misc",
        ),
        Keyword(
            "<Special Conditions>",
            listOf(
                p("Asleep, Burned, Confused, Paralyzed, and Poisoned. Only affect Active Pokemon. Cleared when a Pokemon evolves or moves to the Bench."),
                p("Asleep, Confused, and Paralyzed rotate the card; Burned and Poisoned use markers. A Pokemon can have multiple Special Conditions at once (e.g. Burned + Paralyzed + Poisoned)."),
            ),
            "Special Conditions",
        ),
        Keyword(
            "<Trainer Cards>",
            listOf(
                p("Special cards with four subtypes: Item (unlimited per turn), Supporter (1 per turn), Stadium (1 per turn, stays in play), and Pokemon Tool (attach to a Pokemon, 1 per Pokemon)."),
            ),
            "Game Terms",
        ),
        Keyword(
            "<Abilities>",
            listOf(
                p("Special effects on Pokemon that are not attacks. Some are always active, others you choose to use. Usable from both Active and Benched Pokemon. Does not end your turn."),
            ),
            "Game Terms",
        ),
    )

    val configs: List<RulesConfig> = listOf(
        RulesConfig(
            rulesetId = "2026-standard",
            rulesetName = "2026 — Standard",
            setup = setup,
            winCons = victory,
            playSequence = turnStructure,
            keywords = keywords,
            fieldContent = fieldContent,
            pdfAssets = listOf(
                PdfAsset("Official Rulebook", "rulebooks/pokemon-rulebook.pdf"),
            ),
        ),
    )
}
