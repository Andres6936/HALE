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
	
	var duration = parseInt(3 + casterLevel / 4);
	
	// cast the spell
	targeter.getSlot().setActiveRoundsLeft(duration);
	targeter.getSlot().activate();
	
	if (!spell.checkSpellFailure(parent, target)) return;
	
	var bonus = parseInt(15 + casterLevel / 3);
	
	var effect = targeter.getSlot().createEffect();
	effect.setDuration(duration);
	effect.setTitle(spell.getName());
	effect.addPositiveIcon("items/enchant_actionPoints_small");
	effect.getBonuses().addBonus('ActionPoint', bonus);
	
	var generator = game.createParticleGenerator("Rect", "Continuous", "particles/arrowsmall", 4.0);
	generator.setVelocityDistribution(game.getFixedAngleDistribution(25.0, 50.0, -3.14159 / 2.0));
	generator.setRectBounds(-17.0, 17.0, -5.0, 10.0);
	generator.setDurationInfinite();
	generator.setPosition(target.getLocation());
	generator.setRedDistribution(game.getFixedDistribution(0.85));
	generator.setGreenDistribution(game.getFixedDistribution(0.7));
	generator.setAlphaDistribution(game.getUniformDistribution(0.8, 1.0));
	generator.setAlphaSpeedDistribution(game.getFixedDistribution(-0.9));
	
	effect.addAnimation(generator);
	
	target.applyEffect(effect);
}
