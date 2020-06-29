function onActivate(game, slot) {
	var parent = slot.getParent();
	
	var gestures = getActiveGestures(parent);
	
	var creatures = game.ai.getTouchableCreatures(parent, "Hostile");
	var targeter = game.createListTargeter(slot);
	targeter.addAllowedCreatures(creatures);
	targeter.activate();
}

function canActivate(game, parent, slot) {
	var parent = slot.getParent();
	
	if (!parent.timer.canActivateAbility(slot.getAbilityID())) return false;
	
	if (getNumActiveWords(parent) == 0) return false;
	
	return true;
}

function getNumActiveWords(parent) {
	var count = 0;
	
	if (parent.get("roleMediumWordFire") == true) count++;
	if (parent.get("roleMediumWordIce") == true) count++;
	if (parent.get("roleMediumWordAcid") == true) count++;
	if (parent.get("roleMediumWordLightning") == true) count++;
	
	return count;
}

function getNumActiveGestures(parent) {
	var count = 0;
	
	return count;
}

function onTargetSelect(game, targeter) {
	var ability = targeter.getSlot().getAbility();
	var parent = targeter.getParent();
    // cast the spell
    targeter.getSlot().activate();
	
	var target = targeter.getSelectedCreature();

	// check for spell failure
	if (!ability.checkSpellFailure(parent, target)) return;
	
	// show the generic invocation animation
	var anim = game.getBaseAnimation("scissor");
	var position = parent.getLocation().getCenteredScreenPoint();
	anim.setPosition(position.x, position.y);
	game.runAnimationNoWait(anim);
	game.lockInterface(anim.getSecondsRemaining());
	
	// create the callback that will apply the effect
	var callback = ability.createDelayedCallback("applyWords");
	callback.setDelay(anim.getSecondsRemaining() - 0.2);
	callback.addArguments([parent, target, targeter]);
	callback.start();
}

function getActiveWords(parent) {
	var words = [];
	
	if (parent.get("roleMediumWordFire") == true) words.push("Fire");
	if (parent.get("roleMediumWordIce") == true) words.push("Ice");
	if (parent.get("roleMediumWordAcid") == true) words.push("Acid");
	if (parent.get("roleMediumWordLightning") == true) words.push("Lightning");
	
	return words;
}

function getActiveGestures(parent) {
	var gestures = [];
	
	return gestures;
}

function applyWords(game, parent, target, targeter) {
	var words = getActiveWords(parent);
	var spell = targeter.getSlot().getAbility();
	var casterLevel = parent.stats.getCasterLevel();
	
	// create the animation
	var anim = game.getBaseAnimation("blast");
	var position = target.getLocation().getCenteredScreenPoint();
	anim.setPosition(position.x, position.y);
	
	if (parent.get("roleMediumWordFire") == true) {
		var damage = game.dice().rand(4, 8) + casterLevel;
		spell.applyDamage(parent, target, damage, "Fire");
		anim.setBlue(0.3);
		anim.setGreen(0.3);
	}
	
	if (parent.get("roleMediumWordIce") == true) {
		var damage = game.dice().rand(3, 6) + casterLevel;
		spell.applyDamage(parent, target, damage, "Cold");
		anim.setRed(0.3);
		anim.setGreen(0.5);
	}
	
	if (parent.get("roleMediumWordAcid") == true) {
		var damage = game.dice().rand(2, 6) + casterLevel;
		spell.applyDamage(parent, target, damage, "Acid");
		anim.setRed(0.0);
		anim.setBlue(0.0);
	}
	
	if (parent.get("roleMediumWordLightning") == true) {
		var damage = game.dice().rand(4, 8) + casterLevel;
		spell.applyDamage(parent, target, damage, "Electrical");
	}
	
	game.runAnimationNoWait(anim);
	game.lockInterface(anim.getSecondsRemaining());
}
