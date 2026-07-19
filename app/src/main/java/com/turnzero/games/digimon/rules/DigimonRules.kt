package com.turnzero.games.digimon.rules

import com.turnzero.tools.rules.ContentBlock
import com.turnzero.tools.rules.Keyword
import com.turnzero.tools.rules.PdfAsset
import com.turnzero.tools.rules.RuleStep
import com.turnzero.tools.rules.RulesConfig
import com.turnzero.tools.rules.WinCondition

object DigimonRules {

    private fun p(text: String) = ContentBlock.Paragraph(text)
    private fun sh(text: String) = ContentBlock.SubHeading(text)
    private fun bl(items: List<String>) = ContentBlock.BulletList(items)
    private fun img(path: String) = ContentBlock.Image("asset:$path")

    private val victory = listOf(
        WinCondition(
            "Attack With Zero Security",
            listOf(
                p("When your Digimon attacks your opponent directly and their security stack has 0 cards, your attack is successful and you win the game."),
                p("This is the primary win condition — reduce the opponent's security to zero, then land one more attack."),
            ),
        ),
        WinCondition(
            "Empty Deck",
            listOf(
                p("If a player has 0 cards in their deck and cannot draw during their draw phase, they lose the game."),
                p("This is checked continuously — once a player's deck runs out, they meet a defeat condition."),
            ),
        ),
        WinCondition(
            "Card Effect",
            listOf(
                p("Some card effects can cause a player to win or lose the game. When such an effect resolves, the game ends accordingly."),
            ),
        ),
        WinCondition(
            "Concession",
            listOf(
                p("Either player may declare a forfeit at any time. The conceding player immediately loses."),
                p("A forfeit is never forced and effects don't trigger for a loss by forfeit."),
            ),
        ),
    )

    private val setup = listOf(
        RuleStep(
            "Prepare Your Decks",
            "Gather your 50-card deck, 0–5 card Digi-Egg deck, memory gauge, and marker.",
            listOf(
                p("Your main deck must contain exactly 50 cards made up of Digimon, Tamer, and Option cards."),
                p("You can include up to 4 copies of the same card (by card number)."),
                p("Your Digi-Egg deck can have between 0 and 5 cards — it can even start empty."),
                p("Set the memory gauge between both players with the marker starting at 0 in the center. The left side is your memory, the right side is your opponent's. Memory can never exceed 10."),
                p("Place the Index (quick reference guide for keyword effects) to the side of your play area, away from your other cards."),
            ),
        ),
        RuleStep(
            "Shuffle & Place Decks",
            "Shuffle your main deck and Digi-Egg deck, then place both face-down on the field.",
            listOf(
                p("Thoroughly randomize both your main deck and your Digi-Egg deck."),
                p("Give your opponent the opportunity to shuffle or cut your decks after you're done."),
                p("Place your main deck face-down in the deck area, and your Digi-Egg deck face-down in its designated area. Neither player may look at or change the order of cards in either deck."),
            ),
        ),
        RuleStep(
            "Determine First Player",
            "Play Rock-Paper-Scissors; the winner chooses who goes first.",
            listOf(
                p("Both players play Rock-Paper-Scissors. The winner freely decides whether to go first or second — no outside intervention is allowed."),
            ),
        ),
        RuleStep(
            "Draw Opening Hand",
            "Each player draws 5 cards from their deck as their starting hand.",
            listOf(
                p("Both players draw the top 5 cards of their deck. This is your initial hand — keep it secret from your opponent."),
                p("You may freely look at and rearrange the cards in your own hand at any time."),
            ),
        ),
        RuleStep(
            "Mulligan (Optional)",
            "Each player may redraw their entire hand once, starting with the first player.",
            listOf(
                p("Starting with the first player, each player may declare a re-draw. If you do, return all cards from your hand to your deck, reshuffle, and draw a new hand of 5 cards."),
                p("You can only do this once — after taking your mulligan (or choosing not to), you must keep that hand."),
                p("Tip: If your opening hand has no low-level Digimon or no rookies to hatch, strongly consider taking the mulligan."),
            ),
        ),
        RuleStep(
            "Place Security Stack",
            "Take the top 5 cards off your deck and place them face-down, one at a time, as your security.",
            listOf(
                p("Take 5 cards from the top of your deck and place them face-down one at a time without looking at their faces. The card at the top of your deck ends up at the bottom of your security stack."),
                p("Spread the security cards out so the number of cards can be seen by both players. You cannot change the order of cards in your security stack."),
                p("When your opponent attacks you directly, the top card of your security stack is checked. If it has a [Security] effect, you may activate it."),
            ),
        ),
    )

