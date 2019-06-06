function onActivate(game, slot) {
   var targeter = game.createCircleTargeter(slot);
   targeter.setRadius(4);
   targeter.setMaxRange(10);
   targeter.activate();
}

function onTargetSelect(game, targeter) {
	var spell = targeter.getSlot().getAbility();
	var parent = targeter.getParent();
	var casterLevel = parent.stats.getCasterLevel();

	var duration = parseInt(3 + casterLevel / 4);
	
	targeter.getSlot().setActiveRoundsLeft(duration);
	targeter.getSlot().activate();
	
	if (!spell.checkSpellFailure(parent)) return;
	
	var effect = targeter.getSlot().createEffect("effects/tanglefoot");
	effect.setTitle(spell.getName());
	effect.setDuration(duration);
	effect.getBonuses().addPenalty('Movement', -40 - 3 * casterLevel);
	
	for (var i = 0; i < targeter.getAffectedPoints().size(); i++) {
		var point = targeter.getAffectedPoints().get(i);
	   
		var g1 = game.getBaseParticleGenerator("fog");
		g1.setDrawingMode("BelowEntities");
		g1.setBlueDistribution(game.getFixedDistribution(0.0));
		g1.setRedDistribution(game.getFixedDistribution(0.2));
		g1.setDurationInfinite();
		g1.setPosition(point);
		effect.addAnimation(g1);
	}
	
	game.currentArea().applyEffect(effect, targeter.getAffectedPoints());
}