function onActivate(game, slot) {
	var targeter = game.createListTargeter(slot);
	targeter.addAllowedCreature(slot.getParent());
	targeter.activate();
}

function onTargetSelect(game, targeter) {
	var spell = targeter.getSlot().getAbility();
	var parent = targeter.getParent();
	var target = targeter.getSelectedCreature();
	var casterLevel = parent.stats.getCasterLevel();
	
	var lvls = parent.roles.getLevel("War Wizard");
	
	var duration = parseInt(2 + lvls / 3);
	
	targeter.getSlot().setActiveRoundsLeft(duration);
	targeter.getSlot().activate();
	
	if (!spell.checkSpellFailure(parent, target)) return;
	
	var effect = targeter.getSlot().createEffect();
	effect.setDuration(duration);
	effect.addPositiveIcon("items/enchant_attack_small");
	effect.addPositiveIcon("items/enchant_damage_small");
	effect.addPositiveIcon("items/enchant_strength_small");
	effect.setTitle(spell.getName());
	effect.getBonuses().addBonus('Str', 5 + parseInt(lvls / 2));
	effect.getBonuses().addBonus('Attack', lvls * 4);
	effect.getBonuses().addBonus('Damage', lvls * 8);
	effect.getBonuses().add("ImmobilizationImmunity");
	
	effect.getBonuses().addPenalty("SpellFailure", -30);
	
	if (parent.abilities.has("Destroyer")) {
		effect.getBonuses().addStandaloneDamageBonus("Force", lvls, 2 * lvls);
	}
	
	var generator = game.createParticleGenerator("Line", "Continuous", "particles/plus", 2.0);
	generator.setLineStart(-10.0, 0.0);
	generator.setLineEnd(10.0, 0.0);
	generator.setDurationInfinite();
	
	var position = parent.getLocation().getCenteredScreenPoint();
	generator.setPosition(position.x, position.y - 20.0);
	generator.setStopParticlesAtOpaque(false);
	generator.setDrawParticlesInOpaque(true);
	generator.setVelocityDistribution(game.getFixedAngleDistribution(10.0, 15.0, -3.14159 / 2));
	generator.setDurationDistribution(game.getUniformDistribution(1.5, 2.2));
	generator.setAlphaSpeedDistribution(game.getFixedDistribution(-0.5));
	generator.setAlphaDistribution(game.getFixedDistribution(1.0));
	generator.setRedDistribution(game.getFixedDistribution(1.0));
	generator.setGreenDistribution(game.getUniformDistribution(0.0, 0.2));
	generator.setBlueDistribution(game.getFixedDistribution(0.0));
	
	effect.addAnimation(generator);
	
	target.applyEffect(effect);
}
