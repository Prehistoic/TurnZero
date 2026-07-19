package com.turnzero.games.swu.rules

import com.turnzero.tools.rules.ContentBlock
import com.turnzero.tools.rules.Keyword
import com.turnzero.tools.rules.PdfAsset
import com.turnzero.tools.rules.RuleStep
import com.turnzero.tools.rules.RulesConfig
import com.turnzero.tools.rules.InfoCard

object SwuRules {

    private fun p(text: String) = ContentBlock.Paragraph(text)
    private fun sh(text: String) = ContentBlock.SubHeading(text)
    private fun bl(items: List<String>) = ContentBlock.BulletList(items)
    private fun img(path: String) = ContentBlock.Image("asset:$path")

    private val victory = listOf(
        InfoCard(
            "Reduce the Enemy Base to 0 HP",
            listOf(
                p("Each base starts with 30 HP. Deal enough damage to the enemy base to reduce it to 0 HP and you win the game."),
                p("If both bases reach 0 HP at the same time, the game is a draw."),
            ),
        ),
        InfoCard(
            "Empty Deck",
            listOf(
                p("If you run out of cards in your deck, you keep playing. Each time you would draw a card from an empty deck, you instead deal 3 damage to your base per card."),
                p("For example, drawing 2 cards during the regroup phase with an empty deck deals 6 damage to your base."),
            ),
        ),
        InfoCard(
            "Concession",
            listOf(
                p("You may concede at any time. Simply announce that you concede and you lose immediately."),
            ),
        ),
    )

    private val setup = listOf(
        RuleStep(
            "Place Your Base",
            "Put your base at the top of your play area, facing your opponent's base.",
            listOf(
                p("Each base has its own HP value shown on the card. It provides 1 aspect icon for playing cards."),
            ),
        ),
        RuleStep(
            "Place Your Leader",
            "Put your leader below your base, horizontal (non-unit) side faceup.",
            listOf(
                p("Your leader provides 2 aspect icons. Its Epic Action ability will let you deploy it as a powerful unit later in the game."),
            ),
        ),
        RuleStep(
            "Determine First Player",
            "Randomly choose who gets the initiative counter.",
            listOf(
                p("Randomly pick a player. That player decides who starts with the initiative counter — they can keep it or give it to their opponent."),
                p("The player with the initiative takes the first action of the game."),
            ),
        ),
        RuleStep(
            "Shuffle & Draw",
            "Shuffle your deck, then draw 6 cards.",
            listOf(
                p("Both players shuffle their decks and draw 6 cards as their opening hand."),
            ),
        ),
        RuleStep(
            "Mulligan (Optional)",
            "You may shuffle your hand back and draw a new hand of 6 cards.",
            listOf(
                p("The player with the initiative decides first, then their opponent."),
                p("You can only take one mulligan, and you must keep the new hand."),
                p("Tip: Take a mulligan if your hand has no units that cost 1 or 2."),
            ),
        ),
        RuleStep(
            "Resource Two Cards",
            "Choose 2 cards from your hand and put them into play facedown and ready as resources.",
            listOf(
                p("Resources are used to pay for cards you play. Each player must resource exactly 2 cards."),
                p("A card used as a resource is placed facedown — its text and abilities are inactive while it's a resource."),
                p("The game begins with the first action phase."),
            ),
        ),
    )

