function canActivate(game, parent) {
	if (!parent.timer.canAttack()) return false;
	
	var weapon = parent.getMainHandWeapon();
	
	return !weapon.isMelee();
}

function onActivate(game, slot) {
	var targeter = game.createLineTargeter(slot);
	
	targeter.setOrigin(slot.getParent().getLocation());
	targeter.setForceLineLength(10);
	targeter.activate();
}

function onTargetSelect(game, targeter) {
	targeter.getSlot().activate();

	var parent = targeter.getParent();
	var ability = targeter.getSlot().getAbility();
	
	parent.timer.performAttack();
	
	var g1 = game.getBaseParticleGenerator("ray");
	g1.setRedDistribution(game.getFixedDistribution(0.4));
	g1.setGreenDistribution(game.getFixedDistribution(0.28));
	g1.setBlueDistribution(game.getFixedDistribution(0.22));
	g1.setAlphaSpeedDistribution(game.getFixedDistribution(-1.0));
	g1.setVelocityDurationRotationBasedOnSpeed(parent.getLocation().toPoint(), targeter.getEndPoint(), 600.0);
	
	var targets = targeter.getAffectedCreatures();
	for (var i = 0; i < targets.size(); i++) {
		var target = targets.get(i);
	
		var delay = targets.get(i).getLocation().getScreenDistance(parent.getLocation()) / g1.getSpeed();
		
		var callback = ability.createDelayedCallback("applyDamage");
		callback.setDelay(delay);
		callback.addArguments([parent, target]);
		callback.start();
	}
	
	game.runParticleGeneratorNoWait(g1);
	game.lockInterface(g1.getTimeLeft());
}

function applyDamage(game, parent, target) {
	game.singleAttack(parent, target);
}
