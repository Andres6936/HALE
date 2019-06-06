function onActivate(game, slot) {
	if (slot.getParent().abilities.has("MassRegeneration")) {
		var targeter = game.createCircleTargeter(slot);
		targeter.setRadius(4);
		targeter.setRelationshipCriterion("Friendly");
		targeter.addAllowedPoint(slot.getParent().getLocation());
		targeter.activate();
	} else {
		var creatures = game.ai.getTouchableCreatures(slot.getParent(), "Friendly");
	
		var targeter = game.createListTargeter(slot);
		targeter.addAllowedCreatures(creatures);
		targeter.activate();
	}
}

function onTargetSelect(game, targeter) {
	if (targeter.getSlot().getParent().abilities.has("MassRegeneration")) {
	    massRegeneration(game, targeter);
    } else {
	    regeneration(game, targeter);
    }
}

function massRegeneration(game, targeter) {
	var spell = targeter.getSlot().getAbility();
	var parent = targeter.getParent();
	
	var duration = 5;
	
	targeter.getSlot().setActiveRoundsLeft(duration);
	targeter.getSlot().activate();
	
	if (!spell.checkSpellFailure(parent)) return;
	
	var targets = targeter.getAffectedCreatures();
	for (var i = 0; i < targets.size(); i++) {
		applyEffect(game, targeter, targets.get(i), spell, duration);
	}
}

function regeneration(game, targeter) {
	var spell = targeter.getSlot().getAbility();
	var parent = targeter.getParent();
	var target = targeter.getSelectedCreature();
	
	var duration = 5;
	
	// cast the spell
	targeter.getSlot().setActiveRoundsLeft(duration);
	targeter.getSlot().activate();
   
	// check for spell failure
	if (!spell.checkSpellFailure(parent, target)) return;
	
	applyEffect(game, targeter, target, spell, duration);
}

function applyEffect(game, targeter, target, spell, duration) {
	var parent = targeter.getParent();

	var casterLevel = parent.stats.getCasterLevel();
	
	if (parent.abilities.has("MonstrousRegeneration")) {
		var healingLeft = game.dice().randInt(6 * casterLevel, 12 * casterLevel);
	} else {
		var healingLeft = game.dice().randInt(4 * casterLevel, 8 * casterLevel);
	}
	
	var effect = targeter.getSlot().createEffect("effects/regeneration");
	effect.setDuration(duration);
	effect.setTitle(spell.getName());
	effect.addPositiveIcon("items/enchant_spellHealing_small");
	
	effect.put("healingLeft", healingLeft);
	effect.put("totalRounds", duration);
	effect.put("currentRound", 0);
	
	target.applyEffect(effect);
	
	var anim = game.getBaseAnimation("crossFlash");
	anim.setRed(0.0);
	anim.setGreen(1.0);
	anim.setBlue(1.0);
	
	var position = target.getLocation().getCenteredScreenPoint();
	anim.setPosition(position.x, position.y - 10);
	
	game.runAnimationNoWait(anim);
	game.lockInterface(anim.getSecondsRemaining());
}