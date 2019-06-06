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
	
	var duration = parseInt(3 + casterLevel / 3);
	
	// cast the spell
	targeter.getSlot().setActiveRoundsLeft(duration);
	targeter.getSlot().activate();
   
	// check for spell failure
	if (!spell.checkSpellFailure(parent, target)) return;
	
	var effect = targeter.getSlot().createEffect("effects/ward");
	effect.addPositiveIcon("items/enchant_spellResistance_small");
	effect.setDuration(duration);
	effect.setTitle(spell.getName());
	
	var g1 = game.getBaseParticleGenerator("shield");
	g1.setPosition(target.getLocation());
	g1.setDurationInfinite();
	effect.addAnimation(g1);
	
	target.applyEffect(effect);
}
