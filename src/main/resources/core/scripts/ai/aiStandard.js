
/*
 * A simple AI script that attempts to activate damage, debuff, and buff type abilities
 * Once the creature runs out of abilities to activate, it falls back to the basic AI
 */

function runTurn(game, parent) {
	if (parent.getEncounter() != null) {
		// perform no actions if this AI's encounter hasn't spotted any hostiles yet
		if (parent.getEncounter().getHostiles().size() == 0) {
			return;
		}
	}

	var aiSet = parent.abilities.createAISet();
	
	// figure out the set of usable action types
	var usableActionTypes = [ "Buff", "Debuff", "Damage", "Summon" ];
	
	// see if healing spells should be in the mix
	if (checkForHealingSpells(game, parent, aiSet)) {
		usableActionTypes = usableActionTypes.concat("Heal");
	}
	
	var allSlots = aiSet.getWithActionTypes(usableActionTypes);
	
	// add any specific tactical spells that should be used
	// most will provide a corresponding target
	var preferredTarget = checkForTacticalAbilities(game, parent, aiSet, allSlots,
		[checkTotalDefense, checkRenewal, checkDispell]);
	
	// check if we have any valid abilities to use
	// if not, fall back to basic AI
	var numAbilities = allSlots.size();
	if (numAbilities == 0) {
		fallbackToBasicAI(game, parent);
		return;
	}
	
	// go through all of the abilities in the list in order and try them until
	// we run out of AP
	for (var i = 0; i < allSlots.size(); i++) {
		// reset the preferred target, only the first entry (the tactical ability)
		// should have a preferred target
		if (i != 0)
			preferredTarget = null;
	
		var slot = allSlots.get(i);
		
		// don't try to activate slots which are already active
		if (slot.isActive()) continue;
		
		// first attempt to move within range as needed
		var targetData = moveTowardsForAbility(game, parent, slot, preferredTarget);
		
		if (targetData.endTurn)
			return;
		
		// at this point, the ability will either be used or be determined to be unusable
		// this turn
		numAbilities--;
		
		if (!targetData.targetFound)
			continue;

		// now try to activate the ability
		var activateData = tryActivateAbility(game, parent, targetData.target, slot, aiSet);
			
		// we can either end the turn here or try another ability
		if (activateData.endTurn)
			return;
	}

	// if all abilities have been used at this point, we may still have AP left
	// to fall back to basic AI
	if (numAbilities == 0) {
		fallbackToBasicAI(game, parent);
		return;
	}
}

/*
 * checks the status of nearby friendlies to see if healing spells should be added to the
 * set of usable abilities
 */

function checkForHealingSpells(game, parent, aiSet) {
	// check to see if we have any healing spells
	var healingSlots = aiSet.getWithActionType("Heal");
	if (healingSlots.size() > 0) {
		// if we have a healing spell, check if there are any good targets
		
		var friendlies = game.ai.getLiveVisibleCreatures(parent, "Friendly");
		
		// check for a friendly below 1/2 Max HP
		for (var i = 0; i < friendlies.size(); i++) {
			var friendly = friendlies.get(i);
			
			if (friendly.getCurrentHitPoints() / friendly.stats.getMaxHP() < 0.5) {
				// we found a good target
				return true;
			}
		}
	}
	
	return false;
}

/*
 * checks the combat situation to see if various tactical abilities
 * should be potentially used.  this method will return after it finds
 * one usable tactical ability.  Returns the target position which should
 * be used with the chosen tactical ability
 */

function checkForTacticalAbilities(game, parent, aiSet, allSlots, functions) {
	for (var i = 0; i < functions.length; i++) {
		var target = functions[i](game, parent, aiSet, allSlots);
		
		if (target != null) return target;
	}
	
	return null;
}

function checkTotalDefense(game, parent, aiSet, allSlots) {
	if (!parent.abilities.has("TotalDefense")) return null;
	
	var nearbyHostiles = game.ai.getTouchableCreatures(parent, "Hostile");
	if (nearbyHostiles.size() <= 2) return null;
	
	// if the parent is surrounded by hostiles, now is a good time to use total defense
	var slot = parent.abilities.getSlotWithReadiedAbility("TotalDefense");
	if (slot == null) return null;
	
	allSlots.add(0, slot);
	return parent.getLocation();
}

function checkRenewal(game, parent, aiSet, allSlots) {
	if (!parent.abilities.has("Renewal")) return null;
	
	var slot = parent.abilities.getSlotWithReadiedAbility("Renewal");
	if (slot == null) return null;
	
	var ability = slot.getAbility();
	var friendlies = game.ai.getLiveVisibleCreatures(parent, "Friendly");
	
	for (var i = 0; i < friendlies.size(); i++) {
		var friendly = friendlies.get(i);
		
		var isValid = ability.executeFunction("isTargetValid", friendly, slot);
		
		if (isValid == true) {
			// if the friendly has attribute penalties, then add renewal to the list of spells
			allSlots.add(0, slot);
			return friendly.getLocation();
		}
	}
}

