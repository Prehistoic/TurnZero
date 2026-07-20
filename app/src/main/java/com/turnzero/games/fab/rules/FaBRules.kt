package com.turnzero.games.fab.rules

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

object FaBRules {

    private val victory = listOf(
        InfoCard(
            "Reduce Opponent's Life to 0",
            listOf(
                p("Each hero starts with a life total shown on their card. Deal enough damage to reduce the opponent's life to 0 and you win."),
                p("If both players' life totals are reduced to 0 simultaneously, the game is a draw."),
            ),
        ),
        InfoCard(
            "Concession",
            listOf(
                p("You may concede at any time. Announce your concession and you lose immediately."),
            ),
        ),
    )

    private val baseSetupStart = listOf(
        RuleStep(
            "Reveal Heroes",
            "Each player places their hero card face-up in their hero zone.",
            listOf(
                p("Your hero determines your class, talents, life total, and intellect (cards drawn at end of turn)."),
            ),
        ),
        RuleStep(
            "Determine First Player",
            "Randomly pick a player, who then decides who goes first.",
            listOf(
                p("For the first game of a match, use any mutually agreeable random method. For subsequent games, the loser of the previous game decides."),
            ),
        ),
        RuleStep(
            "Choose Equipment & Weapons",
            "Secretly select your starting arena-cards from your card-pool.",
            listOf(
                p("Choose up to one card per equipment zone (Arms, Chest, Head, Legs) and weapon zone(s). Place them face-down in their zones or in a single face-down pile."),
                p("Your choice stays hidden until the game begins."),
            ),
        ),
    )

    private val setupEnd = listOf(
        RuleStep(
            "Shuffle & Cut",
            "Shuffle your deck and present it to your opponent to shuffle or cut.",
            listOf(
                p("After your opponent cuts, place your deck in its zone. You may no longer change which cards you selected."),
            ),
        ),
        RuleStep(
            "Reveal & Draw",
            "Reveal your weapons and equipment. Draw cards up to your hero's intellect.",
            listOf(
                p("Starting with the first player, each player reveals their equipment and weapons face-up. Then each draws cards equal to their hero's intellect."),
                p("The first player begins their first turn."),
            ),
        ),
    )

    private val ccSetup = baseSetupStart + setupEnd
    private val sageSetup = baseSetupStart + setupEnd

    private val turnStructure = listOf(
        RuleStep(
            "Start Phase",
            "Beginning-of-turn effects trigger and resolve.",
            listOf(
                p("Players do not get priority during this phase. Any 'start of turn' effects trigger and resolve automatically."),
            ),
        ),
        RuleStep(
            "Action Phase",
            "The main phase — you have 1 action point and can play cards, attack, and activate abilities.",
            listOf(
                p("You start the action phase with 1 action point. Most actions (attacking, playing action cards) cost your action point."),
                p("You can also pitch cards to gain resources — discard a card from hand to gain resource points equal to the card's pitch value (the number of pips in the top-left corner)."),
                sh("What you can do during the action phase:"),
                bl(
                    listOf(
                        "**Play an action card** — Costs 1 action point + its resource cost. Resolves on the stack, then goes to graveyard.",
                        "**Attack with a weapon** — Some weapons have an activated ability to attack. Costs 1 action point + any resource cost.",
                        "**Play an instant** — Does not cost an action point. Can be played any time you have priority, even during your opponent's turn.",
                        "**Activate an ability** — Pay the cost (action point + resources) and resolve.",
                        "**Pitch a card** — Discard any card from hand to gain resource points. You can pitch multiple times to build up resources for a big play.",
                    ),
                ),
                p("The action phase ends when the stack is empty, the combat chain is closed, and both players pass priority in succession."),
            ),
        ),
        RuleStep(
            "End Phase",
            "Clean up, arsenal a card, pitch away, ready everything, and draw back to your intellect.",
            listOf(
                sh("In order:"),
                bl(
                    listOf(
                        "**Reset allies' life totals** (if any).",
                        "**Arsenal** — You may put one card from your hand face-down into your empty arsenal zone.",
                        "**Pitch** — All cards in your pitch zone go to the bottom of your deck in any order.",
                        "**Ready** — Untap all of your permanents (weapons, equipment, allies).",
                        "**Lose action/resource points** — Any remaining points are lost.",
                        "**Draw** — Draw cards until your hand size equals your hero's intellect.",
                    ),
                ),
                p("The turn ends, and the next player in clockwise order begins their turn. For the first turn of the game, all other players also draw to their intellect after the turn player's end phase."),
            ),
        ),
    )

