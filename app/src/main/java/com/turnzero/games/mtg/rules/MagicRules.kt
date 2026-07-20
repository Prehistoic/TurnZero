package com.turnzero.games.mtg.rules

import com.turnzero.core.ui.components.ContentBlock
import com.turnzero.tools.rules.InfoCard
import com.turnzero.tools.rules.Keyword
import com.turnzero.tools.rules.PdfAsset
import com.turnzero.tools.rules.RuleStep
import com.turnzero.tools.rules.RulesConfig
import com.turnzero.tools.rules.bl
import com.turnzero.tools.rules.img
import com.turnzero.tools.rules.p
import com.turnzero.tools.rules.sh

object MagicRules {

    private val victory = listOf(
        InfoCard("Reduce Life to 0", listOf(p("Each player starts with 20 life (40 for Commander). Reduce your opponent's life to 0 through combat damage, spell effects, or abilities to win."))),
        InfoCard("Empty Library", listOf(p("If a player is required to draw a card but has no cards remaining in their library, they lose the game."))),
        InfoCard("Poison Counters", listOf(p("A player who receives 10 or more poison counters loses the game."))),
        InfoCard("Alternative Conditions", listOf(p("Some cards have effects that declare an automatic win (e.g., Approach of the Second Sun, Thassa's Oracle, Laboratory Maniac)."))),
        InfoCard("Concession", listOf(p("Either player may concede at any time. Announce your concession and you lose immediately."))),
    )

    private val commanderVictory = listOf(
        InfoCard("Reduce Life to 0", listOf(p("Each player starts with 40 life. Reduce each opponent's life to 0 through combat damage, spell effects, or abilities to win."))),
        InfoCard("Empty Library", listOf(p("If a player is required to draw a card but has no cards remaining, they lose."))),
        InfoCard("Poison Counters", listOf(p("A player who receives 10 or more poison counters loses the game."))),
        InfoCard("Commander Damage", listOf(p("If a player is dealt 21 or more combat damage by a single commander over the course of the game, that player loses. Damage is tracked per commander."))),
        InfoCard("Alternative Conditions", listOf(p("Some cards have effects that declare an automatic win."))),
        InfoCard("Concession", listOf(p("Either player may concede at any time. Announce your concession and you lose immediately."))),
    )

    private val baseSetup = listOf(
        RuleStep("Determine First Player", "Randomly determine who goes first. The winner chooses to play or draw.", listOf(p("Use any mutually agreeable random method (dice roll, coin flip). The winner decides whether to play first or draw first."))),
        RuleStep("Shuffle & Draw", "Each player shuffles their library and draws 7 cards as their opening hand.", listOf(p("Thoroughly randomize your deck. Your opponent may shuffle or cut it after you finish."), p("Both players draw 7 cards from the top of their library."))),
        RuleStep("Mulligan (London Mulligan)", "Draw 7 each time. After keeping, put 1 card on the bottom of your library for each mulligan taken.", listOf(p("Starting with the first player, each player may declare a mulligan: shuffle your hand back into your library and draw 7 new cards."), p("After you keep a hand, put 1 card from it on the bottom of your library for each mulligan you took. You may mulligan as many times as you want."))),
    )

    private val commanderSetup = listOf(
        RuleStep("Choose Your Commander", "Each player reveals their legendary creature commander and places it in the command zone.", listOf(p("Your commander must be a legendary creature (or a planeswalker allowed by the rules). It starts in the command zone, not in your library."), p("Your deck's color identity is determined by all mana symbols in your commander's mana cost and rules text. Every card in your 99-card library must fall within this color identity."))),
        RuleStep("Determine Turn Order", "Randomly determine seating. The first player chooses the direction of play.", listOf(p("Commander is typically played with 4 players. Determine seating randomly, then the player going first chooses whether play proceeds clockwise or counterclockwise."), p("Each player starts with 40 life. Commander damage is tracked separately for each commander (21+ combat damage from a single commander = loss)."))),
        RuleStep("Shuffle & Draw", "Shuffle your 99-card library. Draw 7 cards, then take mulligans as desired.", listOf(p("Thoroughly shuffle your deck. Opponents may cut. Draw 7 cards. London Mulligan applies: shuffle back, draw 7 again, bottom 1 card per mulligan taken."))),
    )

    private val duelCommanderSetup = listOf(
        RuleStep("Choose Your Commander", "Each player reveals their legendary creature (or planeswalker) commander and places it in the command zone.", listOf(p("Duel Commander uses the same color identity rules as regular Commander. Your commander starts in the command zone. Both players start with 20 life."))),
        RuleStep("Determine First Player", "Randomly determine who goes first. The winner chooses to play or draw.", listOf(p("Use any mutually agreeable random method. The winner decides whether to play first or draw first."))),
        RuleStep("Shuffle & Draw", "Shuffle your 99-card library. Draw 7 cards. London Mulligan: shuffle back, draw 7, bottom 1 per mulligan.", listOf(p("Thoroughly shuffle your deck. Draw 7. Mulligan as many times as you want, bottoming 1 card per mulligan."))),
    )