    private val turnStructure = listOf(
        RuleStep(
            "Unsuspend Phase",
            "Set all your suspended cards back to unsuspended (vertical). [Start of Turn] effects trigger first.",
            listOf(
                p("Set all your suspended cards in the battle area and breeding area to the unsuspended (vertical) position. Cards don't need to be unsuspended in any particular order."),
                p("[Start of Your Turn] effects trigger and resolve before any cards are unsuspended. If the turn end conditions are met after these effects resolve, the turn ends before unsuspending occurs."),
                p("A Digimon with <Reboot> also unsuspends during your opponent's unsuspend phase."),
            ),
        ),
        RuleStep(
            "Draw Phase",
            "Draw 1 card from your deck. The first player skips this on their very first turn.",
            listOf(
                p("Draw 1 card from the top of your deck and add it to your hand."),
                p("The player going first does not draw a card on their first turn of the game. A card must be drawn in all subsequent draw phases for both players."),
            ),
        ),
        RuleStep(
            "Breeding Phase",
            "Choose one: hatch a Digi-Egg from your Digi-Egg deck, move a Digimon from breeding area to battle area, or do nothing.",
            listOf(
                p("During the breeding phase, you perform exactly one of these three actions:"),
                sh("Hatch a Digi-Egg"),
                p("Take the top card of your Digi-Egg deck and place it face-up in the breeding area. You can't hatch if you have no cards left in your Digi-Egg deck or if you already have a Digimon in the breeding area."),
                sh("Move to Battle Area"),
                p("Move your Digimon from the breeding area to the battle area. It keeps its current orientation (suspended or unsuspended) and any effects applied to it. Note: its effect status (e.g. pending activations) is NOT carried over during the move."),
                sh("Do Nothing"),
                p("Proceed directly to the Main Phase without performing either action above."),
                p("Important: Only 1 card can be in the breeding area at a time. Cards in the breeding area are highly protected — they can't be targeted by effects, their effects don't trigger or activate, and their information can't be referenced (unless an effect explicitly specifies the breeding area)."),
            ),
        ),
        RuleStep(
            "Main Phase",
            "Play cards, digivolve, use Options, link, attack, and activate effects — in any order, any number of times.",
            listOf(
                sh("Play a Digimon or Tamer"),
                p("Pay a card's play cost by moving the memory marker toward your opponent's side by that amount. The card is placed unsuspended in the battle area. A Digimon cannot attack on the turn it is played (unless it has <Rush>)."),
                p("Special play mechanics:"),
                bl(
                    listOf(
                        "**DigiXros** — For cards with DigiXros requirements (e.g. \"DigiXros -2: [Shoutmon]×[Ballistamon]\"), you may place the specified cards from your hand and/or battle area under the played card. The play cost is reduced by the specified amount for each card placed. You must place at least 1 card.",
                        "**[Assembly]** — Similar to DigiXros. Place specified cards from your hand under the played Digimon to reduce its play cost by the amount shown.",
                    ),
                ),
                sh("Digivolve"),
                p("Stack a Digimon card from your hand onto one of your Digimon on the field by paying its digivolution cost (move the memory marker right by that amount). The previous top card becomes a digivolution card under the new one, and the new Digimon gains all inherited effects from cards below it."),
                bl(
                    listOf(
                        "**DNA Digivolution** — Combine two or more of your Digimon in the battle area into a single Digimon card from your hand that has DNA Digivolution requirements.",
                        "**Burst Digivolve** — A special digivolution method with its own requirements.",
                        "**App Fusion** — A unique digivolution mechanic for Appmon cards.",
                        "**<Blast Digivolve>** — Digivolve one of your Digimon into this card from your hand without paying cost. Can be used on your opponent's turn.",
                        "**<Digisorption>** — When digivolving into this card, you may suspend one of your Digimon to reduce the digivolution cost by the specified amount.",
                    ),
                ),
                sh("Use an Option Card"),
                p("Pay the cost of an Option card from your hand, resolve its effect, then trash it. You must have a Digimon or Tamer on your field that shares a color with the Option card to meet its color requirements."),
                sh("Link a Card"),
                p("Plug 1 card from your hand or battle area sideways under one of your Digimon in the battle area as a link card. A Digimon can have a maximum of 1 link card (unless it has <Link +>). The linked Digimon gains the link card's DP bonus and link effects. Link cards are not considered to be stacked cards or on the field."),
                sh("Attack"),
                p("Suspend (rest) one of your Digimon in the battle area and declare an attack. Your target can be either the opponent directly (performing a security check if they have security) or one of their Digimon (which results in a battle comparing DP)."),
                p("Attack procedure: ① Declare attack & choose target → ② [Counter] timing (opponent may activate one [Counter] effect) → ③ Block timing (opponent may redirect to a <Blocker>) → ④ Confirm success (resolve security check or battle) → ⑤ End of attack."),
                sh("Activate Effects"),
                p("Use [Main] effects on your cards during this phase, when there is no unresolved processing."),
                sh("Pass"),
                p("Declare a pass to immediately move the memory marker to 3 on your opponent's side. This can be a strategic way to end your turn quickly and deny your opponent too much memory."),
            ),
        ),
        RuleStep(
            "End of Turn",
            "When memory reaches 1 or more on your opponent's side and all processing is resolved, your turn ends and your opponent's turn begins.",
            listOf(
                p("The turn end conditions are met when the memory marker is at 1 or more on your opponent's side and all current processing is complete. The turn ends with the current phase (it does not proceed to the next phase)."),
                sh("End of Turn Sequence"),
                bl(
                    listOf(
                        "**1.** [End of Your Turn] effects trigger and activate — the turn player chooses the order if multiple trigger.",
                        "**2.** [End of Opponent's Turn] effects trigger and activate — the non-turn player chooses their order.",
                        "**3.** [End of All Turns] effects trigger and activate.",
                        "**4.** <Overflow> effects are resolved — first the turn player resolves all of theirs one at a time, then the non-turn player resolves all of theirs.",
                        "**5.** All \"during this turn\" and \"until end of turn\" effects expire.",
                    ),
                ),
                p("If the memory moves to 0 or more on your side during end-of-turn processing, the turn end is postponed and the current phase continues."),
                p("Once all end-of-turn processing is resolved, the turn passes to your opponent."),
            ),
        ),
    )