    private val ccDeckRules = listOf(
        InfoCard(
            "Card Pool & Deck",
            listOf(
                p("Card pool: exactly 80 cards. Select at least 60 cards from your pool to form your deck."),
            ),
        ),
        InfoCard(
            "Hero & Restrictions",
            listOf(
                p("Must use a non-young (adult) hero."),
                p("Up to 3 copies of each unique card (same name AND color)."),
                p("Cards must match your hero's class and talents — generic cards can go in any deck."),
            ),
        ),
        InfoCard(
            "Equipment",
            listOf(
                p("Up to 1 card per equipment zone (Arms, Chest, Head, Legs) and weapon zone(s)."),
                p("Unused pool cards become your inventory for sideboarding between games."),
            ),
        ),
    )

    private val sageDeckRules = listOf(
        InfoCard(
            "Card Pool & Deck",
            listOf(
                p("Card pool: exactly 55 cards. Select exactly 40 cards from your pool to form your deck."),
            ),
        ),
        InfoCard(
            "Hero & Restrictions",
            listOf(
                p("Must use a young hero."),
                p("All cards must be common, rare, or basic rarity."),
                p("Up to 2 copies of each unique card (same name AND color)."),
                p("Cards must match your hero's class and talents — generic cards can go in any deck."),
            ),
        ),
        InfoCard(
            "Equipment",
            listOf(
                p("Up to 1 card per equipment zone (Arms, Chest, Head, Legs) and weapon zone(s)."),
                p("Unused pool cards become your inventory for sideboarding between games."),
            ),
        ),
    )

    private val fieldContent = listOf(
        img("rulebooks/images/fab-playmat.png"),
        sh("Play Area Layout"),
        p("Flesh and Blood uses a distinct play area with several zones:"),
        bl(
            listOf(
                "**Hero zone** — Your hero card sits here, face-up. It shows your life total, class, talents, and intellect.",
                "**Weapon zones** — You may have 1 or 2 weapon zones. Weapons are equipped here face-up and can be used to attack.",
                "**Equipment zones** — Arms, Chest, Head, and Legs. Equipment is equipped here face-up and provides passive effects or defense.",
                "**Deck zone** — Your draw pile. Cards enter your hand from here.",
                "**Graveyard** — Defeated equipment, used action cards, and discarded cards go here face-up.",
                "**Banished zone** — Cards removed from the game go here face-up. Some effects let you play cards from banish.",
                "**Pitch zone** — When you pitch a card, it goes here temporarily. At the end of the turn, all pitched cards go to the bottom of your deck.",
                "**Arsenal** — One face-down card you can play as though it were in your hand. Only one card at a time.",
                "**Combat chain** — When you attack, the attack card and any defending/reaction cards form a temporary chain link here.",
            ),
        ),
        sh("Pitching"),
        p("Instead of using lands or mana, FaB uses a pitching system. Each card has a pitch value (1, 2, or 3 colored pips in the top-left). To gain resources, discard a card from your hand — you gain resource points equal to its pitch value. You can pitch multiple cards in a turn to pay for expensive plays."),
        sh("The Combat Chain"),
        p("When you attack, the combat chain opens. Attacking cards form chain links. The defending player may defend with cards from hand or equipment. Both players can play attack/defense reactions. Each chain link resolves in order: Declare attack → Declare defense → Reactions → Damage."),
        sh("Defense Values"),
        p("Every card has a defense value (bottom-right number). When defending, you block damage equal to the card's defense value. Equipment has defense too, but may take durability loss."),
    )

