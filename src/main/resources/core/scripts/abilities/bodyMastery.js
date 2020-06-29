function onActivate(game, slot) {
	var ability = slot.getAbility();
	var parent = slot.getParent();

	var duration = parseInt(2 + parent.roles.getLevel("Monk") / 4);
	
	// cast the ability
	slot.setActiveRoundsLeft(duration);
	slot.activate();
   
	var effect = slot.createEffect();
	effect.setDuration(duration);
	effect.addPositiveIcon("items/enchant_physical_small");
	effect.setTitle(ability.getName());
	effect.getBonuses().addBonus('PhysicalResistance', 'Stackable', 25);
	effect.getBonuses().addBonus('MentalResistance', 'Stackable', 25);
	effect.getBonuses().addBonus('ReflexResistance', 'Stackable', 25);
	effect.getBonuses().add("ImmobilizationImmunity");
	
	if (parent.abilities.has("ChiMastery")) {
		effect.getBonuses().addBonus('SpellResistance', parent.roles.getLevel("Monk") * 4);
		effect.addPositiveIcon("items/enchant_spellResistance_small");
	}
	
	var g1 = game.getBaseParticleGenerator("sparkle");
	g1.setDurationInfinite();
	g1.setRotationSpeedDistribution(game.getUniformDistribution(100.0, 200.0));
	g1.setPosition(parent.getLocation());
	g1.setRedDistribution(game.getUniformDistribution(0.4, 0.6));
	g1.setGreenDistribution(game.getUniformDistribution(0.2, 0.3));
	g1.setBlueDistribution(game.getUniformDistribution(0.5, 0.8));
	effect.addAnimation(g1);
	
	parent.applyEffect(effect);
}