    private val turnStructure = listOf(
        RuleStep(
            "Action Phase",
            "Players take turns doing one action at a time until both pass in a row.",
            listOf(
                p("The player with the initiative goes first, then the opponent, then back and forth. On your turn, you may do one of these:"),
                bl(
                    listOf(
                        "**Play a Card** — Pay a card's cost by exhausting that many resources. Units and upgrades enter play exhausted. Events go straight to your discard pile after resolving.",
                        "**Attack With a Unit** — Exhaust a ready unit and choose an enemy unit in the same arena (ground or space), or attack the enemy base directly. Units deal damage equal to their power.",
                        "**Use an Action Ability** — Pay any cost in brackets after the word \"Action\" or \"Epic Action\" and resolve the effect.",
                        "**Take the Initiative** — Take the initiative counter and flip it. Only one player can do this per round. After this, you pass for the rest of the phase but will go first next round.",
                        "**Pass** — Do nothing this turn. You can still take actions later if your opponent acts after you.",
                    ),
                ),
                p("The phase ends when both players pass consecutively (or one takes the initiative right after the other passes)."),
            ),
        ),
        RuleStep(
            "Draw Cards",
            "Each player draws 2 cards from their deck.",
            listOf(
                p("If your deck is empty, instead deal 3 damage to your base for each card you would have drawn."),
            ),
        ),
        RuleStep(
            "Resource a Card",
            "You may put 1 card from your hand into play facedown and exhausted as a resource.",
            listOf(
                p("The player with the initiative decides first, then their opponent. You may choose not to resource anything."),
                p("Tip: Resource a card each round until you have enough to deploy your leader, then consider whether you need more."),
            ),
        ),
        RuleStep(
            "Ready All Cards",
            "Ready all your exhausted cards — units, resources, and your leader.",
            listOf(
                p("All of your cards stand back up, ready for the next action phase."),
                p("After readying, a new round begins."),
            ),
        ),
    )

    private val keywords = listOf(
        Keyword(
            "Ambush",
            listOf(
                p("When you play this unit, it may ready and attack an enemy unit immediately. It enters play exhausted like normal, so it must use Ambush to ready and attack in the same turn. If there are no enemy units to attack, Ambush does nothing."),
            ),
            "Combat",
        ),
        Keyword(
            "Grit",
            listOf(
                p("This unit gets +1 power for each point of damage on it. The more injured it is, the harder it hits back."),
            ),
            "Combat",
        ),
        Keyword(
            "Overwhelm",
            listOf(
                p("When this unit attacks and deals more damage than needed to defeat the defender, the extra damage hits the enemy base. Example: A 5-power unit with Overwhelm attacks a unit with 3 HP remaining — it deals 2 excess damage to the enemy base."),
            ),
            "Combat",
        ),
        Keyword(
            "Raid X",
            listOf(
                p("While attacking, this unit gets +X power. Example: A unit with Raid 2 gets +2 power while it's the attacker."),
            ),
            "Combat",
        ),
        Keyword(
            "Restore X",
            listOf(
                p("When this unit attacks, heal X damage from your base before damage is dealt. Example: A unit with Restore 2 heals 2 damage from your base each time it attacks."),
            ),
            "Combat",
        ),
        Keyword(
            "Saboteur",
            listOf(
                p("When this unit attacks, you can ignore enemy Sentinel units and choose any target. Also, remove all Shield tokens from the defender before dealing damage."),
            ),
            "Combat",
        ),
        Keyword(
            "Sentinel",
            listOf(
                p("Enemy units in the same arena can't attack your non-Sentinel units or your base — they must attack your Sentinel unit first. If you control multiple Sentinels, the attacker chooses which one to attack."),
            ),
            "Combat",
        ),
        Keyword(
            "Shielded",
            listOf(
                p("When you play this unit, give it a Shield token. The shield absorbs all damage from the next hit, then is removed. Multiple shields don't stack — each Shielded gives one shield."),
            ),
            "On Play",
        ),
        Keyword(
            "Bounty",
            listOf(
                p("When this unit is defeated, the opponent who defeated it may trigger the listed Bounty effect. Each Bounty is an optional reward for taking down the unit. If you defeat your own unit with Bounty, your opponent still collects."),
            ),
            "Triggered",
        ),
        Keyword(
            "Smuggle [Y]",
            listOf(
                p("You may play this card from your resource zone (instead of from your hand) by paying cost Y. The card in your resource zone is replaced by the top card of your deck. Useful for accessing extra cards you stashed as resources early on."),
            ),
            "Resources",
        ),
        Keyword(
            "Coordinate",
            listOf(
                p("The ability listed after Coordinate is only active while you control 3 or more units (including this one). If you drop below 3 units, the ability turns off until you build your board back up."),
            ),
            "Misc",
        ),
        Keyword(
            "Exploit X",
            listOf(
                p("While playing this card, you may defeat up to X of your own units. For each unit you sacrifice this way, the card costs 2 less. Sacrificing cheap units lets you play expensive cards much earlier."),
            ),
            "Misc",
        ),
        Keyword(
            "Piloting [Y]",
            listOf(
                p("You may play this unit as an upgrade on a friendly VEHICLE unit instead of as a unit, paying cost Y instead of its printed cost. The attached unit gains the pilot's power, HP, and abilities. Useful for upgrading vehicles with fighter pilots."),
            ),
            "Misc",
        ),
        Keyword(
            "Hidden",
            listOf(
                p("This unit can't be attacked during the same phase it was played. It's safe for one round before becoming a valid target. Note: Sentinel overrides Hidden — a unit with both can still be attacked."),
            ),
            "Defense",
        ),
        Keyword(
            "Plot",
            listOf(
                p("When you deploy your leader, you may play this card from your resource zone at its normal cost. The card is replaced by the top card of your deck. You can play multiple Plot cards when deploying — show your opponent all of them, then resolve in any order."),
            ),
            "Resources",
        ),
        Keyword(
            "Support",
            listOf(
                p("When you play this unit, you may immediately attack with another unit. That unit gains all of this unit's other abilities for that attack. Great for setting up combo attacks with a buffed ally."),
            ),
            "On Play",
        ),
    )

