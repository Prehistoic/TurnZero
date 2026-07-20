package com.turnzero.games.onepiece.rules

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

object OnePieceRules {

    private val victory = listOf(
        InfoCard(
            "Deliver the Finishing Blow",
            listOf(
                p("If you attack your opponent's Leader and deal damage when they have 0 Life cards remaining, you win the game."),
                p("This is the primary win condition — reduce the opponent's Life to zero, then land one final hit."),
            ),
        ),
        InfoCard(
            "Empty Deck",
            listOf(
                p("If a player has 0 cards in their deck, they lose the game at the next rule processing check."),
                p("This is checked continuously — as soon as a player's deck runs out, they meet a defeat condition."),
            ),
        ),
        InfoCard(
            "Concession",
            listOf(
                p("Either player may concede at any time. Announce your concession and you lose immediately."),
                p("Concession is not affected by any card effect and cannot be forced by any card either."),
            ),
        ),
    )

    private val setup = listOf(
        RuleStep(
            "Shuffle & Place Deck",
            "Thoroughly shuffle your deck, then place it face-down in the deck area.",
            listOf(
                p("Randomize the order of all cards in your deck. Give your opponent the opportunity to shuffle or cut your deck after you're done."),
                p("Place your deck face-down in its designated area. Neither player may look at or change the order of cards in either deck."),
            ),
        ),
        RuleStep(
            "Place Your Leader",
            "Put your Leader card face-up in the Leader area.",
            listOf(
                p("Your Leader card sits face-up in the Leader area and stays there for the entire game — it cannot be moved from this area by any effect or rule."),
                p("Your Leader provides the colors that determine which cards can be included in your deck. Its Life value (printed on the card) determines how many Life cards you'll place in the final setup step."),
                p("If your Leader has an \"At the start of the game\" effect, it is processed now."),
            ),
        ),
        RuleStep(
            "Determine First Player",
            "Play Rock-Paper-Scissors; the winner freely chooses to go first or second.",
            listOf(
                p("Both players play Rock-Paper-Scissors. The winner decides whether to go first or second — no outside intervention is allowed in this decision."),
            ),
        ),
        RuleStep(
            "Draw Opening Hand",
            "Each player draws 5 cards from the top of their deck as their starting hand.",
            listOf(
                p("Both players draw the top 5 cards of their deck simultaneously. Keep your hand secret from your opponent — you may freely rearrange your own cards."),
            ),
        ),
        RuleStep(
            "Mulligan (Optional)",
            "Each player may return their entire hand to the deck, reshuffle, and draw a new hand of 5 cards — once only, starting with the first player.",
            listOf(
                p("Starting with the first player, each player may declare a re-draw. If you do, return all cards from your hand to your deck, reshuffle, and draw a new hand of 5 cards."),
                p("You can only do this once. After taking your mulligan (or choosing not to), you must keep that hand for the rest of the game."),
                p("Tip: If your opening hand has no low-cost cards or no Characters to play early, strongly consider taking the mulligan."),
            ),
        ),
        RuleStep(
            "Place Life Cards",
            "Draw cards equal to your Leader's Life value from the top of your deck and place them face-down in your Life area.",
            listOf(
                p("Draw cards from the top of your deck equal to your Leader's Life value (typically 4 or 5) and place them face-down one at a time in your Life area without looking at their contents."),
                p("The card at the top of your deck ends up at the bottom of your Life stack. This means the first card you placed will be the last one drawn when you take damage."),
                p("Your Life area is your primary defense — when your Leader takes damage, cards are moved from here to your hand. If a card has [Trigger], you may activate it instead of adding it to your hand."),
            ),
        ),
    )

