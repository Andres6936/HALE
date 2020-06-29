function onActivate(game, slot) {
	if (slot.getParent().abilities.has("MassChameleon")) {
		var targeter = game.createCircleTargeter(slot);
		targeter.setRadius(3);
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
	var spell = targeter.getSlot().getAbility();
	var parent = targeter.getParent();
	var casterLevel = parent.stats.getCasterLevel();
	
	var duration = game.dice().randInt(5, 10);
	
	// cast the spell
	targeter.getSlot().setActiveRoundsLeft(duration);
	targeter.getSlot().activate();
   
    if (parent.abilities.has("MassChameleon")) {
		// check for spell failure
		if (!spell.checkSpellFailure(parent)) return;
		
		var targets = targeter.getAffectedCreatures();
		for (var i = 0; i < targets.size(); i++) {
			var target = targets.get(i);
			
			applyEffect(game, targeter, target, duration);
		}
		
	} else {
		var target = targeter.getSelectedCreature();
   
		// check for spell failure
		if (!spell.checkSpellFailure(parent, target)) return;
	
		applyEffect(game, targeter, target, duration);
	}
}

function applyEffect(game, targeter, target, duration) {
	var spell = targeter.getSlot().getAbility();
	var parent = targeter.getParent();
	var casterLevel = parent.stats.getCasterLevel();

	var effect = targeter.getSlot().createEffect();
	effect.setDuration(duration);
	effect.addPositiveIcon("items/enchant_invisibility_small");
	effect.setTitle(spell.getName());
	
	if (parent.abilities.has("ImprovedChameleon")) {
		effect.getBonuses().addBonus('Concealment', 15 + casterLevel);
	} else {
		effect.getBonuses().addBonus('Concealment', parseInt(15 + casterLevel / 2));
	}
	
	var anim = game.createAnimation("particles/circle33", 2.0);
	anim.setDurationInfinite();
	anim.setAlpha(0.7);
	anim.setRed(0.3);
	anim.setGreen(0.8);
	anim.setBlue(0.0);
	anim.setPosition(target.getLocation().getCenteredScreenPoint());
	
	effect.addAnimation(anim);
	
	target.applyEffect(effect);
}