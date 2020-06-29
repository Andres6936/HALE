function onActivate(game, slot) {
	var targeter = game.createListTargeter(slot);
	targeter.addAllowedCreature(slot.getParent());
	targeter.activate();
}

function onTargetSelect(game, targeter, type) {
	var spell = targeter.getSlot().getAbility();
	var parent = targeter.getParent();
	var target = targeter.getSelectedCreature();
	var casterLevel = parent.stats.getCasterLevel();
	
	var duration = game.dice().randInt(5, 10);
	
	targeter.getSlot().setActiveRoundsLeft(duration);
	targeter.getSlot().activate();
	
	if (!spell.checkSpellFailure(parent, target)) return;
	
	var effect = targeter.getSlot().createEffect();
	effect.setTitle(spell.getName());
	effect.addPositiveIcon("items/enchant_attack_small");
	effect.addPositiveIcon("items/enchant_armor_small");
	effect.addPositiveIcon("items/enchant_physical_small");
	effect.setDuration(duration);
	
	effect.getBonuses().addBonus('Str', parseInt(4 + casterLevel / 4));
	effect.getBonuses().addBonus('Con', parseInt(4 + casterLevel / 4));
	
	effect.getBonuses().addBonus('Attack', 20 + casterLevel);
	effect.getBonuses().addBonus('ArmorClass', 'NaturalArmor', 20 + casterLevel);
	effect.getBonuses().addDamageReduction("Physical", parseInt(10 + casterLevel / 4));
	effect.getBonuses().addDamageImmunity("Electrical", 50);
	effect.getBonuses().addDamageImmunity("Cold", 50);
	effect.getBonuses().addDamageImmunity("Acid", 50);
	effect.getBonuses().add("UndispellableImmobilized");
	
	var g1 = game.getBaseParticleGenerator("fog");
	g1.setDrawingMode("BelowEntities");
	g1.setDurationInfinite();
	g1.setPosition(target.getLocation());
	g1.setRedDistribution(game.getFixedDistribution(0.0));
	g1.setBlueDistribution(game.getFixedDistribution(0.0));
	
	effect.addAnimation(g1);
	
	target.applyEffect(effect);
}