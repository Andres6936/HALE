function onActivate(game, slot) {
   var targeter = game.createCircleTargeter(slot);
   targeter.setRadius(4);
   targeter.setMaxRange(10);
   targeter.activate();
}

function onTargetSelect(game, targeter) {
    var duration = game.dice().randInt(5, 10);
	
	targeter.getSlot().setActiveRoundsLeft(duration);
	targeter.getSlot().activate();
	
	var spell = targeter.getSlot().getAbility();
	var parent = targeter.getParent();
	var casterLevel = parent.stats.getCasterLevel();
	
	if (!spell.checkSpellFailure(parent)) return;
	
	var effect = targeter.getSlot().createEffect("effects/fog");
	
	effect.setTitle(spell.getName());
	effect.setDuration(duration);
	effect.getBonuses().addBonus('Concealment', 10);
	
	for (var i = 0; i < targeter.getAffectedPoints().size(); i++) {
	   var point = targeter.getAffectedPoints().get(i);
	   
	   var g1 = game.getBaseParticleGenerator("fog");
	   g1.setDurationInfinite();
	   g1.setPosition(point);
	   effect.addAnimation(g1);
	}
	
	game.currentArea().applyEffect(effect, targeter.getAffectedPoints());
}
