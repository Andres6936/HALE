function onActivate(game, slot) {
	var targeter = game.createConeTargeter(slot);
	
	targeter.setOrigin(slot.getParent().getLocation());
	targeter.setConeAngle(30);
	targeter.setConeRadius(8);
	targeter.setHasVisibilityCriterion(false);
	
	targeter.activate();
}

function onTargetSelect(game, targeter) {
	targeter.getSlot().activate();

	var spell = targeter.getSlot().getAbility();
	var parent = targeter.getParent();
	var casterLevel = parent.stats.getCasterLevel();

	// check for spell failure
	if (!spell.checkSpellFailure(parent)) return;
	
	var g1 = game.getBaseParticleGenerator("spray");
	g1.setPosition(parent.getLocation());
	g1.setRedDistribution(game.getUniformDistribution(0.438 - 0.05, 0.438 + 0.05));
    g1.setGreenDistribution(game.getUniformDistribution(0.379 - 0.05, 0.379 + 0.05));
    g1.setBlueDistribution(game.getFixedDistribution(0.0));
	g1.setDuration(0.10);
	g1.setDurationDistribution(game.getFixedDistribution(4.0));
	g1.setAlphaSpeedDistribution(game.getFixedDistribution(-0.5));
	
	var angle = targeter.getCenterAngle();
	
	g1.setVelocityDistribution(game.getUniformArcDistribution(400.0, 450.0, angle - 0.25, angle + 0.25));
	
	var targets = targeter.getAffectedCreatures();
	for (var i = 0; i < targets.size(); i++) {
		var delay = targets.get(i).getLocation().getScreenDistance(parent.getLocation()) / 400.0;
		
		var callback = spell.createDelayedCallback("applyEffect");
		callback.setDelay(delay);
		callback.addArguments([parent, targets.get(i), targeter.getSlot()]);
		
		callback.start();
	}
	
	game.runParticleGeneratorNoWait(g1);
	game.lockInterface(g1.getTimeLeft());
	
	game.shakeScreen();
}

function applyEffect(game, parent, target, slot) {
	if ( target.stats.has("ImmobilizationImmunity")) {
		game.addMessage("blue", target.getName() + " is immune.");
		return;
	}
	
	if ( target.stats.getPhysicalResistanceCheck(slot.getAbility().getCheckDifficulty(parent)) )
		return;
		
	var effect = slot.createEffect();
	effect.setDuration(1);
	effect.setTitle(slot.getAbility().getName());
	effect.getBonuses().add("Immobilized");
	effect.getBonuses().add("Helpless");
	
	var g1 = game.getBaseParticleGenerator("sparkle");
	g1.setDurationInfinite();
	g1.setRotationSpeedDistribution(game.getUniformDistribution(100.0, 200.0));
	g1.setPosition(target.getLocation());
	g1.setBlueDistribution(game.getFixedDistribution(0.0));
	g1.setGreenDistribution(game.getFixedDistribution(0.0));
	effect.addAnimation(g1);
	
	target.applyEffect(effect);
}
