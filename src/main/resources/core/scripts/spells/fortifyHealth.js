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

	var duration = parseInt(game.dice().randInt(5, 10));

	targeter.getSlot().setActiveRoundsLeft(duration);
	targeter.getSlot().activate();
	
	if (!spell.checkSpellFailure(parent, target)) return;
	
	// compute the amount of healing
	var healing = 5 + game.dice().randInt(casterLevel, casterLevel * 3);
	
	var effect = targeter.getSlot().createEffect();
	effect.setTitle(spell.getName());
	effect.setDuration(duration);
	effect.getBonuses().addBonus("TemporaryHP", healing);
	
	target.applyEffect(effect);
	
	var anim = game.getBaseAnimation("crossFlash");
	anim.setRed(0.0);
	anim.setGreen(1.0);
	anim.setBlue(0.0);
	
	var position = target.getLocation().getCenteredScreenPoint();
	anim.setPosition(position.x, position.y - 10);
	
	game.runAnimationNoWait(anim);
	game.lockInterface(anim.getSecondsRemaining());
}
