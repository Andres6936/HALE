function onActivate(game, slot) {
	var targeter = game.createCircleTargeter(slot);
	targeter.setRadius(3);
	targeter.setRelationshipCriterion("Friendly");
	targeter.addAllowedPoint(slot.getParent().getLocation());
	targeter.activate();
}

function onTargetSelect(game, targeter) {
	var spell = targeter.getSlot().getAbility();
	var parent = targeter.getParent();
	var casterLevel = parent.stats.getCasterLevel();
	
	// cast the spell
	targeter.getSlot().activate();
   
	// check for spell failure
	if (!spell.checkSpellFailure(parent)) return;
	
	var creatures = targeter.getAffectedCreatures();

	for (var creatureIndex = 0; creatureIndex < creatures.size(); creatureIndex++) {
		var target = creatures.get(creatureIndex);
		
		var points = parseInt(2 + casterLevel / 2);
	
		var types = [ "Str", "Dex", "Con", "Int", "Wis", "Cha" ];
	
		for (var i = 0; i < types.length; i++) {
			points = target.stats.reducePenaltiesOfTypeByAmount(types[i], points);
		}
	
		if (targeter.getSlot().getParent().abilities.has("RestoreMorale")) {
			var reductionAmount = parseInt(5 + casterLevel / 2);
		
			var types = [ "Attack", "AttackCost", "ArmorClass", "ArmorPenalty", "Skill",
				"MentalResistance", "PhysicalResistance", "ReflexResistance", "SpellFailure" ];
		
			for (var i = 0; i < types.length; i++) {
				target.stats.reducePenaltiesOfTypeByAmount(types[i], reductionAmount);
			}
		}
	
		if (targeter.getSlot().getParent().abilities.has("RemoveParalysis")) {
			target.stats.removeEffectPenaltiesOfType("Immobilized");
			target.stats.removeEffectPenaltiesOfType("Helpless");
		}
	
		var anim = game.getBaseAnimation("crossFlash");
		anim.setRed(0.8);
		anim.setGreen(0.8);
		anim.setBlue(1.0);
	
		var position = target.getLocation().getCenteredScreenPoint();
		anim.setPosition(position.x, position.y - 10);
		
		game.runAnimationNoWait(anim);
	}
	
	game.lockInterface(anim.getSecondsRemaining());
}