    private val deckRules = listOf(
        InfoCard(
            "Card Limits",
            listOf(
                p("Minimum 50 cards (no maximum limit) in Premier/Eternal format."),
                p("Exactly 1 Leader and 1 Base — these exist alongside your deck and don't count toward the minimum."),
                p("Up to 3 copies of any unit, event, or upgrade card."),
            ),
        ),
        InfoCard(
            "Aspect Rules",
            listOf(
                p("You may include cards of ANY aspect in your deck, regardless of your Leader and Base aspects."),
                p("When playing a card with aspect icons beyond those provided by your Leader and Base, you must pay 2 extra resources per unmatched icon — this is the \"aspect penalty.\""),
                p("Neutral cards (gray border, no aspect icons) never incur the aspect penalty."),
            ),
        ),
        InfoCard(
            "Sideboard",
            listOf(
                p("Up to 10 cards to swap between games in a match."),
            ),
        ),
    )

    private val fieldContent = listOf(
        img("rulebooks/images/swu-field.png"),
        sh("Play Area Layout"),
        p("Your play area is arranged as follows:"),
        bl(
            listOf(
                "**Base zone** — Your base card sits top center. This is what your opponent is trying to destroy.",
                "**Leader zone** — Your leader sits below your base, starting on its horizontal (non-unit) side faceup.",
                "**Ground arena** — Ground units are played in a row on one side of your base. Ground units can only attack other ground units, or the enemy base directly.",
                "**Space arena** — Space units are played in a row on the other side of your base. Space units can only attack other space units, or the enemy base directly.",
                "**Resource zone** — Cards placed facedown here become resources, used to pay costs.",
                "**Deck** — Your draw pile. Cards enter your hand from here.",
                "**Discard pile** — Defeated units and used events go here faceup.",
            ),
        ),
        sh("Ground & Space Arenas"),
        p("Combat is divided into two separate arenas: ground and space. Each unit card shows its arena type at the top."),
        p("A ground unit can't attack a space unit, and a space unit can't attack a ground unit — they fight in separate lanes."),
        p("However, any unit can attack the enemy base directly, regardless of arena."),
        sh("Ready & Exhausted"),
        p("Cards are either ready (upright) or exhausted (turned sideways)."),
        p("Units and resources enter play exhausted, then become ready at the end of the regroup phase."),
        p("You must exhaust resources to pay for cards. You must exhaust a unit to attack with it. An exhausted card can't be exhausted again until it's readied."),
    )

    val configs: List<RulesConfig> = listOf(
        RulesConfig(
            rulesetId = "v8-comprehensive",
            rulesetName = "v8.0 - Comprehensive",
            setup = setup,
            victoryRules = victory,
            playSequence = turnStructure,
            keywords = keywords,
            fieldContent = fieldContent,
            deckRules = deckRules,
            pdfAssets = listOf(
                PdfAsset("Quickstart Rules", "rulebooks/swu-quickstart.pdf"),
                PdfAsset("Comprehensive Rules 8.0", "rulebooks/swu-comprehensive-v8.pdf"),
            ),
        ),
    )
}