    private val turnStructure = listOf(
        RuleStep(
            "Refresh Phase",
            "Set all your rested cards to active (upright). Return all DON!! cards attached to your Leader and Characters back to your cost area, rested.",
            listOf(
                p("Set all your rested cards in your Leader area, Character area, Stage area, and cost area to the active (upright) position."),
                p("All DON!! cards that were given to (attached to) your Leader and Character cards are returned to your cost area and become rested."),
                p("Effects that last \"until the start of your next turn\" expire now."),
            ),
        ),
        RuleStep(
            "Draw Phase",
            "Draw 1 card from the top of your deck. The first player skips this draw on their very first turn.",
            listOf(
                p("Draw 1 card from the top of your deck and add it to your hand."),
                p("The player going first does not draw a card on their first turn of the game. A card must be drawn in all subsequent draw phases for both players."),
            ),
        ),
        RuleStep(
            "DON!! Phase",
            "Place DON!! cards face-up from your DON!! deck into your cost area: 2 normally, or only 1 on your first turn.",
            listOf(
                p("Normally, take 2 DON!! cards from your DON!! deck and place them face-up in your cost area, set to active."),
                p("On your very first turn, only place 1 DON!! card instead."),
                p("If your DON!! deck has fewer cards than required, place as many as you can. If your DON!! deck is empty, place nothing."),
                p("DON!! cards in your cost area are your primary resource — you rest them to pay for cards and you can attach them to Characters for +1000 power each during your turn."),
            ),
        ),
        RuleStep(
            "Main Phase",
            "Play cards, activate effects, give DON!! to Characters, and battle — any number of times, in any order you choose.",
            listOf(
                sh("Play Cards"),
                p("Pay a card's cost by resting that many active DON!! cards in your cost area:"),
                bl(
                    listOf(
                        "**Play a Character card** — Pay its cost, then place it active in your Character area. You can have a maximum of 5 Characters; if your area is full, you must trash one existing Character first. Characters cannot attack the turn they are played (unless they have [Rush]).",
                        "**Play a Stage card** — Pay its cost, then place it active in your Stage area. You can only have 1 Stage card at a time; if you already have one, trash it before playing the new one. Stages provide ongoing effects while on the field.",
                        "**Play an Event card [Main]** — Reveal the card, pay its cost, resolve its effect, then trash it. Event cards are one-time effects that go to the trash after use.",
                    ),
                ),
                sh("Activate Card Effects"),
                p("Use effects marked [Activate: Main] on your Leader, Characters, or Stages. Pay any listed activation costs. You can do this before, after, or between battles."),
                sh("Give DON!! Cards"),
                p("Take 1 active DON!! card from your cost area and place it under your Leader or one of your Character cards. Each DON!! card attached this way gives that card +1000 power during your turn."),
                p("You can do this as many times as you have active DON!! cards available. Attached DON!! cards return to your cost area (rested) at the start of your next Refresh Phase."),
                sh("Battle"),
                p("Neither player can attack on their first turn. A battle follows these 5 steps in strict order:"),
                sh("1. Attack Declaration"),
                bl(
                    listOf(
                        "Rest (turn sideways) one of your active Leaders or Characters and declare your attack.",
                        "Choose your target: the opponent's Leader, or one of their rested Characters.",
                        "[When Attacking] and [On Your Opponent's Attack] effects trigger and activate now.",
                    ),
                ),
                sh("2. Block Step"),
                bl(
                    listOf(
                        "The defending player may rest one of their Characters with [Blocker] to redirect the attack to that Blocker instead of the original target.",
                        "Only one Blocker can be activated per battle. [On Block] effects trigger if a Blocker is used.",
                    ),
                ),
                sh("3. Counter Step"),
                bl(
                    listOf(
                        "The defending player may play Event cards with [Counter] from their hand by paying their cost.",
                        "They may also trash Character cards with a Counter value from their hand to boost their Leader's or a Character's power by that value for this battle only.",
                        "These actions can be performed in any order, as many times as the defending player wishes.",
                    ),
                ),
                sh("4. Damage Step"),
                bl(
                    listOf(
                        "Compare the attacking card's power to the defending card's power.",
                        "If the attacker's power is greater than or equal to the defender's — the battle is won:",
                        "  - Against a Leader: Deal 1 damage. If the opponent has 0 Life cards after the damage, you win the game immediately.",
                        "  - Against a Character: That Character is K.O.'d and sent to the trash.",
                        "If the attacker's power is lower than the defender's — the battle is lost and nothing happens. No counter-damage is dealt.",
                    ),
                ),
                sh("5. End of Battle"),
                bl(
                    listOf(
                        "End-of-battle effects trigger and resolve.",
                        "All effects that last \"during this battle\" expire.",
                        "Return to the Main Phase — you may continue attacking with other Characters or performing other actions.",
                    ),
                ),
                p("Declare the end of your Main Phase to proceed to the End Phase."),
            ),
        ),
        RuleStep(
            "End Phase",
            "Resolve all end-of-turn effects in order: first [End of Your Turn], then [End of Your Opponent's Turn]. All \"during this turn\" effects expire.",
            listOf(
                p("[End of Your Turn] effects activate and resolve — the turn player chooses the order if multiple effects trigger."),
                p("[End of Your Opponent's Turn] effects then activate and resolve — the non-turn player chooses the order."),
                p("Effects that last \"during this turn\" or \"until the end of the turn\" expire now."),
                p("Your turn ends and your opponent's turn begins with their Refresh Phase."),
            ),
        ),
    )

