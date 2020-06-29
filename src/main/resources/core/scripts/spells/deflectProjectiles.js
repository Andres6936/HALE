function onActivate(game, slot) {
	var creatures = game.ai.getTouchableCreatures(slot.getParent(), "Friendly");
	
	var targeter = game.createListTargeter(slot);
	targeter.addAllowedCreatures(creatures);
	targeter.activate();
}

function onTargetSelect(game, targeter) {
	var spell = targeter.getSlot().getAbility();
	var parent = targeter.getParent();
	var target = targeter.getSelectedCreature();
	var casterLevel = parent.stats.getCasterLevel();
	
	var duration = 3 + parseInt(casterLevel / 5);
	
	// cast the spell
	targeter.getSlot().setActiveRoundsLeft(duration);
	targeter.getSlot().activate();
	
	// check for spell failure
	if (!spell.checkSpellFailure(parent, target)) return;
	
	var effect = targeter.getSlot().createEffect("effects/deflectProjectiles");
	effect.addPositiveIcon("items/enchant_armor_small");
	effect.setTitle(spell.getName());
	effect.setDuration(duration);
	
	var g1 = game.getBaseParticleGenerator("sparkle");
	g1.setDurationInfinite();
	g1.setRotationSpeedDistribution(game.getUniformDistribution(100.0, 200.0));
	g1.setPosition(target.getLocation());
	g1.setGreenDistribution(game.getFixedDistribution(0.0));
	g1.setBlueDistribution(game.getFixedDistribution(0.0));
	g1.setRedDistribution(game.getFixedDistribution(0.0));
	g1.setBlueSpeedDistribution(game.getUniformDistribution(0.2, 1.0));
	g1.setRedSpeedDistribution(game.getUniformDistribution(0.2, 1.0));
	g1.setGreenSpeedDistribution(game.getUniformDistribution(0.2, 1.0));
	effect.addAnimation(g1);
	
	target.applyEffect(effect);
}