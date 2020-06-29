function onActivate(game, slot) {
	var creatures = game.ai.getLiveVisibleCreatures(slot.getParent(), "Hostile");
	
	var targeter = game.createListTargeter(slot);
	targeter.addAllowedCreatures(creatures);
	targeter.activate();
}

function onTargetSelect(game, targeter) {
	var spell = targeter.getSlot().getAbility();
	var parent = targeter.getParent();
	var target = targeter.getSelectedCreature();
	var casterLevel = parent.stats.getCasterLevel();
	
	var lvls = targeter.getParent().roles.getLevel("Avenger");
	
	var duration = 3 + parseInt(lvls / 3);
	
	targeter.getSlot().setActiveRoundsLeft(duration);
	targeter.getSlot().activate();
	
	if (!spell.checkSpellFailure(parent, target)) return;
	
	var effect = targeter.getSlot().createEffect();
	effect.setDuration(duration);
	effect.setTitle(spell.getName());
	
	effect.getBonuses().addPenalty("SpellCooldown", -8);
	effect.getBonuses().addPenalty("SpellDamage", -20 - 3 * lvls);
	effect.getBonuses().addPenalty("SpellHealing", -20 - 3 * lvls);
	effect.getBonuses().addPenalty("SpellDuration", -20 - 3 * lvls);
	effect.addNegativeIcon("items/enchant_spellCooldown_small");
	effect.addNegativeIcon("items/enchant_spellDamage_small");
	effect.addNegativeIcon("items/enchant_spellHealing_small");
	
	var g1 = game.createParticleGenerator("Point", "Continuous", "particles/halosmall", 2.0);
	g1.setDurationDistribution(game.getFixedDistribution(1.0));
	g1.setRedDistribution(game.getUniformDistribution(0.0, 1.0));
	g1.setGreenDistribution(game.getUniformDistribution(0.0, 1.0));
	g1.setBlueDistribution(game.getUniformDistribution(0.7, 1.0));
	g1.setAlphaDistribution(game.getFixedDistribution(0.6));
	g1.setAlphaSpeedDistribution(game.getFixedDistribution(-0.4));
	g1.setPosition(target.getLocation());
	g1.setDurationInfinite();
	effect.addAnimation(g1);
	
	target.applyEffect(effect);
}