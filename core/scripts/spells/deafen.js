function onActivate(game, slot) {
   var targeter = game.createCircleTargeter(slot);
   targeter.setRadius(3);
   targeter.setMaxRange(10);
   targeter.activate();
}

function onTargetSelect(game, targeter) {
	var spell = targeter.getSlot().getAbility();
	var parent = targeter.getParent();
	var casterLevel = parent.stats.getCasterLevel();
	
	targeter.getSlot().setActiveRoundsLeft(5);
	targeter.getSlot().activate();
	
	if (!spell.checkSpellFailure(parent)) return;
	
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
		
		var callback = spell.createDelayedCallback("applyDeafen");
		callback.setDelay(delay);
		callback.addArguments([parent, target, spell, targeter]);
		callback.start();
	}
	
	if (parent.abilities.has("Silence")) {
		var callback = spell.createDelayedCallback("applySilence");
		callback.setDelay(g1.getTimeLeft() - 1.0);
		callback.addArguments([spell, targeter]);
		callback.start();
	}
}

function applySilence(game, spell, targeter) {
	var effect = targeter.getSlot().createEffect();
	effect.setTitle(spell.getName());
	effect.setDuration(3);
	effect.getBonuses().add('Silence');
	effect.addNegativeIcon("items/enchant_spellFailure_small");
	
	for (var i = 0; i < targeter.getAffectedPoints().size(); i++) {
		var point = targeter.getAffectedPoints().get(i);
	   
		var g1 = game.getBaseParticleGenerator("fog");
		g1.setAlphaDistribution(game.getFixedDistribution(0.2));
		g1.setGreenDistribution(game.getFixedDistribution(0.0));
		g1.setBlueDistribution(game.getFixedDistribution(0.9));
		g1.setDurationInfinite();
		g1.setPosition(point);
		effect.addAnimation(g1);
	}
	
	game.currentArea().applyEffect(effect, targeter.getAffectedPoints());
}

function applyDeafen(game, parent, target, spell, targeter) {
	if ( !target.stats.getPhysicalResistanceCheck(spell.getCheckDifficulty(parent)) ) {
		var duration = game.dice().randInt(3, 5);
		
		var effect = targeter.getSlot().createEffect();
		effect.setDuration(duration);
		effect.setTitle(spell.getName());
		
		effect.getBonuses().addPenalty("Attack", "Morale", -10);
		effect.getBonuses().addSkillPenalty("Search", -20);
		effect.getBonuses().addSkillPenalty("Speech", -50);
		effect.getBonuses().addPenalty("VerbalSpellFailure", "Morale", -30);
		
		effect.addNegativeIcon("items/enchant_spellFailure_small");
		effect.addNegativeIcon("items/enchant_attack_small");
		
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