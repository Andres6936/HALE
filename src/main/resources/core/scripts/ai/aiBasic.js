/*
 * A basic AI script that attempts to attack with the weapon currently in the creature's hands.
 * It attempts to close to an appropriate range and then attack as many times as possible
 */

function runTurn(game, parent) {
    var preferredDistance = 1;
    var weapon = parent.inventory.getEquippedMainHand();
    
	// compute preferred distance for melee or ranged weapon
    if (weapon != null) {
        if (weapon.isMelee()) {
            if (weapon.getTemplate().threatensAoOs()) preferredDistance = weapon.getTemplate().getMaxRange();
            else preferredDistance = 1;
        } else {
            preferredDistance = weapon.getTemplate().getMaxRange();
			if (preferredDistance > game.currentArea().getVisibilityRadius()) {
				preferredDistance = game.currentArea().getVisibilityRadius();
			}
        }
    }

	var triedToMoveLastIteration = false;
	var locationLastIteration = parent.getLocation();
	
	// move towards opponents and attack as many times as possible
    for (var i = 0; i < 8; i++) {
		// stop if we died for any reason, such as an AoO
		if (parent.isDead()) break;
		
		// stop if we tried to move but were unable to move at all
		if (triedToMoveLastIteration && parent.getLocation().equals(locationLastIteration)) {
			break;
		}
	
		var potentialTargets = game.ai.getPotentialAttackTargets(parent);
		
		if (potentialTargets.size() == 0) break; // no targets, nothing to do
		
		if (parent.getMainHandWeapon().isMelee()) {
			potentialTargets.sortFewestAoOsFirst();
			var target = potentialTargets.getTarget(0);
			
			// find the weakest target with the fewest AoOs
			var fewestHP = 10000;
			var numAoOs = potentialTargets.getPath(0).getNumAoOs();
			for (var j = 0; j < potentialTargets.size(); j++) {
				if (potentialTargets.getPath(j).getNumAoOs() > numAoOs)
					break;
					
				var curTarget = potentialTargets.getTarget(j);
				if (curTarget.getCurrentHitPoints() < fewestHP) {
					target = curTarget;
					fewestHP = target.getCurrentHitPoints();
				}
			}
		} else {
			potentialTargets.sortClosestFirst();
			var target = potentialTargets.getTarget(0);
			
			// find the weakest closest target
			var fewestHP = 10000;
			var distance = potentialTargets.getDistance(0);
			for (var j = 0; j < potentialTargets.size(); j++) {
				if (potentialTargets.getDistance(j) > distance)
					break;
				
				var curTarget = potentialTargets.getTarget(j);
				if (curTarget.getCurrentHitPoints() < fewestHP) {
					target = curTarget;
					fewestHP = target.getCurrentHitPoints();
				}
			}
		}
		
        var curDistance = parent.getLocation().getDistance(target);
        
        var distance = preferredDistance;
        
		// if we are already closer than preferred distance and cannot attack, attempt to
		// move in by 2 and attack again
        if (preferredDistance >= curDistance) distance = curDistance - 2;
        
        if (!parent.timer.canAttack()) {
			// if we don't have enough AP to attack, nothing else to do
            break;
        } else if (!game.creatureCanAttackTarget(parent, target)) {
			triedToMoveLastIteration = true;
			locationLastIteration = parent.getLocation();
		
			// if we cannot attack, then attempt to move towards the target
			var initialDistance = distance;
			
			// try to move next to the target.  if that fails, try to move one square further away
			// from the target, and so on
			
			// infinite loop protection
			var loopNum = 0;
			
			while (!game.ai.moveTowards(parent, target.getLocation(), distance)) {
				distance++;
				
				if (distance >= curDistance)
					break;
				
				if (loopNum > 30)
					break;
				
				loopNum++;
			}
			
			// if we were unable to move the initial distance, then we cannot attack
			// and should end our turn
            if (distance > initialDistance) {
                break;
            }
        } else if (parent.inventory.hasAmmoEquippedForWeapon()) {
			triedToMoveLastIteration = false;
		
			// attack the target
            game.standardAttack(parent, target);
        } else {
			// we have no ammo for our weapon and cannot attack
            break;
        }
    }
}

function takeAttackOfOpportunity(game, parent, target) {
    return true;
}
