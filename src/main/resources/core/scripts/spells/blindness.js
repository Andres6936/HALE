function onActivate(game, slot) {
	var creatures = game.ai.getVisibleCreaturesWithinRange(slot.getParent(), "Hostile", 20);

	var targeter = game.createListTargeter(slot);
	targeter.addAllowedCreatures(creatures);
	targeter.activate();
}

function onTargetSelect(game, targeter) {
	var spell = targeter.getSlot().getAbility();
	var parent = targeter.getParent();
	var target = targeter.getSelectedCreature();
	var casterLevel = parent.stats.getCasterLevel();
	
	var duration = game.dice().rand(2, 4);
	
	targeter.getSlot().setActiveRoundsLeft(duration);
	targeter.getSlot().activate();
	
	if (!spell.checkSpellFailure(parent, target)) return;
	
	if ( target.stats.getPhysicalResistanceCheck(spell.getCheckDifficulty(parent)) )
		return;
	
	var effect = targeter.getSlot().createEffect();
	effect.setDuration(duration);
	effect.getBonuses().add('Blind');
	effect.setTitle(spell.getName());
	effect.addNegativeIcon("items/enchant_invisibility_small");
	
	var g1 = game.getBaseParticleGenerator("fog");
	g1.setDurationInfinite();
	g1.setPosition(target.getLocation());
	g1.setCircleBounds(0.0, 12.0);
	effect.addAnimation(g1);
	
	target.applyEffect(effect);
}
