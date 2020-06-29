function canActivate(game, parent) {
	var weapon = parent.getMainHandWeapon();
	if (!weapon.isMelee()) return false;
	
	if (parent.stats.isImmobilized() || parent.stats.isHelpless()) return false;
	
	var attackCost = parent.stats.getAttackCost();
	//var moveCost = parent.stats.getMovementCost();
	
	//return parent.timer.canPerformAction(attackCost + 2 * moveCost);
	
	return parent.timer.canPerformAction(attackCost);
}

function onActivate(game, slot) {
	var parent = slot.getParent();

	// figure out the shortest and longest distances that can be charged based on available
	// AP and weapon range
	var weapon = parent.getMainHandWeapon();
	var maxDistAway = 1;
	if (weapon.getTemplate().threatensAoOs()) maxDistAway = weapon.getTemplate().getMaxRange();
	
	var minDistAway = 1;
	if (weapon.getTemplate().threatensAoOs()) minDistAway = weapon.getTemplate().getMinRange();
	
	//var moveAPLeft = (parent.timer.getAP() - parent.stats.getAttackCost());
	//var maxDist = maxDistAway + parseInt(moveAPLeft / parent.stats.getMovementCost());
	
	var targeter = game.createLineTargeter(slot);
	
	targeter.setOrigin(parent.getLocation());
	targeter.setMaxRange(8);
	//targeter.setMaxRange(maxDist);
	targeter.setMinRange(2 + minDistAway);
	targeter.setRelationshipCriterion("Hostile");
	targeter.setAllowAffectedCreaturesEmpty(false);
	targeter.setStopLineAtCreature(true);
	targeter.setStopLineAtImpassable(true);
	targeter.activate();
}

function onTargetSelect(game, targeter) {
	targeter.getSlot().activate();

	var parent = targeter.getParent();
	var target = targeter.getAffectedCreatures().get(0);
	var ability = targeter.getSlot().getAbility();
	
	var weapon = parent.getMainHandWeapon();
	var distance = parent.getLocation().getDistance(target);
	
	// normally, charge to the weapon's maximum range and attack
	var distAway = 1;
	if (weapon.getTemplate().threatensAoOs()) distAway = weapon.getTemplate().getMaxRange();
	
	// if the maximum range doesn't give at least 2 movement tiles,
	// then charge to a shorter distance instead
	if (distance - distAway < 2) {
		distAway = distance - 2;
	}
	
	var cb = ability.createDelayedCallback("performCharge");
	cb.addArgument(parent);
	cb.addArgument(target);
	cb.addArgument(distAway);
	cb.start();
}

function performCharge(game, parent, target, distanceAway) {
	// apply a temporary effect with the bonuses
	var parentEffect = parent.createEffect();
	parentEffect.getBonuses().addBonus('Attack', 'Stackable', 40);
	parentEffect.getBonuses().addBonus('Damage', 'Stackable', 100);
	parentEffect.getBonuses().addBonus('Movement', 'Stackable', 1000);
	parent.applyEffect(parentEffect);

	if (!game.ai.moveTowards(parent, target.getLocation(), distanceAway)) {
		game.addMessage("red", "Charge attack by " + parent.getName() + " was interrupted.");
		parent.removeEffect(parentEffect);
		return;
	}
	
	game.standardAttack(parent, target);
	
	parent.removeEffect(parentEffect);
}