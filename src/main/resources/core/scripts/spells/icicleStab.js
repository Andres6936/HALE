function onActivate(game, slot) {
	var targeter = game.createLineTargeter(slot);
	
	targeter.setOrigin(slot.getParent().getLocation());
	targeter.setForceLineLength(8);
	targeter.activate();
}

function onTargetSelect(game, targeter) {
	targeter.getSlot().activate();

	var spell = targeter.getSlot().getAbility();
	var parent = targeter.getParent();
	var casterLevel = parent.stats.getCasterLevel();

	// check for spell failure
	if (!spell.checkSpellFailure(parent)) return;
	
	var g1 = game.getBaseParticleGenerator("lance");
	g1.setVelocityDurationRotationBasedOnSpeed(parent.getLocation().toPoint(), targeter.getEndPoint(), 800.0);
	
	var g2 = game.getBaseParticleGenerator("bolt");
	g2.setVelocityDurationBasedOnSpeed(parent.getLocation().toPoint(), targeter.getEndPoint(), 800.0);
	g1.addSubGenerator(g2, 0.0);
	
	var targets = targeter.getAffectedCreatures();
	for (var i = 0; i < targets.size(); i++) {
		var damage = game.dice().randInt(1, 10) + casterLevel;
		
		var delay = targets.get(i).getLocation().getScreenDistance(parent.getLocation()) / g1.getSpeed();
		
		var callback = spell.createDelayedCallback("applyDamage");
		callback.setDelay(delay);
		callback.addArguments([parent, targets.get(i), damage, spell]);
		
		callback.start();
	}
	
	game.runParticleGeneratorNoWait(g1);
	game.lockInterface(g1.getTimeLeft());
}

function applyDamage(game, parent, target, damage, spell) {
   spell.applyDamage(parent, target, damage, "Piercing");
}