    private val deckRules = listOf(
        InfoCard(
            "Main Deck",
            listOf(
                p("Exactly 50 cards made up of Character, Event, and Stage cards."),
                p("You can have no more than 4 cards with the same card number."),
            ),
        ),
        InfoCard(
            "DON!! Deck",
            listOf(
                p("Exactly 10 DON!! cards — these are your resource cards used to pay costs."),
            ),
        ),
        InfoCard(
            "Leader Card",
            listOf(
                p("1 Leader card, separate from your main deck. Provides the colors that determine which cards can be included."),
            ),
        ),
        InfoCard(
            "Color Restrictions",
            listOf(
                p("All cards must include at least one color that matches your Leader card. Cards with colors outside your Leader's colors cannot be included."),
            ),
        ),
    )

    private val fieldContent = listOf(
        img("rulebooks/images/onepiece-field.png"),
        p("Your play area is arranged as shown above. Cards on the field are either Active (upright) or Rested (sideways). When paying costs or attacking, you rest cards. During the Refresh Phase, all cards return to active."),
        sh("The Eight Areas"),
        bl(
            listOf(
                "**1. Character area** — Your Character cards sit here face-up. Maximum 5. Characters enter active and cannot attack on the turn they're played (unless they have Rush).",
                "**2. Leader area** — Your Leader card, always face-up. Provides colors for deck building, has a Life value. Cannot be moved from this area by any effect or rule.",
                "**3. Stage area** — Your Stage card (max 1). Provides ongoing effects while on the field.",
                "**4. Deck** — Your 50-card draw pile, face-down. A secret area — neither player can look at or change its order.",
                "**5. Trash** — K.O.'d Characters and used Events go here face-up. An open area — both players can look through it.",
                "**6. Cost area** — Your DON!! cards. Cards here are either active (available to pay costs) or rested (used). During your turn, you can give active DON!! cards to Leader/Characters for +1000 power each.",
                "**7. DON!! deck** — 10 DON!! cards, face-down. An open area — both players can view its contents.",
                "**8. Life area** — Face-down cards equal to your Leader's Life value. When your Leader takes damage, move the top card to your hand. If a card has [Trigger], you may activate it instead.",
            ),
        ),
        sh("Active vs. Rested"),
        p("Active cards are upright and can attack, block, or be used to pay costs. Rested cards are sideways and can't be used for anything until readied."),
        p("When a Character with given DON!! cards leaves the field (K.O.'d, returned to hand, etc.), the attached DON!! cards return to your cost area and are rested."),
    )