    private val baseTurn = listOf(
        RuleStep("Beginning Phase", "Untap all your permanents, resolve upkeep triggers, then draw a card.", listOf(sh("Untap Step"), p("Untap all your tapped permanents. No player receives priority during this step."), sh("Upkeep Step"), p("Abilities that trigger at the beginning of upkeep go on the stack. Players may cast instants and activate abilities."), sh("Draw Step"), p("The active player draws 1 card. The player going first skips this draw on their very first turn."))),
        RuleStep("Main Phase 1", "Play 1 land (once per turn). Cast creature, sorcery, enchantment, artifact, and planeswalker spells.", listOf(p("You may play up to 1 land from your hand per turn. You may cast any number of spells as long as you can pay their costs. Instants and cards with flash can be cast at any time."))),
        RuleStep("Combat Phase", "Attack with your creatures and deal combat damage to the opponent or their planeswalkers.", listOf(sh("Beginning of Combat"), p("Announce you are entering combat. The active player receives priority first."), sh("Declare Attackers"), p("Choose which untapped creatures will attack and what they attack (opponent or their planeswalkers). Tapping the creature is part of declaring it as an attacker (unless it has vigilance). Creatures with summoning sickness cannot attack unless they have haste."), sh("Declare Blockers"), p("The defending player chooses which creatures will block and how. Each blocking creature can block only one attacker. Multiple creatures can block the same attacker. A creature with flying can only be blocked by creatures with flying or reach. A creature with menace must be blocked by 2+ creatures."), sh("Combat Damage"), p("Attackers and blockers deal damage equal to their power simultaneously. First strike and double strike create an extra damage step before the normal one. Trample: excess damage to blockers spills over to the defending player. Deathtouch: any amount of damage destroys the blocker. Lifelink: you gain life equal to the damage dealt."), sh("End of Combat"), p("Effects that last until end of combat expire. Creatures stop being attacking or blocking."))),
        RuleStep("Main Phase 2", "A second chance to cast spells and play a land after combat.", listOf(p("Identical to Main Phase 1. You may play a land here if you did not play one during Main Phase 1. Cast any remaining spells before ending your turn."))),
        RuleStep("Ending Phase", "End-of-turn effects trigger, then discard down to maximum hand size.", listOf(sh("End Step"), p("Abilities that trigger at the beginning of the end step go on the stack. Players may cast instants and activate abilities."), sh("Cleanup Step"), p("Discard down to your maximum hand size of 7 cards. All damage is removed from creatures. All 'until end of turn' effects expire."))),
    )

    private val commanderTurn = listOf(
        RuleStep("Beginning Phase", "Untap all your permanents, resolve upkeep triggers, then draw a card.", listOf(sh("Untap Step"), p("Untap all your tapped permanents. No player receives priority."), sh("Upkeep Step"), p("Abilities that trigger at the beginning of upkeep go on the stack. Players may cast instants and activate abilities."), sh("Draw Step"), p("Draw 1 card from your library. The player going first skips this draw on their very first turn."))),
        RuleStep("Main Phase 1", "Play 1 land. Cast spells, including your commander from the command zone.", listOf(p("You may play up to 1 land per turn. Cast creature, sorcery, enchantment, artifact, and planeswalker spells. You may cast your commander from the command zone at any time you could cast a sorcery. Each time after the first, it costs an additional 2 generic mana (commander tax)."))),
        RuleStep("Combat Phase", "Attack opponents or their planeswalkers with your creatures.", listOf(sh("Beginning of Combat"), p("Announce combat entry. The active player gets priority first."), sh("Declare Attackers"), p("Choose which untapped creatures attack and which opponent (or their planeswalker) each attacks. You may attack different opponents with different creatures. Tapping the creature is part of declaring it as an attacker (unless it has vigilance). Creatures with summoning sickness cannot attack without haste."), sh("Declare Blockers"), p("Only the defending player (the one being attacked) may declare blockers. Flying, menace, and other combat abilities apply normally."), sh("Combat Damage"), p("First strike/double strike create an extra damage step. Trample, deathtouch, lifelink all function as normal. Commander damage is tracked separately per commander."), sh("End of Combat"), p("End-of-combat effects expire. Creatures stop being attacking/blocking."))),
        RuleStep("Main Phase 2", "Second main phase — play a land if you haven't yet, cast remaining spells, including your commander.", listOf(p("Identical to Main Phase 1. Play a land if you didn't in Main Phase 1. You may cast your commander from the command zone here if you haven't yet this turn."))),
        RuleStep("Ending Phase", "End-of-turn triggers, discard to 7 cards max, damage removed from creatures.", listOf(sh("End Step"), p("End-of-turn abilities trigger. Players may cast instants."), sh("Cleanup Step"), p("Discard to 7 cards max. All damage removed from creatures. 'Until end of turn' effects expire."))),
    )

    private val duelCommanderTurn = listOf(
        RuleStep("Beginning Phase", "Untap all your permanents, resolve upkeep triggers, then draw a card.", listOf(sh("Untap Step"), p("Untap all your tapped permanents."), sh("Upkeep Step"), p("Upkeep triggers go on the stack. Players may respond."), sh("Draw Step"), p("Draw 1 card. First player skips this on their first turn."))),
        RuleStep("Main Phase 1", "Play 1 land. Cast spells, including your commander from the command zone.", listOf(p("Play up to 1 land. Cast creature, sorcery, enchantment, artifact, and planeswalker spells. You may cast your commander from the command zone. Each recast costs an additional 2 generic mana (commander tax)."))),
        RuleStep("Combat Phase", "Attack your opponent or their planeswalkers with your creatures.", listOf(sh("Beginning of Combat"), p("Announce combat. Active player gets priority first."), sh("Declare Attackers"), p("Choose untapped creatures to attack. Tap (unless vigilance). Summoning sickness prevents attacking without haste. Attack opponent or their planeswalkers."), sh("Declare Blockers"), p("Defending player assigns blockers. Flying blocks flying/reach. Menace requires 2+ blockers."), sh("Combat Damage"), p("First strike/double strike extra step. Trample, deathtouch, lifelink apply. Commander damage tracked per commander (21+ = loss)."), sh("End of Combat"), p("End-of-combat effects expire."))),
        RuleStep("Main Phase 2", "Second main phase — play a land if you haven't, cast remaining spells.", listOf(p("Play a land if you didn't in Main Phase 1. Cast remaining spells, including your commander."))),
        RuleStep("Ending Phase", "End step triggers, discard to 7, damage removed.", listOf(sh("End Step"), p("End-of-turn triggers."), sh("Cleanup Step"), p("Discard to 7 cards max. Damage removed. Effects expire."))),
    )