    private val fieldContent = listOf(
        img("rulebooks/images/digimon-field.png"),
        p("Your play area is arranged as shown above. Cards are either Unsuspended (vertical) or Suspended (horizontal). When paying costs or attacking, you suspend cards."),
        sh("The Game Areas"),
        bl(
            listOf(
                "**① Deck** — Your 50-card draw pile, face-down. A private area — neither player can look at or change its order.",
                "**② Digi-Egg Deck** — 0–5 card deck, face-down. A private area where your egg cards wait to be hatched.",
                "**③ Breeding Area** — Where hatched Digimon go. Max 1 card. Cards here are protected from nearly all effects.",
                "**④ Battle Area** — Your main play zone. Any number of cards can be here. Digimon, Tamers, and Option cards all go here when played.",
                "**⑤ Trash** — Discard pile, face-up. A public area — both players can look through it at any time.",
                "**⑥ Security Stack** — 5 face-down cards from the start of the game. When attacked directly, the top security card is checked.",
                "**⑦ Hand** — Your cards held in secret. You may freely rearrange them.",
            ),
        ),
        sh("The Memory Gauge"),
        p("Memory is the shared resource used to pay costs. 0 is the center. Numbers on your left are your memory; numbers on your right are your opponent's memory. Memory never exceeds 10."),
        p("When you pay a cost, move the marker right toward your opponent. When you \"gain memory,\" move it left toward you. Loss of memory moves it right."),
        p("Your turn ends when the memory reaches 1 or more on your opponent's side. The more you do on your turn, the more memory you give your opponent for their turn."),
    )

