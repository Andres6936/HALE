function onActivate(game, slot) {
	var creatures = game.ai.getTouchableCreatures(slot.getParent(), "Friendly");
	
	var targeter = game.createListTargeter(slot);
	targeter.addAllowedCreatures(creatures);
	targeter.activate();
}

function onTargetSelect(game, targeter, type) {
	var spell = targeter.getSlot().getAbility();
	var parent = targeter.getParent();
	var target = targeter.getSelectedCreature();
	var casterLevel = parent.stats.getCasterLevel();
	
	// cast the spell
	targeter.getSlot().activate();
   
	// check for spell failure
	if (!spell.checkSpellFailure(parent, target)) return;
	
	target.stats.removeEffectPenaltiesOfType("Immobilized");
	target.stats.removeEffectPenaltiesOfType("Helpless");
	target.stats.removeEffectPenaltiesOfType("ActionPoint");
	
	var anim = game.getBaseAnimation("crossFlash");
	anim.setRed(0.0);
	anim.setGreen(1.0);
	anim.setBlue(0.3);
	
	var position = target.getLocation().getCenteredScreenPoint();
	anim.setPosition(position.x, position.y - 10);
	
	game.runAnimationNoWait(anim);
	game.lockInterface(anim.getSecondsRemaining());
}