    private val standardField = listOf(img("rulebooks/images/mtg-field.png"), sh("Battlefield (Standard)"), bl(listOf("**Library** — Your draw pile, face-down.", "**Hand** — Cards you hold. Hidden. Max 7 at end of turn.", "**Battlefield** — Permanents (creatures, lands, artifacts, enchantments, planeswalkers). Each player has their own side.", "**Graveyard** — Discard pile, face-up. Public information.", "**Exile** — Removed-from-game zone.", "**Stack** — Spells and abilities wait to resolve here. Last-in, first-out.")))
    private val commanderField = listOf(sh("Battlefield (Commander, 4 Players)"), bl(listOf("**Library** — 99-card draw pile.", "**Hand** — Hidden. Max 7 at cleanup.", "**Battlefield** — All 4 players share the battlefield with their own sides.", "**Graveyard** — Discard pile, face-up.", "**Exile** — Removed-from-game zone.", "**Stack** — Spells/abilities resolve LIFO.", "**Command Zone** — Commander starts here. Cast from here with commander tax (+2 per recast).")))
    private val duelCommanderField = listOf(sh("Battlefield (Duel Commander, 1v1)"), bl(listOf("**Library** — 99-card draw pile.", "**Hand** — Hidden. Max 7 at cleanup.", "**Battlefield** — 1v1, each player has their own side.", "**Graveyard** — Discard pile, face-up.", "**Exile** — Removed-from-game zone.", "**Stack** — Spells/abilities resolve LIFO.", "**Command Zone** — Commander starts here. Commander tax applies. 20 starting life.")))

    private val deck60 = listOf(InfoCard("Deck Size", listOf(p("Minimum 60 cards, no maximum."))), InfoCard("Card Limit", listOf(p("Max 4 copies of any card with the same English name. Basic lands (Plains, Island, Swamp, Mountain, Forest) are unlimited. Cards that override this (like Relentless Rats) take precedence."))), InfoCard("Sideboard", listOf(p("Up to 15 cards. After game 1, swap cards on a 1-for-1 basis. Deck must stay at least 60 cards after sideboarding."))))

    private val stdDeck = listOf(InfoCard("Card Pool", listOf(p("The most recent 3 years of premiere Standard-legal Magic sets. Older sets rotate out annually.")))) + deck60
    private val modDeck = listOf(InfoCard("Card Pool", listOf(p("All sets from Eighth Edition (July 2003) forward, plus Modern Horizons sets. Does not rotate.")))) + deck60
    private val pioDeck = listOf(InfoCard("Card Pool", listOf(p("All sets from Return to Ravnica (October 2012) forward. Non-rotating format between Standard and Modern.")))) + deck60
    private val legDeck = listOf(InfoCard("Card Pool", listOf(p("All Magic sets ever printed. Has its own banned list separate from other formats. No restricted list.")))) + deck60
    private val vinDeck = listOf(InfoCard("Card Pool", listOf(p("All Magic sets ever printed. Uses a Restricted List — restricted cards are limited to 1 copy instead of 4. Only format with restricted cards.")))) + deck60
    private val preDeck = listOf(InfoCard("Card Pool", listOf(p("Sets from Fourth Edition and Ice Age (1995) through Scourge (2003). Community-run format focused on the pre-Modern era.")))) + deck60
    private val pauDeck = listOf(InfoCard("Card Pool", listOf(p("Only cards printed at common rarity in any Magic set or product. If a card was ever common, all versions are legal.")))) + deck60

    private val cmdrDeck = listOf(InfoCard("Deck Size", listOf(p("Exactly 100 cards including your commander. 99 cards in the library + 1 commander in the command zone."))), InfoCard("Singleton", listOf(p("Only 1 copy of any card except basic lands or cards that specifically say otherwise."))), InfoCard("Commander", listOf(p("1 legendary creature as your commander. Starts in the command zone. Color identity = all mana symbols on your commander — every card must match."), p("Cast from command zone any time you could cast a sorcery. Commander tax: +2 generic mana per previous cast from command zone."))), InfoCard("Starting Life", listOf(p("40 life. 21+ combat damage from a single commander = loss, tracked per commander.")),))

    private val duelDeck = listOf(InfoCard("Deck Size", listOf(p("Exactly 100 cards including commander. Singleton: 1 copy per card except basic lands."))), InfoCard("Commander", listOf(p("1 legendary creature or planeswalker (per banlist). Color identity rules same as Commander. Commander tax: +2 per recast."))), InfoCard("Starting Life", listOf(p("20 life. 21 commander damage rule applies. 1v1 format with a separate banlist from multiplayer Commander."))))