function checkDispell(game, parent, aiSet, allSlots) {
	if (!parent.abilities.has("Dispell")) return null;
	
	var slot = parent.abilities.getSlotWithReadiedAbility("Dispell");
	if (slot == null) return null;
	
	var ability = slot.getAbility();
	var friendlies = game.ai.getLiveVisibleCreatures(parent, "Friendly");
	
	for (var i = 0; i < friendlies.size(); i++) {
		var friendly = friendlies.get(i);
		
		var isValid = ability.executeFunction("isTargetValid", friendly, slot);
		
		if (isValid == true) {
			// if the friendly has dispellable effects, then add dispell to the list of spells
			allSlots.add(0, slot);
			return friendly.getLocation();
		}
	}
}

function fallbackToBasicAI(game, parent) {
    game.runExternalScript("ai/aiBasic", "runTurn", parent);
}

/*
 * Handles menu selections as needed, then returns a targeter
 */

function getTargeter(game, parent, target, slot, aiSet) {
	var abilityID = slot.getAbility().getID();
	
	if (abilityID.equals("Summon")) {
		var menuSelectionsSorted = [ "Giant Wolf", "Yeti", "Giant Spider", "Large Wolf",
			"Sabretooth", "Bear", "Medium Wolf", "Tiger", "Small Wolf", "Rat" ];
		var elementals = shuffle([ "Air Elemental", "Earth Elemental",
			"Fire Elemental", "Water Elemental" ]);
		
		var selections = elementals.concat(menuSelectionsSorted);
		
		return aiSet.activateAndGetTargeter(slot, selections);
	} else {
		return aiSet.activateAndGetTargeter(slot);
	}
}

function tryActivateAbility(game, parent, target, slot, aiSet) {
	// if the ability has a helper script for validation, call it
	// otherwise use the default which checks for an effect from the spell on the target to
	// avoid duplicates
	if (slot.getAbility().hasFunction("aiCheckTargetValid")) {
		var targetValid = slot.getAbility().executeFunction("aiCheckTargetValid", slot, target);
		
		if (targetValid == false) {
			return { 'endTurn' : false };
		}
	} else {
		var targetCreature = target.getCreature();
		if (targetCreature != null) {
			// check to see if this ability has already been applied to the target
			var effect = targetCreature.getEffects().getEffectCreatedBySlot(slot.getAbilityID());
			if (effect != null) {
				return { 'endTurn' : false };
			}
		}
	}

	if (slot.getAbility().getSpellLevel() > 0) {
		// if this is a spell, check for conditions causing total spell failure
		// this will not factor in concealment, but if it too high then the spell
		// is very unlikely to be cast successfully
		if ( slot.getAbility().getSpellFailurePercentage(parent) >= 70 ) {
			// no use in attempting to cast the spell
			return { 'endTurn' : false };
		}
	}

	var targeter = getTargeter(game, parent, target, slot, aiSet);
	
	// if we could not activate the ability, it means we most likely don't
	// have enough AP and probably can't do anything useful
	if (targeter == null) {
		return { 'endTurn' : true };
	}
	
	// try to activate on our preferred target
	targeter.setMousePosition(target);
	
	// check to see if we have a valid selection here
	var condition = targeter.getMouseActionCondition().toString();
	
	if (condition.equals("TargetSelect")) {
		// activate the ability;
		targeter.performLeftClickAction();
		
		//println("AI activating " + slot.getAbility().getID() + " with target count " +
		//	targeter.getDesirableTargetCount() + " desirable, " + targeter.getUndesirableTargetCount() + " undesirable.");
		
		game.sleepStandardDelay(4);
		
		// we may have enough AP to activate another ability
		return { 'endTurn' : false };
	}
	
	// if we didn't have a valid selection with our target, check for
	// a list of valid selections
	var selectable = targeter.getAllowedPoints();
	
	if (!selectable.isEmpty()) {
		// targeter has specific clickable points, so choose the first one
		targeter.setMousePosition(selectable.get(0));
		targeter.performLeftClickAction();
		
		//println("AI activating " + slot.getAbility().getID() + " with target count " +
		//	targeter.getDesirableTargetCount() + " desirable, " + targeter.getUndesirableTargetCount() + " undesirable.");
		
		game.sleepStandardDelay(4);
		
		return { 'endTurn' : false };
	}
	
	// we weren't able to activate the targeter
	targeter.cancel();
	
	return { 'endTurn' : false };
}

/*
 * moves the parent creature as neccessary in order to be in range to use
 * the specified ability slot
 */