    private val keywords = listOf(
        Keyword(
            "Attack",
            listOf(
                p("The attack ability on a card or weapon lets you declare an attack. The card moves onto the combat chain, the chain opens if closed, and you must declare a legal target (enemy hero or ally)."),
            ),
            "Combat",
        ),
        Keyword(
            "Go again",
            listOf(
                p("After this resolves, gain 1 action point. This lets you take another action in the same turn. Multiple instances don't stack — you can only have go again once."),
            ),
            "Combat",
        ),
        Keyword(
            "Dominate",
            listOf(
                p("This attack can't be defended by more than one card from hand. Equipment can still defend alongside that one card."),
            ),
            "Combat",
        ),
        Keyword(
            "Overpower",
            listOf(
                p("This attack can't be defended by more than one action card. Unlike Dominate, you can still block with multiple non-action cards."),
            ),
            "Combat",
        ),
        Keyword(
            "Crush",
            listOf(
                p("A triggered ability that fires when this attack deals 4 or more damage. Common on Guardian cards."),
            ),
            "Combat",
        ),
        Keyword(
            "Piercing N",
            listOf(
                p("If this is defended, the defending hero loses N life. Bypasses the defense value entirely."),
            ),
            "Combat",
        ),
        Keyword(
            "Reprise",
            listOf(
                p("Triggers if the defending hero defended with a card from hand this chain link. Rewards you for drawing out your opponent's hand."),
            ),
            "Combat",
        ),
        Keyword(
            "Battleworn",
            listOf(
                p("When the combat chain closes, if this equipment defended, put a -1 defense counter on it. Equipment wears down over time."),
            ),
            "Equipment",
        ),
        Keyword(
            "Blade Break",
            listOf(
                p("When the combat chain closes, if this defended, destroy it. One-time use, then it's gone."),
            ),
            "Equipment",
        ),
        Keyword(
            "Temper",
            listOf(
                p("When the combat chain closes, if this defended, put it on the bottom of your deck. It returns for later use rather than being destroyed."),
            ),
            "Equipment",
        ),
        Keyword(
            "Ward N",
            listOf(
                p("If you would be dealt N or less damage, destroy this and prevent that damage. Absorbs small hits entirely."),
            ),
            "Defense",
        ),
        Keyword(
            "Spellvoid N",
            listOf(
                p("If you would be dealt N or less arcane damage, you may destroy this and prevent it. Similar to Ward but specifically for arcane damage and optional."),
            ),
            "Defense",
        ),
        Keyword(
            "Spectra",
            listOf(
                p("This can be attacked (normally only heroes/allies can). When it becomes the target of an attack, destroy it. Illusionist auras use this."),
            ),
            "Aura",
        ),
        Keyword(
            "Phantasm",
            listOf(
                p("When defended by a non-Illusionist attack action card with 6 or more power, destroy this. Powerful illusion that shatters against big physical attacks."),
            ),
            "Aura",
        ),
        Keyword(
            "Mirage N",
            listOf(
                p("When defending a non-Illusionist attack, this gets +N defense. Illusionist aura shields."),
            ),
            "Aura",
        ),
        Keyword(
            "Fusion",
            listOf(
                p("Optional additional cost when playing: reveal a card with the specified supertype(s) from your hand. Doing so unlocks a bonus effect. Combines elements together."),
            ),
            "Play",
        ),
        Keyword(
            "Heave N",
            listOf(
                p("While this is in your hand, you may pay N resource points plus put it face-up into your arsenal from hand. Lets you stash a card for later at a discount."),
            ),
            "Play",
        ),
        Keyword(
            "Scrap",
            listOf(
                p("Optional additional cost: banish an item or equipment you control. Reduces the resource cost. Mechanologist theme."),
            ),
            "Play",
        ),
        Keyword(
            "Beat Chest",
            listOf(
                p("Optional additional cost: discard a card with 6 or more power. Brute-themed cost reduction."),
            ),
            "Play",
        ),
        Keyword(
            "Material",
            listOf(
                p("While this card is under a permanent (attached/equipped), that permanent gains its listed effects."),
            ),
            "Play",
        ),
        Keyword(
            "Contract",
            listOf(
                p("You are contracted to complete a specific objective. If you do, you get the listed reward. Failing may have consequences."),
            ),
            "Play",
        ),
        Keyword(
            "Surge",
            listOf(
                p("If this deals N or more damage, get a bonus effect. Rewards big hits."),
            ),
            "Combat",
        ),
        Keyword(
            "Combo",
            listOf(
                p("If the named card was the last attack in the combat chain, this gets a bonus. Builds on your previous attack."),
            ),
            "Combat",
        ),
        Keyword(
            "Rune Gate",
            listOf(
                p("If you control enough Runechants (Runeblade tokens), you may play this from your banished zone. Runeblade recursion mechanic."),
            ),
            "Class",
        ),
        Keyword(
            "Quell N",
            listOf(
                p("If you would be dealt N or less damage, prevent that damage. Similar to Ward but doesn't destroy the source — can be used repeatedly."),
            ),
            "Defense",
        ),
        Keyword(
            "Legendary",
            listOf(
                p("You may only have 1 copy of this card in your card-pool, even if the format allows more copies."),
            ),
            "Deckbuilding",
        ),
        Keyword(
            "Ephemeral",
            listOf(
                p("You can't include this in your card-pool. It can only be created during the game by effects (usually tokens)."),
            ),
            "Deckbuilding",
        ),
        Keyword(
            "Modular",
            listOf(
                p("This may be equipped to any equipment zone (Arms, Chest, Head, or Legs). Flexible placement."),
            ),
            "Equipment",
        ),
        Keyword(
            "Cloaked",
            listOf(
                p("Equip this face-down. Your opponent doesn't know what it is until you use it. Assassin/Ninja theme."),
            ),
            "Equipment",
        ),
        Keyword(
            "Perched",
            listOf(
                p("Can be equipped in addition to a two-handed weapon. Occupies the same weapon zone as the 2H without taking an extra slot."),
            ),
            "Equipment",
        ),
        Keyword(
            "Arcane Shelter N",
            listOf(
                p("If you would be dealt N or less damage, prevent that damage. Ward variant that doesn't require destroying the source."),
            ),
            "Defense",
        ),
        Keyword(
            "Guardwell",
            listOf(
                p("When the combat chain closes, if this defended, it stays equipped instead of being destroyed or removed."),
            ),
            "Equipment",
        ),
        Keyword(
            "Ambush",
            listOf(
                p("While this is in your arsenal, you may defend with it as though it were in your hand. Surprise blocks from the arsenal."),
            ),
            "Class",
        ),
        Keyword(
            "Crank",
            listOf(
                p("As this enters the arena, you may remove a steam counter from a Mechanologist item to get a bonus."),
            ),
            "Class",
        ),
        Keyword(
            "Blood Debt",
            listOf(
                p("While in your banished zone at the start of your turn, you lose 1 life. Shadow cards have a cost for sitting in banish."),
            ),
            "Class",
        ),
        Keyword(
            "Galvanize",
            listOf(
                p("When this defends, you may banish a card from your graveyard for a bonus. Mechanologist recursion."),
            ),
            "Class",
        ),
        Keyword(
            "Bond",
            listOf(
                p("If an Elemental card was pitched to pay for an Elemental card this turn, get a bonus. Rewards staying in-element."),
            ),
            "Class",
        ),
        Keyword(
            "Flow",
            listOf(
                p("If you played a card with the matching element this turn, get a bonus. Rewards elemental sequencing."),
            ),
            "Class",
        ),
        Keyword(
            "Unity",
            listOf(
                p("When this defends together with a card from hand, this gets +1 defense. Skilled at cooperative defense."),
            ),
            "Class",
        ),
        Keyword(
            "Heavy",
            listOf(
                p("If this is the only card equipped to your weapon zones (no off-hand), get a bonus. Guardian two-hander theme."),
            ),
            "Class",
        ),
        Keyword(
            "Tower",
            listOf(
                p("If this has 13 or more power, unlock a powerful effect. Guardian build-up mechanic."),
            ),
            "Class",
        ),
        Keyword(
            "High Tide",
            listOf(
                p("If there are 2 or more Wizard non-attack action cards in your graveyard, get a bonus. Wizard spellcasting."),
            ),
            "Class",
        ),
        Keyword(
            "Solflare",
            listOf(
                p("When this is charged to your hero's soul, trigger an effect. Light Warrior soul-charging."),
            ),
            "Class",
        ),
        Keyword(
            "Go Fish",
            listOf(
                p("When this hits a hero, they reveal their hand and you may banish a card from it. Mind games."),
            ),
            "Class",
        ),
        Keyword(
            "Quickstrike",
            listOf(
                p("If this has go again at the start of the chain link, get a bonus. Rewards building speed."),
            ),
            "Class",
        ),
        Keyword(
            "Starfall",
            listOf(
                p("If an instant has been put into your graveyard this turn, get a bonus. Rewards quick reflexes."),
            ),
            "Class",
        ),
        Keyword(
            "Essence",
            listOf(
                p("Allows your hero to include cards with certain talents even if they don't have that talent. Expands your card-pool options."),
            ),
            "Deckbuilding",
        ),
        Keyword(
            "Meld",
            listOf(
                p("Pay double cost to play both halves of a split card at once. The melded card has properties of both sides."),
            ),
            "Play",
        ),
        Keyword(
            "Stealth",
            listOf(
                p("This card does nothing special on its own — Stealth has no inherent mechanical effect. Assassin cards use Stealth as a label for other abilities to reference."),
            ),
            "Class",
        ),
        Keyword(
            "Pairs with [OBJECT]",
            listOf(
                p("Equip this only if you already have the named object equipped. Paired items that work together."),
            ),
            "Equipment",
        ),
        Keyword(
            "Protect",
            listOf(
                p("You may defend any hero attacked by an opponent with this card. Act as a bodyguard for your team in multiplayer."),
            ),
            "Combat",
        ),
    )