    private val keywords = listOf(
        Keyword("<Absorb>", listOf(p("If a source would deal damage to this creature, prevent X of that damage, where X is the absorb number.")), "Keyword Ability"),
        Keyword("<Adapt>", listOf(p("If this creature has no +1/+1 counters, put N +1/+1 counters on it.")), "Keyword Action"),
        Keyword("<Affinity>", listOf(p("This spell costs 1 less for each permanent of the specified type you control.")), "Keyword Ability"),
        Keyword("<Adventurer Cards>", listOf(p("Creature cards with an instant/sorcery Adventure you can cast first. After the Adventure resolves, the card is exiled and you may cast the creature later.")), "Core Concept"),
        Keyword("<Afterlife>", listOf(p("When this creature dies, create N 1/1 white and black Spirit creature tokens with flying.")), "Keyword Ability"),
        Keyword("<Aftermath>", listOf(p("You may cast the Aftermath half of this card from your graveyard, then exile it.")), "Keyword Ability"),
        Keyword("<Annihilator>", listOf(p("Whenever this creature attacks, defending player sacrifices N permanents.")), "Keyword Ability"),
        Keyword("<Artifact>", listOf(p("A card type representing magical items, constructs, and equipment. Artifact cards are typically colorless and are cast like other spells.")), "Core Concept"),
        Keyword("<Attraction Cards>", listOf(p("Artifacts that open attractions from a side deck called the Attraction deck. Each has a visit ability that triggers when you roll to visit it.")), "Core Concept"),
        Keyword("<Ascend>", listOf(p("If you control 10+ permanents, you gain the city's blessing for the rest of the game.")), "Keyword Ability"),
        Keyword("<Backup>", listOf(p("When this creature enters, put N +1/+1 counter(s) on target creature, which also gains this creature's non-Backup abilities.")), "Keyword Ability"),
        Keyword("<Battle>", listOf(p("A permanent type introduced in March of the Machine. Battles enter under an opponent's control and can be attacked. When defeated, they transform under your control.")), "Core Concept"),
        Keyword("<Bargain>", listOf(p("You may sacrifice an artifact, enchantment, or token as you cast this spell for an additional effect.")), "Keyword Ability"),
        Keyword("<Battle Cry>", listOf(p("Whenever this creature attacks, each other attacking creature gets +1/+0 until end of turn.")), "Keyword Ability"),
        Keyword("<Bestow>", listOf(p("You may cast this card as an Aura for its bestow cost. If the enchanted creature leaves, it becomes a creature again.")), "Keyword Ability"),
        Keyword("<Blitz>", listOf(p("You may cast this spell for its blitz cost. If you do, it gains haste and 'When this creature dies, draw a card.' Sacrifice it at the next end step.")), "Keyword Ability"),
        Keyword("<Bloodthirst>", listOf(p("If an opponent was dealt damage this turn, this creature enters with N +1/+1 counters.")), "Keyword Ability"),
        Keyword("<Boast>", listOf(p("Activate this ability only if this creature attacked this turn and only once per turn.")), "Keyword Ability"),
        Keyword("<Bolster>", listOf(p("Choose the creature you control with the least toughness. Put N +1/+1 counters on it.")), "Keyword Action"),
        Keyword("<Bushido>", listOf(p("When this creature blocks or becomes blocked, it gets +N/+N until end of turn.")), "Keyword Ability"),
        Keyword("<Buyback>", listOf(p("You may pay an additional cost as you cast this spell. If you do, put it into your hand instead of the graveyard as it resolves.")), "Keyword Ability"),
        Keyword("<Cascade>", listOf(p("When you cast this spell, exile cards from the top of your library until you hit a nonland card with lesser mana value. You may cast it for free.")), "Keyword Ability"),
        Keyword("<Casualty>", listOf(p("As you cast this spell, you may sacrifice a creature with power N or greater for an additional effect.")), "Keyword Ability"),
        Keyword("<Case Cards>", listOf(p("Enchantments with a condition to 'solve' the case. Once solved, the card gains additional or upgraded effects.")), "Core Concept"),
        Keyword("<Changeling>", listOf(p("This card is every creature type at all times.")), "Keyword Ability"),
        Keyword("<Class Cards>", listOf(p("Enchantments with class levels (1-3) that can be unlocked by paying mana. Each level grants progressively stronger abilities.")), "Core Concept"),
        Keyword("<Cleave>", listOf(p("You may cast this spell for its cleave cost. If you do, remove the words in square brackets from its text.")), "Keyword Ability"),
        Keyword("<Collect Evidence>", listOf(p("Exile cards with total mana value N or greater from your graveyard as an additional cost.")), "Keyword Action"),
        Keyword("<Companion>", listOf(p("If your starting deck meets the companion's deckbuilding requirement, you may pay 3 to put it into your hand from outside the game once per game.")), "Keyword Ability"),
        Keyword("<Connive>", listOf(p("Draw a card, then discard a card. If a nonland card was discarded, put a +1/+1 counter on this creature.")), "Keyword Action"),
        Keyword("<Conspire>", listOf(p("As you cast this spell, you may tap two untapped creatures that share a color with it to copy it.")), "Keyword Ability"),
        Keyword("<Convoke>", listOf(p("You may tap untapped creatures you control to help pay for this spell. Each creature tapped pays for 1 mana of its color.")), "Keyword Ability"),
        Keyword("<Craft>", listOf(p("Pay the craft cost, exile this permanent and the specified materials, then return it transformed under your control.")), "Keyword Ability"),
        Keyword("<Creature>", listOf(p("The most common card type. Creatures have power and toughness, can attack and block during combat, and are subject to summoning sickness.")), "Core Concept"),
        Keyword("<Crew>", listOf(p("Tap any number of creatures you control with total power N or greater to have this Vehicle become an artifact creature until end of turn.")), "Keyword Ability"),
        Keyword("<Cycling>", listOf(p("Pay the cycling cost, discard this card, then draw a card.")), "Keyword Ability"),
        Keyword("<Day and Night>", listOf(p("A game state tracked globally. If a player casts no spells during their turn, it becomes night next turn. If they cast 2+ spells, it becomes day. Cards with daybound/nightbound transform accordingly.")), "Core Concept"),
        Keyword("<Dash>", listOf(p("You may cast this creature for its dash cost. If you do, it gains haste and returns to your hand at end of turn.")), "Keyword Ability"),
        Keyword("<Deathtouch>", listOf(p("Any amount of damage dealt by this creature to another creature is enough to destroy it.")), "Keyword Ability"),
        Keyword("<Decayed>", listOf(p("This creature can't block. When it attacks, sacrifice it at end of combat.")), "Keyword Ability"),
        Keyword("<Defender>", listOf(p("This creature cannot attack.")), "Keyword Ability"),
        Keyword("<Delve>", listOf(p("As you cast this spell, you may exile cards from your graveyard. Each card exiled pays for 1 generic mana.")), "Keyword Ability"),
        Keyword("<Demonstrate>", listOf(p("When you cast this spell, you may copy it. If you do, an opponent of your choice may also copy it.")), "Keyword Ability"),
        Keyword("<Destroy>", listOf(p("Move a permanent from the battlefield to its owner's graveyard.")), "Keyword Action"),
        Keyword("<Dethrone>", listOf(p("Whenever this creature attacks the player with the most or tied for most life, put a +1/+1 counter on it.")), "Keyword Ability"),
        Keyword("<Devour>", listOf(p("As this enters the battlefield, you may sacrifice any number of creatures. It enters with N +1/+1 counters for each creature sacrificed.")), "Keyword Ability"),
        Keyword("<Discard>", listOf(p("Take a card from your hand and put it into your graveyard.")), "Keyword Action"),
        Keyword("<Discover>", listOf(p("Exile cards from the top of your library until you hit a nonland with mana value N or less. Cast it for free or put it into your hand.")), "Keyword Action"),
        Keyword("<Disguise>", listOf(p("You may cast this card face-down as a 2/2 colorless creature for 3. Turn it face up for its disguise cost at any time.")), "Keyword Ability"),
        Keyword("<Disturb>", listOf(p("You may cast this card transformed from your graveyard.")), "Keyword Ability"),
        Keyword("<Double Strike>", listOf(p("This creature deals combat damage twice — once in the first strike step and again in the normal damage step.")), "Keyword Ability"),
        Keyword("<Dredge>", listOf(p("Instead of drawing a card, you may mill exactly N cards and return this card from your graveyard to your hand.")), "Keyword Ability"),
        Keyword("<Echo>", listOf(p("At the beginning of your next upkeep after this permanent comes under your control, sacrifice it unless you pay its echo cost.")), "Keyword Ability"),
        Keyword("<Emblem>", listOf(p("A marker in the command zone with one or more abilities. Created by planeswalker ultimates and other effects. Not a card or permanent — cannot be destroyed or removed.")), "Core Concept"),
        Keyword("<Embalm>", listOf(p("Pay the embalm cost and exile this card from your graveyard to create a token copy of it that's a white Zombie in addition.")), "Keyword Ability"),
        Keyword("<Enchantment>", listOf(p("A card type representing ongoing magical effects. Enchantments are permanents that stay on the battlefield and provide continuous or triggered abilities.")), "Core Concept"),
        Keyword("<Emerge>", listOf(p("You may cast this spell by sacrificing a creature and paying the emerge cost reduced by that creature's mana value.")), "Keyword Ability"),
        Keyword("<Enchant>", listOf(p("This Aura can be attached to the specified kind of permanent. If the enchanted permanent leaves, the Aura goes to the graveyard.")), "Keyword Ability"),
        Keyword("<Encore>", listOf(p("Pay the encore cost and exile this card from your graveyard to create a token copy for each opponent that must attack that opponent if able.")), "Keyword Ability"),
        Keyword("<Enlist>", listOf(p("As this creature attacks, you may tap an untapped nonattacking creature you control to add its power to this creature's.")), "Keyword Ability"),
        Keyword("<Entwine>", listOf(p("Pay the entwine cost to choose both modes of this modal spell instead of just one.")), "Keyword Ability"),
        Keyword("<Equip>", listOf(p("Pay the equip cost at sorcery speed to attach this Equipment to a creature you control.")), "Keyword Ability"),
        Keyword("<Escape>", listOf(p("You may cast this card from your graveyard for its escape cost by exiling additional cards from your graveyard.")), "Keyword Ability"),
        Keyword("<Evolve>", listOf(p("Whenever a creature with greater power or toughness enters under your control, put a +1/+1 counter on this creature.")), "Keyword Ability"),
        Keyword("<Exalted>", listOf(p("Whenever a creature you control attacks alone, that creature gets +1/+1 until end of turn for each Exalted permanent you control.")), "Keyword Ability"),
        Keyword("<Exile (Zone)>", listOf(p("A holding area outside the game. Exiled cards are much harder to interact with than those in the graveyard. Some effects exile temporarily and return cards later.")), "Core Concept"),
        Keyword("<Exile>", listOf(p("Remove a card from the game entirely. Exiled cards are much harder to interact with than those in the graveyard.")), "Keyword Action"),
        Keyword("<Exploit>", listOf(p("When this creature enters, you may sacrifice a creature for an additional effect.")), "Keyword Ability"),
        Keyword("<Explore>", listOf(p("Reveal the top card of your library. If it's a land, put it into your hand. Otherwise, put a +1/+1 counter on this creature and put the card back or into your graveyard.")), "Keyword Action"),
        Keyword("<Extort>", listOf(p("Whenever you cast a spell, you may pay W/B. If you do, each opponent loses 1 life and you gain life equal to the total lost.")), "Keyword Ability"),
        Keyword("<Fabricate>", listOf(p("When this creature enters, choose one: put N +1/+1 counters on it, or create N 1/1 colorless Servo artifact creature tokens.")), "Keyword Ability"),
        Keyword("<Fading>", listOf(p("This permanent enters with N fade counters. At your upkeep, remove one. If you can't, sacrifice it.")), "Keyword Ability"),
        Keyword("<Fight>", listOf(p("Two creatures deal damage equal to their power to each other.")), "Keyword Action"),
        Keyword("<First Strike>", listOf(p("This creature deals combat damage before creatures without first strike or double strike.")), "Keyword Ability"),
        Keyword("<Flash>", listOf(p("You may cast this spell any time you could cast an instant.")), "Keyword Ability"),
        Keyword("<Flashback>", listOf(p("You may cast this spell from your graveyard for its flashback cost. It is exiled afterward.")), "Keyword Ability"),
        Keyword("<Flying>", listOf(p("This creature can only be blocked by creatures with flying or reach.")), "Keyword Ability"),
        Keyword("<Foretell>", listOf(p("During your turn, you may pay 2 and exile this card face-down. Cast it on a later turn for its foretell cost.")), "Keyword Ability"),
        Keyword("<Frenzy>", listOf(p("Whenever this creature attacks and isn't blocked, it gets +N/+0 until end of turn.")), "Keyword Ability"),
        Keyword("<Fuse>", listOf(p("You may cast both halves of this split card from your hand.")), "Keyword Ability"),
        Keyword("<Goad>", listOf(p("A goaded creature must attack a player other than you if able during its controller's next combat.")), "Keyword Action"),
        Keyword("<Graft>", listOf(p("This creature enters with N +1/+1 counters. Whenever another creature enters, you may move a +1/+1 counter from this creature onto it.")), "Keyword Ability"),
        Keyword("<Graveyard>", listOf(p("Each player's discard pile. Cards go here when destroyed, discarded, sacrificed, countered, or milled. All cards are face-up and may be examined at any time.")), "Core Concept"),
        Keyword("<Haste>", listOf(p("This creature can attack and use tap abilities the turn it enters the battlefield — it ignores summoning sickness.")), "Keyword Ability"),
        Keyword("<Haunt>", listOf(p("When this card is put into a graveyard, exile it haunting target creature. The haunt ability works from exile.")), "Keyword Ability"),
        Keyword("<Hexproof>", listOf(p("This permanent or player cannot be the target of spells or abilities controlled by opponents.")), "Keyword Ability"),
        Keyword("<Hideaway>", listOf(p("When this permanent enters, look at the top N cards of your library, exile one face down, then put the rest on bottom. You may play the exiled card if conditions are met.")), "Keyword Ability"),
        Keyword("<Horsemanship>", listOf(p("This creature can only be blocked by creatures with horsemanship.")), "Keyword Ability"),
        Keyword("<Improvise>", listOf(p("You may tap untapped artifacts you control to help pay for this spell. Each artifact tapped pays for 1 generic mana.")), "Keyword Ability"),
        Keyword("<Indestructible>", listOf(p("This permanent cannot be destroyed by damage or effects that say 'destroy.' It can still be sacrificed, exiled, or have its toughness reduced to 0.")), "Keyword Ability"),
        Keyword("<Infect>", listOf(p("This creature deals damage to creatures in the form of -1/-1 counters and to players in the form of poison counters.")), "Keyword Ability"),
        Keyword("<Instant>", listOf(p("A one-shot spell type that can be cast at any time you have priority — including during your opponent's turn. Goes to the graveyard after resolving.")), "Core Concept"),
        Keyword("<Ingest>", listOf(p("Whenever this creature deals combat damage to a player, that player exiles the top card of their library.")), "Keyword Ability"),
        Keyword("<Intimidate>", listOf(p("This creature can only be blocked by artifact creatures and creatures that share a color with it.")), "Keyword Ability"),
        Keyword("<Investigating>", listOf(p("Create a colorless Clue artifact token with '2, Sacrifice this artifact: Draw a card.'")), "Keyword Action"),
        Keyword("<Kicker>", listOf(p("You may pay an additional cost as you cast this spell for an added effect.")), "Keyword Ability"),
        Keyword("<Land>", listOf(p("A permanent type that produces mana when tapped. You may play 1 land per turn. Lands are colorless and are not spells — they cannot be countered.")), "Core Concept"),
        Keyword("<Landwalk>", listOf(p("If the defending player controls a land of the specified type, this creature cannot be blocked.")), "Keyword Ability"),
        Keyword("<Learn>", listOf(p("You may discard a card to draw a card. If you do and you own a Lesson card outside the game, you may reveal it and put it into your hand.")), "Keyword Action"),
        Keyword("<Lifelink>", listOf(p("When this creature deals damage, you gain that much life simultaneously.")), "Keyword Ability"),
        Keyword("<Library>", listOf(p("Your draw pile, kept face-down. Cards are drawn from here. Neither player may look at or change the order of cards in a library unless an effect allows it.")), "Core Concept"),
        Keyword("<Living Weapon>", listOf(p("When this Equipment enters, create a 0/0 black Phyrexian Germ creature token and attach this Equipment to it.")), "Keyword Ability"),
        Keyword("<Madness>", listOf(p("If you discard this card, you may exile it. If you do, cast it for its madness cost or put it into your graveyard.")), "Keyword Ability"),
        Keyword("<Manifest>", listOf(p("Put the top card of your library onto the battlefield face down as a 2/2 creature. Turn it face up at any time if it's a creature card by paying its mana cost.")), "Keyword Action"),
        Keyword("<Meld>", listOf(p("When the two specified cards are on the battlefield, exile them and return them transformed as one combined permanent.")), "Keyword Action"),
        Keyword("<Menace>", listOf(p("This creature can only be blocked by two or more creatures.")), "Keyword Ability"),
        Keyword("<Mentor>", listOf(p("Whenever this creature attacks, put a +1/+1 counter on target attacking creature with lesser power.")), "Keyword Ability"),
        Keyword("<Mill>", listOf(p("Put the top N cards of a library into that player's graveyard.")), "Keyword Action"),
        Keyword("<Miracle>", listOf(p("If the first card you draw each turn has miracle, you may reveal it and cast it for its miracle cost immediately.")), "Keyword Ability"),
        Keyword("<Modular>", listOf(p("This creature enters with N +1/+1 counters. When it dies, you may put its +1/+1 counters on target artifact creature.")), "Keyword Ability"),
        Keyword("<Monstrosity>", listOf(p("Pay the cost to put N +1/+1 counters on this creature. It becomes monstrous and may trigger an additional effect.")), "Keyword Action"),
        Keyword("<Morph>", listOf(p("You may cast this card face-down as a 2/2 colorless creature for 3. Turn it face up at any time for its morph cost.")), "Keyword Ability"),
        Keyword("<Mutate>", listOf(p("Cast this spell on top or underneath a non-Human creature you own. The mutated creature has all abilities of the top card plus those of cards below it.")), "Keyword Ability"),
        Keyword("<Myriad>", listOf(p("Whenever this creature attacks, for each opponent other than the defending player, create a tapped and attacking token copy. Exile those tokens at end of combat.")), "Keyword Ability"),
        Keyword("<Ninjutsu>", listOf(p("Return an unblocked attacker to hand, pay ninjutsu cost, then put this card from your hand onto the battlefield tapped and attacking.")), "Keyword Ability"),
        Keyword("<Offering>", listOf(p("You may cast this spell at the time you could cast an instant by sacrificing a creature of the specified type, reducing the cost by that creature's mana value.")), "Keyword Ability"),
        Keyword("<Omen Cards>", listOf(p("Cards with an Omen cost — a cheaper alternative cost. When cast for its Omen cost, the card is shuffled back into its owner's library instead of going to the graveyard.")), "Core Concept"),
        Keyword("<Outlast>", listOf(p("Pay the outlast cost and tap this creature to put a +1/+1 counter on it. Activate only as a sorcery.")), "Keyword Ability"),
        Keyword("<Overload>", listOf(p("Pay the overload cost instead of the mana cost. If you do, replace 'target' with 'each' in the spell's text.")), "Keyword Ability"),
        Keyword("<Partner>", listOf(p("You may have two commanders. Their color identities combine for deck construction. Each has its own commander tax.")), "Keyword Ability"),
        Keyword("<Persist>", listOf(p("When this creature dies, if it had no -1/-1 counters, return it to the battlefield under its owner's control with a -1/-1 counter.")), "Keyword Ability"),
        Keyword("<Permanent>", listOf(p("A card or token currently on the battlefield. Permanents include creatures, artifacts, enchantments, planeswalkers, battles, and lands.")), "Core Concept"),
        Keyword("<Planeswalker>", listOf(p("A permanent type with loyalty counters. Activate one loyalty ability per turn per planeswalker at sorcery speed. Can be attacked directly by creatures and damaged by spells.")), "Core Concept"),
        Keyword("<Plot>", listOf(p("Exile this card from your hand. You may cast it on a future turn without paying its mana cost. Plot only as a sorcery.")), "Keyword Ability"),
        Keyword("<Poisonous>", listOf(p("Whenever this creature deals combat damage to a player, that player gets N poison counters.")), "Keyword Ability"),
        Keyword("<Power/Toughness>", listOf(p("The two numbers (X/Y) on creature cards. Power is the amount of combat damage the creature deals. Toughness is the amount of damage needed to destroy it.")), "Core Concept"),
        Keyword("<Populate>", listOf(p("Create a token that's a copy of a creature token you control.")), "Keyword Action"),
        Keyword("<Preparation Cards>", listOf(p("Cards you may exile face-down from your hand as a sorcery. On a future turn, you may cast them from exile without paying their mana cost.")), "Core Concept"),
        Keyword("<Proliferate>", listOf(p("Choose any number of permanents and/or players that have counters, then give each one more counter of each kind already there.")), "Keyword Action"),
        Keyword("<Protection>", listOf(p("Cannot be Damaged, Enchanted/Equipped, Blocked, or Targeted by sources of the stated quality (mnemonic: DEBT).")), "Keyword Ability"),
        Keyword("<Prowess>", listOf(p("Whenever you cast a noncreature spell, this creature gets +1/+1 until end of turn.")), "Keyword Ability"),
        Keyword("<Rad Counters>", listOf(p("Counters that cause a player to mill cards and take damage at the beginning of their precombat main phase. The number of cards milled equals the number of rad counters.")), "Core Concept"),
        Keyword("<Ravenous>", listOf(p("This creature enters with N +1/+1 counters. If N or more was paid as part of X, draw a card when it enters.")), "Keyword Ability"),
        Keyword("<Reach>", listOf(p("This creature can block creatures with flying.")), "Keyword Ability"),
        Keyword("<Read Ahead>", listOf(p("As this Saga enters, choose a chapter and start there instead of chapter 1. You still get all previous chapter abilities.")), "Keyword Ability"),
        Keyword("<Rebound>", listOf(p("If you cast this spell from your hand, exile it as it resolves. At the beginning of your next upkeep, you may cast it from exile without paying its mana cost.")), "Keyword Ability"),
        Keyword("<Reconfigure>", listOf(p("Pay the reconfigure cost to attach this Equipment creature to another creature or unattach it. While attached, it's not a creature.")), "Keyword Ability"),
        Keyword("<Regenerate>", listOf(p("The next time this permanent would be destroyed this turn, instead tap it, remove it from combat, and remove all damage from it.")), "Keyword Action"),
        Keyword("<Renown>", listOf(p("When this creature deals combat damage to a player, if it isn't renowned, put N +1/+1 counters on it and it becomes renowned.")), "Keyword Ability"),
        Keyword("<Replicate>", listOf(p("As you cast this spell, you may pay its replicate cost any number of times. If you do, copy it that many times.")), "Keyword Ability"),
        Keyword("<Retrace>", listOf(p("You may cast this card from your graveyard by discarding a land card as an additional cost.")), "Keyword Ability"),
        Keyword("<Riot>", listOf(p("This creature enters with your choice of a +1/+1 counter or haste.")), "Keyword Ability"),
        Keyword("<Sacrifice>", listOf(p("Move a permanent you control from the battlefield to its owner's graveyard. Cannot be prevented by indestructible.")), "Keyword Action"),
        Keyword("<Scavenge>", listOf(p("Pay the scavenge cost and exile this card from your graveyard to put N +1/+1 counters on target creature.")), "Keyword Ability"),
        Keyword("<Scry>", listOf(p("Look at the top N cards of your library. Put any number on the bottom and the rest back on top in any order.")), "Keyword Action"),
        Keyword("<Shadow>", listOf(p("This creature can only block or be blocked by creatures with shadow.")), "Keyword Ability"),
        Keyword("<Skulk>", listOf(p("This creature can't be blocked by creatures with greater power.")), "Keyword Ability"),
        Keyword("<Soulbond>", listOf(p("You may pair this creature with another unpaired creature when either enters. As long as they remain paired, both gain the listed ability.")), "Keyword Ability"),
        Keyword("<Sorcery>", listOf(p("A one-shot spell type castable only during your main phase when the stack is empty. Goes to the graveyard after resolving.")), "Core Concept"),
        Keyword("<Spectacle>", listOf(p("You may cast this spell for its spectacle cost if an opponent lost life this turn.")), "Keyword Ability"),
        Keyword("<Split Cards>", listOf(p("Cards with two halves separated by a dividing line. You may cast either half. Some have Fuse allowing both halves to be cast together.")), "Core Concept"),
        Keyword("<Splice>", listOf(p("As you cast an instant or sorcery spell, you may reveal this card and pay its splice cost to add its effects to that spell.")), "Keyword Ability"),
        Keyword("<Split Second>", listOf(p("While this spell is on the stack, players can't cast spells or activate nonmana abilities.")), "Keyword Ability"),
        Keyword("<Station Cards>", listOf(p("Enchantments with abilities that can be activated by tapping (stationing) themselves. Often part of a cycle where each station has a related effect.")), "Core Concept"),
        Keyword("<Squad>", listOf(p("As you cast this spell, you may pay the squad cost any number of times. Create that many token copies of this creature.")), "Keyword Ability"),
        Keyword("<Stack>", listOf(p("The zone where spells and abilities wait to resolve. Uses a last-in, first-out (LIFO) system. Both players may respond before anything resolves.")), "Core Concept"),
        Keyword("<Storm>", listOf(p("When you cast this spell, copy it for each spell cast before it this turn. You may choose new targets for the copies.")), "Keyword Ability"),
        Keyword("<Sunburst>", listOf(p("This permanent enters with a +1/+1 counter for each color of mana spent to cast it.")), "Keyword Ability"),
        Keyword("<Surge>", listOf(p("You may cast this spell for its surge cost if you or a teammate have cast another spell this turn.")), "Keyword Ability"),
        Keyword("<Surveil>", listOf(p("Look at the top N cards of your library. Put any number into your graveyard and the rest back on top in any order.")), "Keyword Action"),
        Keyword("<Suspect>", listOf(p("A suspected creature has menace and can't block. It remains suspected until it leaves the battlefield.")), "Keyword Action"),
        Keyword("<Suspend>", listOf(p("Exile this card from your hand with N time counters. At your upkeep, remove one. When the last is removed, cast it without paying its mana cost. It gains haste.")), "Keyword Ability"),
        Keyword("<The Initiative>", listOf(p("A designation a player can have. Take the initiative to venture into the Undercity dungeon. Only one player can have the initiative at a time.")), "Core Concept"),
        Keyword("<The Monarch>", listOf(p("A designation a player can have. At the beginning of the monarch's end step, they draw a card. When a creature deals combat damage to the monarch, its controller becomes the monarch.")), "Core Concept"),
        Keyword("<Toxic>", listOf(p("Combat damage from this creature also gives the player N poison counters in addition to regular damage.")), "Keyword Ability"),
        Keyword("<Training>", listOf(p("Whenever this creature and another creature with greater power attack together, put a +1/+1 counter on this creature.")), "Keyword Ability"),
        Keyword("<Trample>", listOf(p("Excess combat damage dealt to blockers spills over to the defending player or planeswalker.")), "Keyword Ability"),
        Keyword("<Transform>", listOf(p("Flip the card over to its other face. The transformed side is now the active face.")), "Keyword Action"),
        Keyword("<Transmute>", listOf(p("Pay the transmute cost, discard this card, then search your library for a card with the same mana value, reveal it, put it into your hand, and shuffle.")), "Keyword Ability"),
        Keyword("<Umbra Armor>", listOf(p("If the enchanted creature would be destroyed, instead remove all damage from it and destroy this Aura.")), "Keyword Ability"),
        Keyword("<Undying>", listOf(p("When this creature dies, if it had no +1/+1 counters, return it to the battlefield under its owner's control with a +1/+1 counter.")), "Keyword Ability"),
        Keyword("<Unearth>", listOf(p("Pay the unearth cost to return this creature from your graveyard to the battlefield. It gains haste. Exile it at the beginning of the next end step or if it would leave.")), "Keyword Ability"),
        Keyword("<Unleash>", listOf(p("You may have this creature enter with a +1/+1 counter. If you do, it can't block.")), "Keyword Ability"),
        Keyword("<Vanishing>", listOf(p("This permanent enters with N time counters. At your upkeep, remove one. When the last is removed, sacrifice it.")), "Keyword Ability"),
        Keyword("<Vigilance>", listOf(p("Attacking does not cause this creature to tap.")), "Keyword Ability"),
        Keyword("<Ward>", listOf(p("Whenever this permanent becomes the target of a spell or ability an opponent controls, counter it unless that player pays the ward cost.")), "Keyword Ability"),
        Keyword("<Wither>", listOf(p("This creature deals damage to creatures in the form of -1/-1 counters.")), "Keyword Ability"),
    )