function moveTowardsForAbility(game, parent, slot, preferredTarget) {
	// if the slot cannot activate, it means we don't have enough AP
	// and probably can't do anything useful
	if (!slot.canActivate()) {
		return { 'endTurn' : true };
	}

	var ability = slot.getAbility();
	
	// figure out how far away we should be
	var rangeType = ability.getRangeType().toString();
	
	var preferredDistance = 0;
	if (rangeType.equals("Touch")) preferredDistance = 1;
	else if (rangeType.equals("Short")) preferredDistance = 2;
	else if (rangeType.equals("Long")) preferredDistance = 6;

	// if we don't already have a target, try to find one
	if (preferredTarget == null) {
		// find the best target for our ability
		var preferredTarget = findBestTarget(game, preferredDistance, parent, slot);
	}

	// if no target was found, return and try a different ability
	if (preferredTarget == null) {
		return { 'endTurn' : false, 'targetFound' : false };
	}

	// now move towards the target as needed until we either run out of AP or are in position to
	// use the ability
	var curDistance = parent.getLocation().getDistance(preferredTarget);
	
	// infinite loop protection
	var loopNum = 0;
	
	while (curDistance > preferredDistance) {
		// if creature cannot move any more
		if (parent.timer.getMovementLeft() == 0 || parent.stats.isImmobilized())
			return { 'endTurn' : true };
	
		var moved = game.ai.moveTowards(parent, preferredTarget, preferredDistance);
		
		// if no movement occurred, the target is probably blocked
		// but we may still be able to use another ability
		if (!moved) {
			return { 'endTurn' : false };
		}
		
		var curDistance = parent.getLocation().getDistance(preferredTarget);
		
		if (loopNum > 30)
			return { 'endTurn' : true };
		
		loopNum++;
	}
	
	// make sure we still have enough AP to use the ability
	if (!slot.canActivate()) {
		return { 'endTurn' : true };
	}
	
	// at this point we should be able to activate the ability on our target
	return { 'endTurn' : false, 'targetFound' : true, 'target' : preferredTarget };
}

/*
 * Finds the "best" (in some sense) target for the given ability with the
 * assumed preferred target distance.  The returned value is the position of
 * that target
 */

function findBestTarget(game, preferredDistance, parent, slot) {
	// figure out whether we should target friendlies or hostiles
	var actionType = slot.getAbility().getActionType().toString();
	
	var targetRelationship = "Friendly";
	if (actionType.equals("Damage") || actionType.equals("Debuff")) {
		targetRelationship = "Hostile";
	}
	
	// if it is self targeted
	if (preferredDistance == 0) {
		return parent.getLocation();
	}
	
	// for summon spells, try to find an empty tile to summon the creature onto
	if (actionType.equals("Summon")) {
		var position = game.ai.findClosestEmptyTile(parent.getLocation().toPoint(), preferredDistance);
		
		// we return the position that was found, even if it is null
		// this should be a very rare event since it requires all tiles around the caster
		// to be unusable
		return position;
	}
	
	// get the list of all targets sorted closest first
	var allTargets = game.ai.getLiveVisibleCreatures(parent, targetRelationship);
	game.ai.sortCreatureListClosestFirst(parent, allTargets);
	
	// find the best target based on action type
	if (actionType.equals("Heal")) {
		// find the most damaged friendly
		var target = findBestHealTarget(game, allTargets, parent, slot);
	} else {
		// find the closest target match
		var target = findClosestTarget(game, allTargets, parent, slot);
	}
	
	if (target != null)
		return target.getLocation();
	else
		return null;
}

/*
 * it is assumed that the creatures array is already sorted closest first
 */

function findBestHealTarget(game, creatures, parent, slot) {
	var bestTarget = null;
	var bestHP = 1.0;
	
	for (var i = 0; i < creatures.size(); i++) {
		var hpFraction = creatures.get(i).getCurrentHitPoints() / creatures.get(i).stats.getMaxHP();
		
		// favor earlier (closer) entries in the inequality when ties arise
		if (hpFraction < bestHP) {
			bestTarget = creatures.get(i);
			bestHP = hpFraction;
		}
	}
	
	return bestTarget;
}

/*
 * it is assumed that the creatures array is already sorted closest first
 */

function findClosestTarget(game, creatures, parent, slot) {
	var ability = slot.getAbility();
	var preferredTarget = null;
	
	if (ability.hasFunction("isTargetValid")) {
		// the ability can validate targets
		for (var i = 0; i < creatures.size(); i++) {
			var curTarget = creatures.get(i);
		
			var isValid = ability.executeFunction("isTargetValid", curTarget, slot);
			
			if (isValid == true) {
				preferredTarget = curTarget;
				break;
			}
		}
	} else {
		// the ability does not validate targets so just pick the closest one
		if (creatures.size() > 0)
			preferredTarget = creatures.get(0);
	}
	
	return preferredTarget;
}

function shuffle(array) {
    var tmp, current, top = array.length;

    if (top) while(--top) {
        current = Math.floor(Math.random() * (top + 1));
        tmp = array[current];
        array[current] = array[top];
        array[top] = tmp;
    }

    return array;
}