function onActivate(game, slot) {
	var effect = slot.createEffect();
	effect.setTitle(slot.getAbility().getName());
	effect.addPositiveIcon("items/enchant_invisibility_small");
	effect.setRemoveOnDeactivate(true);
	effect.getBonuses().add('Hidden');
	
	var g1 = game.getBaseParticleGenerator("sparkle");
	g1.setDurationInfinite();
	g1.setRotationSpeedDistribution(game.getUniformDistribution(100.0, 200.0));
	g1.setPosition(slot.getParent().getLocation());
	g1.setBlueDistribution(game.getFixedDistribution(0.0));
	g1.setRedDistribution(game.getFixedDistribution(0.0));
	g1.setGreenDistribution(game.getFixedDistribution(0.0));
	g1.setAlphaSpeedDistribution(game.getUniformDistribution(-0.2, -0.5));
	effect.addAnimation(g1);
	
	slot.getParent().applyEffect(effect);
	slot.activate();
	
	if (slot.getParent().abilities.has("HideInPlainSight"))
		game.performSearchChecksForCreature(slot.getParent(), 0);
	else
		game.performSearchChecksForCreature(slot.getParent(), game.ruleset().getValue("HideInPlainSightPenalty"));
}

function onDeactivate(game, slot) {
	slot.deactivate();
}