    private val keywords = listOf(
        Keyword(
            "[Rush]",
            listOf(
                p("A Character with Rush can attack on the same turn it is played. Normally, Characters must wait until the next turn."),
            ),
            "Combat",
        ),
        Keyword(
            "[Rush: Character]",
            listOf(
                p("A Character with Rush: Character can attack opponent's Characters on the turn it is played, but not the opponent's Leader."),
            ),
            "Combat",
        ),
        Keyword(
            "[Double Attack]",
            listOf(
                p("When this card deals damage to the opponent's Leader, it deals 2 damage instead of 1. The opponent processes 2 Life cards."),
            ),
            "Combat",
        ),
        Keyword(
            "[Banish]",
            listOf(
                p("When this card deals damage to the opponent's Leader, the Life card is trashed directly — it is not added to their hand, and its [Trigger] cannot be activated."),
            ),
            "Combat",
        ),
        Keyword(
            "[Blocker]",
            listOf(
                p("During the Block Step, you may rest this Character to have it take the place of the target being attacked. The attacker now faces the Blocker instead."),
                p("Only one Blocker can be activated per battle."),
            ),
            "Combat",
        ),
        Keyword(
            "[Unblockable]",
            listOf(
                p("When this card attacks, your opponent cannot activate [Blocker]. The attack cannot be redirected and will hit its intended target."),
            ),
            "Combat",
        ),
        Keyword(
            "[On Play]",
            listOf(
                p("An effect that automatically activates when this Character or Stage card is played onto the field."),
            ),
            "Timing",
        ),
        Keyword(
            "[When Attacking]",
            listOf(
                p("An effect that automatically activates when this card declares an attack during your Attack Step."),
            ),
            "Timing",
        ),
        Keyword(
            "[On Block]",
            listOf(
                p("An effect that automatically activates when this card uses its [Blocker] ability during the Block Step."),
            ),
            "Timing",
        ),
        Keyword(
            "[On K.O.]",
            listOf(
                p("An effect that activates when this Character is K.O.'d (trashed by battle or effect). The effect activates on the field, but resolves after the card is in the trash."),
                p("Only activates when the card moves to an open area — K.O.'d is different from being trashed by other means (e.g., making room for a 6th Character)."),
            ),
            "Timing",
        ),
        Keyword(
            "[On Your Opponent's Attack]",
            listOf(
                p("An effect that activates when your opponent declares an attack, after their [When Attacking] effects have resolved."),
            ),
            "Timing",
        ),
        Keyword(
            "[End of Your Turn]",
            listOf(
                p("An effect that activates during the End Phase of your turn. If you have multiple, you choose the activation order."),
            ),
            "Timing",
        ),
        Keyword(
            "[End of Your Opponent's Turn]",
            listOf(
                p("An effect that activates during the End Phase of your opponent's turn. If the opponent has multiple, they choose the order."),
            ),
            "Timing",
        ),
        Keyword(
            "[Activate: Main]",
            listOf(
                p("An effect you can manually activate during your Main Phase (but not during a battle). Pay any listed activation cost to use it."),
            ),
            "Activation",
        ),
        Keyword(
            "[Main]",
            listOf(
                p("Found on Event cards. You can play this Event during your Main Phase (not during battle) by paying its cost. After resolving, the card goes to the trash."),
            ),
            "Activation",
        ),
        Keyword(
            "[Counter]",
            listOf(
                p("Found on Event cards. You can play this Event during the Counter Step of your opponent's attack by paying its cost. After resolving, the card goes to the trash."),
                p("Counter cannot be activated by other card effects unless the effect specifically says \"activate [Counter]\"."),
            ),
            "Activation",
        ),
        Keyword(
            "[Once Per Turn]",
            listOf(
                p("This effect can only be activated and resolved once during each turn. If the card moves to another area and returns, it's treated as a new card and can activate again."),
            ),
            "Conditions",
        ),
        Keyword(
            "[Your Turn]",
            listOf(
                p("This effect's condition is only met during your own turn. The effect is inactive during your opponent's turn."),
            ),
            "Conditions",
        ),
        Keyword(
            "[Opponent's Turn]",
            listOf(
                p("This effect's condition is only met during your opponent's turn. The effect is inactive during your own turn."),
            ),
            "Conditions",
        ),
        Keyword(
            "[DON!! xX]",
            listOf(
                p("This effect's condition is met when this card has X or more DON!! cards attached to it (from the Give DON!! action). The effect only activates when the DON!! threshold is reached."),
            ),
            "Conditions",
        ),
        Keyword(
            "DON!! -X",
            listOf(
                p("A cost requirement: return a total of X DON!! cards from your Leader area, Character area, and/or cost area back to your DON!! deck. Active or rested — both count."),
            ),
            "Costs",
        ),
        Keyword(
            "Rest Symbol",
            listOf(
                p("A cost requirement: rest the specified number of active DON!! cards in your cost area. The number in the circle symbol tells you how many DON!! to rest (e.g. a circled 1 means rest 1 DON!!)."),
                p("This is the most common activation cost — you read the number in the symbol as the number of DON!! to rest."),
            ),
            "Costs",
        ),
        Keyword(
            "[Trigger]",
            listOf(
                p("When a Life card with [Trigger] is added to your hand from taking damage, you may reveal it and activate its [Trigger] effect instead of adding it to your hand."),
                p("You can choose not to activate it and add the card to your hand normally. After resolving a [Trigger], the card is trashed unless the effect says otherwise."),
            ),
            "Misc",
        ),
        Keyword(
            "K.O.",
            listOf(
                p("A Character card being trashed as a result of losing a battle or a card effect. K.O.'d cards go to the trash."),
                p("Trashing a Character to make room for a 6th Character (the 5-card limit rule) is NOT a K.O. — [On K.O.] won't trigger."),
            ),
            "Misc",
        ),
    )

    val configs: List<RulesConfig> = listOf(
        RulesConfig(
            rulesetId = "v1.2-comprehensive",
            rulesetName = "v1.2.0 — Comprehensive",
            setup = setup,
            victoryRules = victory,
            playSequence = turnStructure,
            keywords = keywords,
            deckRules = deckRules,
            fieldContent = fieldContent,
            pdfAssets = listOf(
                PdfAsset("Official Rule Manual", "rulebooks/onepiece-rule-manual.pdf"),
                PdfAsset("Comprehensive Rules", "rulebooks/onepiece-comprehensive.pdf"),
            ),
        ),
    )
}