    fun std(id: String, n: String, d: List<InfoCard>) = RulesConfig(id, n, baseSetup, victory, baseTurn, keywords, listOf(PdfAsset("Comprehensive Rules", "rulebooks/mtg-comprehensive.pdf")), deckRules = d, fieldContent = standardField)
    fun cmd(id: String, n: String, d: List<InfoCard>) = RulesConfig(id, n, commanderSetup, commanderVictory, commanderTurn, keywords, listOf(PdfAsset("Comprehensive Rules", "rulebooks/mtg-comprehensive.pdf")), deckRules = d, fieldContent = commanderField)
    fun du1(id: String, n: String, d: List<InfoCard>) = RulesConfig(id, n, duelCommanderSetup, commanderVictory, duelCommanderTurn, keywords, listOf(PdfAsset("Comprehensive Rules", "rulebooks/mtg-comprehensive.pdf")), deckRules = d, fieldContent = duelCommanderField)

    val configs: List<RulesConfig> = listOf(
        std("standard", "Standard", stdDeck),
        std("modern", "Modern", modDeck),
        std("pioneer", "Pioneer", pioDeck),
        std("legacy", "Legacy", legDeck),
        std("vintage", "Vintage", vinDeck),
        std("premodern", "Premodern", preDeck),
        std("pauper", "Pauper", pauDeck),
        cmd("commander", "Commander", cmdrDeck),
        du1("duel-commander", "Duel Commander", duelDeck),
    )
}
