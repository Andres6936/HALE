function onActivate(game, slot) {
	if (slot.getParent().abilities.has("MassParalyze")) {
		var targeter = game.createCircleTargeter(slot);
		targeter.setRadius(4);
		targeter.setRelationshipCriterion("Hostile");
		targeter.addAllowedPoint(slot.getParent().getLocation());
		targeter.activate();
	} else {
		var creatures = game.ai.getVisibleCreaturesWithinRange(slot.getParent(), "Hostile", 20);
	
		var targeter = game.createListTargeter(slot);
		targeter.addAllowedCreatures(creatures);
		targeter.activate();
	}
}

function onTargetSelect(game, targeter) {
	var spell = targeter.getSlot().getAbility();
	var parent = targeter.getParent();
	
	var casterLevel = parent.stats.getCasterLevel();
	
	var duration = game.dice().randInt(3, 6);
	
	targeter.getSlot().setActiveRoundsLeft(duration);
	targeter.getSlot().activate();
	
	if (parent.abilities.has("MassParalyze")) {
		if (!spell.checkSpellFailure(parent)) return;
	
		var targets = targeter.getAffectedCreatures();
		
		for (var i = 0; i < targets.size(); i++) {
			var target = targets.get(i);
			
			applyEffect(game, targeter, target, duration);
		}
	} else {
		var target = targeter.getSelectedCreature();
		
		if (!spell.checkSpellFailure(parent, target)) return;
		
		applyEffect(game, targeter, target, duration);
	}
}

function applyEffect(game, targeter, target, duration) {
	var spell = targeter.getSlot().getAbility();
	var parent = targeter.getParent();
	var casterLevel = parent.stats.getCasterLevel();

	if ( target.stats.getMentalResistanceCheck(spell.getCheckDifficulty(parent)) )
		return;
	
	var effect = targeter.getSlot().createEffect();
	effect.setDuration(duration);
	effect.setTitle(spell.getName());
	
	if ( target.stats.has("ImmobilizationImmunity")) {
		game.addMessage("blue", target.getName() + " is immune.");
		return;
	}
	
	if (parent.abilities.has("Paralyze")) {
		effect.getBonuses().add("Immobilized");
		effect.getBonuses().add("Helpless");
		effect.addNegativeIcon("items/enchant_death_small");
	
		var position = target.getLocation().getCenteredScreenPoint();
		
		var g1 = game.getBaseParticleGenerator("paralysis");
		var g2 = game.getBaseParticleGenerator("paralysis");
	
		g1.setPosition(position.x, position.y - 10.0);
		g2.setPosition(position.x, position.y + 10.0);
	
		g2.setLineStart(-18.0, 0.0);
		g2.setLineEnd(18.0, 0.0);
		g2.setRedDistribution(game.getFixedDistribution(0.6));

		effect.addAnimation(g1);
		effect.addAnimation(g2);
	} else {
		effect.getBonuses().addPenalty('ActionPoint', 'Morale', -25 - casterLevel);
		effect.addNegativeIcon("items/enchant_actionPoints_small");
		
		if (parent.abilities.has("Sleep")) {
			var sleepEffect = targeter.getSlot().createEffect("effects/sleep");
			sleepEffect.setDuration(duration);
			sleepEffect.setTitle("Sleep");
			target.applyEffect(sleepEffect);
		}
		
		var g1 = game.getBaseParticleGenerator("sparkle");
		g1.setDurationInfinite();
		g1.setRotationSpeedDistribution(game.getUniformDistribution(100.0, 200.0));
		g1.setPosition(target.getLocation());
		g1.setBlueDistribution(game.getFixedDistribution(0.0));
		g1.setGreenDistribution(game.getFixedDistribution(0.0));
		effect.addAnimation(g1);
	}
	   
	target.applyEffect(effect);
}