    val configs: List<RulesConfig> = listOf(
        RulesConfig(
            rulesetId = "classic-constructed",
            rulesetName = "Classic Constructed",
            setup = ccSetup + listOf(
                RuleStep(
                    "Match Format",
                    "55-minute rounds, best of 1.",
                    listOf(
                        p("Classic Constructed tournaments use best-of-1 matches with 55-minute rounds. If time expires, the turn player finishes their turn plus one additional turn. If still no winner, the game is a draw."),
                    ),
                ),
            ),
            victoryRules = victory,
            playSequence = turnStructure,
            keywords = keywords,
            pdfAssets = listOf(
                PdfAsset("Comprehensive Rules", "rulebooks/fab-comprehensive-rulebook.pdf"),
            ),
            fieldContent = fieldContent,
            deckRules = ccDeckRules,
        ),
        RulesConfig(
            rulesetId = "silver-age",
            rulesetName = "Silver Age",
            setup = sageSetup + listOf(
                RuleStep(
                    "Match Format",
                    "35-minute rounds, best of 1.",
                    listOf(
                        p("Silver Age tournaments use best-of-1 matches with 35-minute rounds. If time expires, the turn player finishes their turn plus one additional turn. If still no winner, the game is a draw."),
                    ),
                ),
            ),
            victoryRules = victory,
            playSequence = turnStructure,
            keywords = keywords,
            pdfAssets = listOf(
                PdfAsset("Comprehensive Rules", "rulebooks/fab-comprehensive-rulebook.pdf"),
            ),
            fieldContent = fieldContent,
            deckRules = sageDeckRules,
        ),
    )
}
