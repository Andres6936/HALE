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
	
	var duration = game.dice().randInt(5, 10);
	
	// cast the spell
	targeter.getSlot().setActiveRoundsLeft(duration);
	targeter.getSlot().activate();
   
	// check for spell failure
	if (!spell.checkSpellFailure(parent, target)) return;
	
	var effect = targeter.getSlot().createEffect();
	effect.setDuration(duration);
	effect.addPositiveIcon("items/enchant_intelligence_small");
	effect.setTitle(spell.getName());
	effect.getBonuses().addBonus('Int', 1 + parseInt(casterLevel / 4));
	effect.getBonuses().addBonus('Wis', 1 + parseInt(casterLevel / 4));
	effect.getBonuses().addBonus('Cha', 1 + parseInt(casterLevel / 4));
	
	if (parent.abilities.has("AnimalPower")) {
		effect.addPositiveIcon("items/enchant_strength_small");
		effect.getBonuses().addBonus('Str', 1 + parseInt(casterLevel / 4));
		effect.getBonuses().addBonus('Con', 1 + parseInt(casterLevel / 4));
		effect.getBonuses().addBonus('Dex', 1 + parseInt(casterLevel / 4));
	}
	
	target.applyEffect(effect);
	
	var anim = game.getBaseAnimation("arrowFlash");
	anim.setRed(0.0);
	anim.setGreen(1.0);
	anim.setBlue(0.3);
	
	var position = target.getLocation().getCenteredScreenPoint();
	anim.setPosition(position.x, position.y - 10);
	
	game.runAnimationNoWait(anim);
	game.lockInterface(anim.getSecondsRemaining());
}