    private val keywords = listOf(
        Keyword(
            "<ACE>",
            listOf(
                p("ACE cards are a special card type with <Overflow>. When an ACE card leaves the field or from under a card to another area, the memory marker moves toward your opponent's side by the <Overflow> value."),
                p("ACE cards have \"ACE\" in their name and provide powerful counterplay options at the risk of giving your opponent memory when removed."),
            ),
            "Misc",
        ),
        Keyword(
            "<All Turns>",
            listOf(
                p("This effect is active during both your turn and your opponent's turn."),
            ),
            "Timing",
        ),
        Keyword(
            "<Alliance>",
            listOf(
                p("When this Digimon attacks, you may suspend one of your other Digimon to add its DP to the attacker and give it <Security A. +1> for the attack. The DP added is the value at time of suspension and is not modified later."),
            ),
            "Combat",
        ),
        Keyword(
            "<App Fusion>",
            listOf(
                p("A digivolution mechanic where two linked Digimon in the battle area digivolve together into a single Digimon card from your hand by paying the App Fusion cost."),
            ),
            "Evolution",
        ),
        Keyword(
            "<Armor Purge>",
            listOf(
                p("When this Digimon would be deleted, you may trash the top card of its stack to prevent the deletion. This is an immediate-type effect with an optional processing condition."),
            ),
            "Protection",
        ),
        Keyword(
            "<Ascension>",
            listOf(
                p("When this card is deleted, you may place it face-down at the top of your security stack."),
            ),
            "Evolution",
        ),
        Keyword(
            "<Assembly>",
            listOf(
                p("A play mechanic where you place the specified number of cards from your trash under the Digimon being played. The play cost is reduced by the specified value."),
                p("Similar to <DigiXros>, but cards are taken from your trash instead of hand/battle area."),
            ),
            "Evolution",
        ),
        Keyword(
            "<Barrier>",
            listOf(
                p("When this Digimon would be deleted in battle, you may trash the top card of your security stack to prevent the deletion."),
            ),
            "Protection",
        ),
        Keyword(
            "<Blast Digivolve>",
            listOf(
                p("One of your Digimon in the battle area may digivolve into this card from your hand without paying the cost. The activation is optional."),
            ),
            "Evolution",
        ),
        Keyword(
            "<Blast DNA Digivolve>",
            listOf(
                p("One of your Digimon specified in the effect and a card from your hand may DNA digivolve into this card without paying the cost. The card's DNA digivolution requirements cannot be ignored."),
            ),
            "Evolution",
        ),
        Keyword(
            "<Blitz>",
            listOf(
                p("This Digimon can attack if your opponent has 1 or more memory. Once declared, the attack proceeds even if memory moves back to 0 or more. Cannot be used if memory is already at 0 or more when activating."),
            ),
            "Movement",
        ),
        Keyword(
            "<Blocker>",
            listOf(
                p("During your opponent's attack, you may suspend this Digimon to redirect the attack to it. Only one Blocker can be activated per attack, and the attack target itself cannot block."),
                p("A block can only be performed if the attacking Digimon is in the battle area."),
            ),
            "Combat",
        ),
        Keyword(
            "<Burst Digivolve>",
            listOf(
                p("A digivolution mechanic where you return one of your Tamers from the battle area to your hand to digivolve a specified Digimon into this card by paying the burst digivolve cost."),
                p("At the end of the turn it was burst digivolved, the top card of the stack is trashed unless there are no more cards under it."),
            ),
            "Evolution",
        ),
        Keyword(
            "<Collision>",
            listOf(
                p("While this Digimon is attacking, all of your opponent's Digimon gain <Blocker> and your opponent is forced to block if possible during the block timing."),
            ),
            "Combat",
        ),
        Keyword(
            "<Counter>",
            listOf(
                p("An effect that can be activated during the Counter Timing after your opponent declares an attack. Only one <Counter> effect can be activated per attack. Cannot be activated by other card effects unless the effect specifically says \"activate <Counter>.\""),
            ),
            "Timing",
        ),
        Keyword(
            "<DNA Digivolution>",
            listOf(
                p("A digivolution mechanic where two or more of your Digimon are stacked together to digivolve into a single Digimon card from your hand by paying the DNA digivolution cost."),
                p("The cards placed under the DNA-digivolved Digimon become new digivolution cards. The new Digimon is always placed unsuspended, regardless of the previous orientation."),
                p("DNA digivolution can only be performed by effects that specifically say DNA digivolution — not by regular digivolution effects."),
            ),
            "Evolution",
        ),
        Keyword(
            "<De-Digivolve>",
            listOf(
                p("Trash the specified number of cards from the top of the chosen Digimon's stack, one card at a time. The player using the effect declares a number from 1 up to the number specified. Cannot trash cards from level 3 or lower Digimon (i.e. you cannot go below level 3)."),
            ),
            "Costs",
        ),
        Keyword(
            "<Decode>",
            listOf(
                p("When this Digimon would leave the battle area other than by battle, you may play the specified Digimon from its digivolution cards without paying cost. E.g. <Decode (blue Lv.4)>."),
            ),
            "Evolution",
        ),
        Keyword(
            "<Decoy>",
            listOf(
                p("When another of your Digimon matching the specified trait would be deleted by an opponent's effect, you may delete this Digimon instead to prevent it. E.g. <Decoy (Black)> protects Digimon with the Black trait."),
            ),
            "Protection",
        ),
        Keyword(
            "<Delay>",
            listOf(
                p("While this card is in the battle area, you may trash it to activate its <Delay> effect. Cannot be used the same turn the card is placed in the battle area. The activation is optional."),
            ),
            "Utility",
        ),
        Keyword(
            "<Digi-Burst>",
            listOf(
                p("Trash the specified number of digivolution cards from this Digimon to activate another effect. The trashing is optional — you may choose how many cards to trash up to the specified number."),
            ),
            "Costs",
        ),
        Keyword(
            "<DigiXros>",
            listOf(
                p("A play mechanic where you place cards specified in the DigiXros requirements from your hand and/or battle area under the Digimon being played. The play cost is reduced by the specified amount for each card placed."),
                p("DigiXros is optional but you must place at least 1 card. Cards are stacked left-to-right as shown in the requirements."),
            ),
            "Evolution",
        ),
        Keyword(
            "<Digisorption>",
            listOf(
                p("When digivolving into a card with this effect, you may suspend one of your Digimon to reduce the digivolution cost by the specified amount. You can use this even to suspend a Digimon that will become a digivolution card. Multiple instances can be triggered for a single digivolution."),
            ),
            "Costs",
        ),
        Keyword(
            "<Draw>",
            listOf(
                p("Draw the specified number of cards from your deck. The draw is mandatory."),
            ),
            "Utility",
        ),
        Keyword(
            "<DUAL Card>",
            listOf(
                p("A DUAL card can be treated as either a Digimon or an Option card. You declare which mode you're using when playing it."),
                p("On the field, a DUAL card is always treated as a Digimon — it cannot be referenced as an Option card while on the field. DUAL cards have both Digimon information (upper text) and Option information (lower text)."),
            ),
            "Misc",
        ),
        Keyword(
            "<End of All Turns>",
            listOf(
                p("An effect that triggers at the end of every turn, both yours and your opponent's."),
            ),
            "Timing",
        ),
        Keyword(
            "<End of Attack>",
            listOf(
                p("An effect that triggers at the point when the end of the attack arrives, after the attack has been fully resolved."),
            ),
            "Timing",
        ),
        Keyword(
            "<End of Opponent's Turn>",
            listOf(
                p("An effect that triggers when your opponent's turn ends. The non-turn player chooses the activation order."),
            ),
            "Timing",
        ),
        Keyword(
            "<End of Your Turn>",
            listOf(
                p("An effect that triggers when your turn ends. The turn player chooses the activation order if multiple effects trigger."),
            ),
            "Timing",
        ),
        Keyword(
            "<Engage>",
            listOf(
                p("At the end of your turn, this Digimon may attack."),
            ),
            "Utility",
        ),
        Keyword(
            "<Evade>",
            listOf(
                p("When this Digimon would be deleted, you may suspend it to prevent the deletion. This is an immediate-type effect with an optional processing condition."),
            ),
            "Protection",
        ),
        Keyword(
            "<Execute>",
            listOf(
                p("At the end of your turn, this Digimon may attack an opponent's Digimon (including unsuspended ones). It is deleted as pending processing after the attack ends."),
            ),
            "Utility",
        ),
        Keyword(
            "<Fortitude>",
            listOf(
                p("When this Digimon with digivolution cards is deleted, you may play it again without paying its cost. This is a mandatory trigger-type effect."),
            ),
            "Combat",
        ),
        Keyword(
            "<Fragment>",
            listOf(
                p("When this Digimon would be deleted, you may trash the specified number of its digivolution cards to prevent the deletion. E.g. <Fragment (3)> requires trashing 3 digivolution cards."),
            ),
            "Protection",
        ),
        Keyword(
            "<Guard>",
            listOf(
                p("When any of your other Digimon would leave the battle area by an opponent's effect, you may delete this Digimon to prevent it from leaving."),
            ),
            "Protection",
        ),
        Keyword(
            "<Iceclad>",
            listOf(
                p("When this Digimon battles (except against Security Digimon), compare the number of digivolution cards instead of DP. The card with more digivolution cards wins; if tied, both lose."),
            ),
            "Combat",
        ),
        Keyword(
            "<Jamming>",
            listOf(
                p("This Digimon is not deleted as a result of battle against an opponent's Security Digimon (the card flipped during a security check)."),
            ),
            "Combat",
        ),
        Keyword(
            "<Link +>",
            listOf(
                p("Increases the maximum number of link cards this Digimon can have by the specified number. Multiple instances add together numerically (e.g. two instances of <Link +1> increases the link limit by 2)."),
            ),
            "Evolution",
        ),
        Keyword(
            "<Main>",
            listOf(
                p("An activation-type effect that can be used during your Main Phase when there is no unresolved processing. Found on Digimon, Tamers, and Option cards."),
            ),
            "Timing",
        ),
        Keyword(
            "<Material Save>",
            listOf(
                p("When this Digimon is deleted, you may place the specified number of cards from its <DigiXros> requirements (from among its digivolution cards) under one of your Tamers. If the Tamer already has cards under it, new ones go to the bottom."),
            ),
            "Utility",
        ),
        Keyword(
            "<Mind Link>",
            listOf(
                p("Place this Tamer under one of your Digimon as a digivolution card. Only works on Digimon that don't already have a Tamer card in their digivolution cards. This is a mandatory effect when its <Main> timing is used."),
            ),
            "Utility",
        ),
        Keyword(
            "<On Deletion>",
            listOf(
                p("An effect that triggers when this card is deleted. It triggers in the area where the card was before moving to the trash, and becomes pending activation once the card arrives in trash."),
            ),
            "Timing",
        ),
        Keyword(
            "<On Play>",
            listOf(
                p("An effect that automatically triggers when this card is played onto the field."),
            ),
            "Timing",
        ),
        Keyword(
            "<Opponent's Turn>",
            listOf(
                p("This effect is only active during your opponent's turn. It is inactive during your own turn."),
            ),
            "Timing",
        ),
        Keyword(
            "<Overclock>",
            listOf(
                p("At the end of your turn, by deleting one of your Tokens or a specified Digimon (e.g. a Digimon with a specific trait), this Digimon attacks a player without suspending. E.g. <Overclock ([Puppet] Trait)>."),
            ),
            "Utility",
        ),
        Keyword(
            "<Overflow>",
            listOf(
                p("A rule on Digimon ACE cards. When a card with <Overflow> moves from the field (or from under a card) to another area, the memory marker moves toward your opponent's side by the specified value. This is processed immediately, even during other actions."),
                p("<Overflow> is not processed when a card is moved INTO the field or placed under another card. When multiple <Overflow> instances trigger simultaneously, the turn player resolves all theirs first, then the non-turn player."),
            ),
            "Costs",
        ),
        Keyword(
            "<Partition>",
            listOf(
                p("When this Digimon would leave the battle area (not by your effect or battle), you may play 1 of each of the specified cards from its digivolution cards without paying cost. E.g. <Partition (blue Lv.4 & green Lv.4)>."),
            ),
            "Evolution",
        ),
        Keyword(
            "<Piercing>",
            listOf(
                p("When this Digimon deletes your opponent's Digimon in battle, it performs a security check immediately before the attack ends. The security check is mandatory and only one check can be performed per attack (even if <Piercing> would activate multiple times)."),
            ),
            "Combat",
        ),
        Keyword(
            "<Progress>",
            listOf(
                p("This Digimon is unaffected by your opponent's effects while attacking."),
            ),
            "Protection",
        ),
        Keyword(
            "<Raid>",
            listOf(
                p("When this Digimon attacks, you may switch the target to your opponent's unsuspended Digimon with the highest DP. If multiple Digimon share the highest DP, you choose which one becomes the target."),
            ),
            "Combat",
        ),
        Keyword(
            "<Reboot>",
            listOf(
                p("This Digimon is unsuspended during your opponent's unsuspend phase, at the same time as the turn player's unsuspending. Multiple instances don't stack — it only activates once per phase."),
            ),
            "Movement",
        ),
        Keyword(
            "<Recovery>",
            listOf(
                p("Place the specified number of cards from the top of your deck face-down on top of your security stack."),
            ),
            "Recovery",
        ),
        Keyword(
            "<Retaliation>",
            listOf(
                p("When this Digimon is deleted in battle, the Digimon it battled is also deleted. This is a mandatory trigger-type effect."),
            ),
            "Combat",
        ),
        Keyword(
            "<Rush>",
            listOf(
                p("This Digimon can attack on the same turn it is played. Normally, Digimon must wait until your next turn."),
            ),
            "Movement",
        ),
        Keyword(
            "<Save>",
            listOf(
                p("You may place this card under one of your Tamers. Cards under a Tamer are not considered to be on the field. The activation is optional."),
            ),
            "Utility",
        ),
        Keyword(
            "<Scapegoat>",
            listOf(
                p("When this Digimon would be deleted by anything other than your own effects, you may delete one of your other Digimon to prevent its deletion."),
            ),
            "Protection",
        ),
        Keyword(
            "<Security A. +1>",
            listOf(
                p("Increases the number of security checks this Digimon performs when attacking the opponent directly. Multiple instances stack numerically (e.g. two instances of <Security A. +1> means 2 additional checks)."),
                p("Even with negative modifiers, the actual number of security checks is always 0 or more."),
            ),
            "Combat",
        ),
        Keyword(
            "<Security>",
            listOf(
                p("An effect that triggers when this card is revealed from your security stack during a security check. You may choose to activate it — if you do, the effect resolves before the card goes to trash (unless the effect says otherwise). <Security> effects take priority over all other effects when triggering simultaneously."),
            ),
            "Timing",
        ),
        Keyword(
            "<Security Digimon>",
            listOf(
                p("When a Digimon card is flipped over from your security stack during a security check, it becomes a Security Digimon. It battles the attacking Digimon by comparing DP."),
                p("A Security Digimon is not added to your hand — it goes to the trash after the battle (unless the attacking Digimon has <Jamming>, in which case the Security Digimon is also not deleted)."),
            ),
            "Misc",
        ),
        Keyword(
            "<Start of Opponent's Main Phase>",
            listOf(
                p("An effect that triggers at the moment your opponent's Main Phase begins."),
            ),
            "Timing",
        ),
        Keyword(
            "<Start of Opponent's Turn>",
            listOf(
                p("An effect that triggers at the start of your opponent's Unsuspend Phase, before any cards are unsuspended."),
            ),
            "Timing",
        ),
        Keyword(
            "<Start of Your Main Phase>",
            listOf(
                p("An effect that triggers at the moment your Main Phase begins."),
            ),
            "Timing",
        ),
        Keyword(
            "<Start of Your Turn>",
            listOf(
                p("An effect that triggers at the start of your Unsuspend Phase, before any cards are unsuspended."),
            ),
            "Timing",
        ),
        Keyword(
            "<Training>",
            listOf(
                p("During your Main Phase, you may suspend this Digimon to place the top card of your deck under it as a digivolution card. This effect can be activated in both the battle area and the breeding area."),
            ),
            "Utility",
        ),
        Keyword(
            "<Use Req.>",
            listOf(
                p("Allows you to ignore the color requirements of the specified cards. Found on cards like Digi-Eggs that give blanket color coverage for digivolution."),
            ),
            "Misc",
        ),
        Keyword(
            "<Vortex>",
            listOf(
                p("At the end of your turn, this Digimon may attack one of your opponent's Digimon. It also allows attacking the turn it was played."),
            ),
            "Utility",
        ),
        Keyword(
            "<When Attacking>",
            listOf(
                p("An effect that automatically triggers when this Digimon declares an attack, after the attack declaration is made."),
            ),
            "Timing",
        ),
        Keyword(
            "<When Digivolving>",
            listOf(
                p("An effect that automatically triggers when this Digimon digivolves."),
            ),
            "Timing",
        ),
        Keyword(
            "<When Linking>",
            listOf(
                p("An effect that triggers when this card links with another card, at the moment the link is established."),
            ),
            "Timing",
        ),
        Keyword(
            "<When Moving>",
            listOf(
                p("An effect that triggers when the card with that effect is moved from one area to another."),
            ),
            "Timing",
        ),
        Keyword(
            "<Your Turn>",
            listOf(
                p("This effect is only active during your own turn. It is inactive during your opponent's turn."),
            ),
            "Timing",
        ),
    )

    val configs: List<RulesConfig> = listOf(
        RulesConfig(
            rulesetId = "v4.1-comprehensive",
            rulesetName = "v4.1 — Comprehensive",
            setup = setup,
            winCons = victory,
            playSequence = turnStructure,
            keywords = keywords,
            fieldContent = fieldContent,
            pdfAssets = listOf(
                PdfAsset("Official Rule Manual", "rulebooks/digimon-rule-manual.pdf"),
                PdfAsset("Comprehensive Rules", "rulebooks/digimon-comprehensive.pdf"),
            ),
        ),
    )
}
