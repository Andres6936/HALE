function onActivate(game, slot) {
	var creatures = game.ai.getTouchableCreatures(slot.getParent(), "Friendly");
	
	for (var i = 0; i < creatures.size(); i++) {
		if (!creatures.get(i).isDead()) {
			creatures.remove(i);
			i--;
		}
	}
	
	var targeter = game.createListTargeter(slot);
	targeter.addAllowedCreatures(creatures);
	targeter.activate();
}

function onTargetSelect(game, targeter) {
	var spell = targeter.getSlot().getAbility();
	var parent = targeter.getParent();
	var target = targeter.getSelectedCreature();
	var casterLevel = parent.stats.getCasterLevel();
	
	// cast the spell
	targeter.getSlot().activate();
   
	// check for spell failure
	if (!spell.checkSpellFailure(parent, target)) return;
   
	var anim = game.getBaseAnimation("crossFlash");
	anim.setRed(1.0);
	anim.setGreen(1.0);
	anim.setBlue(1.0);
	
	var position = target.getLocation().getCenteredScreenPoint();
	anim.setPosition(position.x, position.y - 10);
	
	game.runAnimationNoWait(anim);
	game.lockInterface(anim.getSecondsRemaining());
   
	target.raiseFromDead();
}
