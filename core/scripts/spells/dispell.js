function isTargetValid(game, target, slot) {
	var effects = target.getEffects().getDispellableEffects();
	return effects.size() != 0;
}

function onActivate(game, slot) {
	var creatures = game.ai.getTouchableCreatures(slot.getParent(), "Friendly");
	
	for (var i = 0; i < creatures.size(); i++) {
		if ( !isTargetValid(game, creatures.get(i), slot) ) {
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
	
	var effects = shuffle(target.getEffects().getDispellableEffects());
	
	var maxDispellAttempts = 2;
	var modifierBonus = 0;
	if (parent.abilities.has("GreaterDispell")) {
		maxDispellAttempts = 5;
		modifierBonus = 1;
	}
	
	for (var i = 0; i < effects.size(); i++) {
		if (i >= maxDispellAttempts) break;
	
		var effect = effects.get(i);
		
		// compute the dispell difficulty of the effect
		var effectCaster = effect.getSlot().getParent();
		var effectCasterStat = effectCaster.roles.getBaseRole().getSpellCastingAttribute();
		var effectCasterAttr = effectCaster.stats.get(effectCasterStat);
		
		var dc = effect.getSlot().getAbility().getSpellLevel(effectCaster) * 2 + effectCasterAttr;
		
		// compute the strength of the dispell
		var parentStat = parent.roles.getBaseRole().getSpellCastingAttribute();
		var parentAttr = parent.stats.get(parentStat);
		
		var modifier = spell.getSpellLevel(parent) * 2 + parentAttr + modifierBonus;
		
		// attempt the dispell
		var roll = game.dice().randInt(-4, 4) + modifier - dc;
		
		if (roll > 0) {
			// the dispell was successful
			
			target.removeEffect(effect);
			game.addMessage("blue", "Succeeded in dispelling " + effect.getTitle() + ".");
		} else {
			game.addMessage("blue", "Failed in dispelling " + effect.getTitle() + ".");
		}
	}
	
	var anim = game.getBaseAnimation("crossFlash");
	anim.setRed(0.8);
	anim.setGreen(0.8);
	anim.setBlue(1.0);
	
	var position = target.getLocation().getCenteredScreenPoint();
	anim.setPosition(position.x, position.y - 10);
	
	game.runAnimationNoWait(anim);
	game.lockInterface(anim.getSecondsRemaining());
}

function shuffle(list) {
    var tmp, current, top = list.size();

    if (top) while(--top) {
        current = Math.floor(Math.random() * (top + 1));
		
		tmp = list.get(current);
		list.set(current, list.get(top));
		list.set(top, tmp);
    }

    return list;
}