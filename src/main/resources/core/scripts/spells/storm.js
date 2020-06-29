function onActivate(game, slot) {
	var targeter = game.createCircleTargeter(slot);
	targeter.setRadius(3);
	targeter.activate();
}

function onTargetSelect(game, targeter) {
	var duration = game.dice().randInt(3, 6);
	
	targeter.getSlot().setActiveRoundsLeft(duration);
	targeter.getSlot().activate();
	
	var spell = targeter.getSlot().getAbility();
	var parent = targeter.getParent();
	var casterLevel = parent.stats.getCasterLevel();
	
	if (!spell.checkSpellFailure(parent)) return;
	
	var effect = targeter.getSlot().createEffect("effects/storm");
	effect.setDuration(duration);
	
	for (var i = 0; i < targeter.getAffectedPoints().size(); i++) {
		var point = targeter.getAffectedPoints().get(i);
		
		var g1 = game.getBaseParticleGenerator("sparkle");
		g1.setRectBounds(-36.0, 36.0, -36.0, 36.0);
		g1.setRotationSpeedDistribution(game.getUniformDistribution(100.0, 200.0));
		g1.setDurationInfinite();
		g1.setPosition(point);
		effect.addAnimation(g1);
	}
	
	game.currentArea().applyEffect(effect, targeter.getAffectedPoints());
}

function applyLightningDamage(game, parent, target, damage, spell) {
	spell.applyDamage(parent, target, damage, "Electrical");
}
