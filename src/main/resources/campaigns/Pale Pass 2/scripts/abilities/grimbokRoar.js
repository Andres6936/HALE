function onActivate(game, slot) {
   var targeter = game.createCircleTargeter(slot);
   targeter.setRadius(4);
   targeter.setRelationshipCriterion("Hostile");
   targeter.addAllowedPoint(slot.getParent().getLocation());
   targeter.activate();
}

function onTargetSelect(game, targeter) {
	var spell = targeter.getSlot().getAbility();
	var parent = targeter.getParent();
	var casterLevel = parent.stats.getCasterLevel();
	
	targeter.getSlot().activate();
	
	var explosionCenter = targeter.getMouseGridPosition();
	
	var g1 = game.getBaseParticleGenerator("explosion");
	g1.setPosition(explosionCenter);
	g1.setAlphaSpeedDistribution(game.getFixedDistribution(-1.3));
	g1.setVelocityDistribution(game.getEquallySpacedAngleDistribution(300.0, 400.0, 5.0, 1500.0, 10.0));
	g1.setBlueSpeedDistribution(game.getUniformDistributionWithBase(game.getSpeedDistributionBase(), -0.01, 0.0, 0.05));
	game.runParticleGeneratorNoWait(g1);
	game.lockInterface(g1.getTimeLeft());
	
	var targets = targeter.getAffectedCreatures();
	for (var i = 0; i < targets.size(); i++) {
		var target = targets.get(i);
		
		var delay = target.getLocation().getScreenDistance(explosionCenter) / 400.0;
		
		var callback = spell.createDelayedCallback("applyEffect");
		callback.setDelay(delay);
		callback.addArguments([parent, target, spell, targeter]);
		callback.start();
	}
}

function applyEffect(game, parent, target, spell, targeter) {
	if ( !target.stats.getPhysicalResistanceCheck(90) ) {
		var duration = game.dice().rand(3, 5);
		
		var effect = targeter.getSlot().createEffect();
		effect.setDuration(duration);
		effect.setTitle(spell.getName());
		
		effect.getBonuses().addPenalty("Attack", "Morale", -10);
		effect.getBonuses().addSkillPenalty("Search", -20);
		effect.getBonuses().addSkillPenalty("Speech", -50);
		effect.getBonuses().addPenalty("VerbalSpellFailure", "Morale", -30);
		
		var g1 = game.getBaseParticleGenerator("sparkle");
		g1.setDurationInfinite();
		g1.setRotationSpeedDistribution(game.getUniformDistribution(100.0, 200.0));
		g1.setPosition(target.getLocation());
		g1.setBlueDistribution(game.getFixedDistribution(0.0));
		g1.setGreenDistribution(game.getFixedDistribution(0.0));
		effect.addAnimation(g1);
			
		target.applyEffect(effect);
	}
}