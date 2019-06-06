function onTargetEnter(game, target, aura) {
	var slot = aura.getSlot();
	var parent = slot.getParent();
	var casterLevel = parent.stats.getCasterLevel();
	
	if (parent.getFaction().isHostile(target)) {
		var targetEffect = slot.createEffect();
		targetEffect.setTitle("Curse Song");
		targetEffect.setRemoveOnDeactivate(true);
		aura.addChildEffect(targetEffect);
	
		var chaBonus = (parent.stats.getCha() - 10);
		var lvls = parent.roles.getLevel("Bard");
	
		var penalty = parseInt((lvls + chaBonus) / 2);
	
		targetEffect.getBonuses().addPenalty('Dex', 'Luck', -penalty);
		targetEffect.getBonuses().addPenalty('Str', 'Luck', -penalty);
		targetEffect.addNegativeIcon("items/enchant_strength_small");
		targetEffect.addNegativeIcon("items/enchant_dexterity_small");
		
		if (parent.abilities.has("SongOfEnemies"))
			targetEffect.getBonuses().addPenalty('Attack', 'Luck', -10 - chaBonus);
		
		target.applyEffect(targetEffect);
	} else if (parent.abilities.has("SongOfAllies") && parent.getFaction().isFriendly(target)) {
		var targetEffect = slot.createEffect();
		targetEffect.setTitle("Curse Song");
		targetEffect.setRemoveOnDeactivate(true);
		aura.addChildEffect(targetEffect);
	
		var chaBonus = (parent.stats.getCha() - 10);
		
		targetEffect.getBonuses().addBonus('Con', 'Luck', parseInt(chaBonus / 2) );
		targetEffect.addPositiveIcon("items/enchant_constitution_small");
		target.applyEffect(targetEffect);
	}
}

function onTargetExit(game, target, aura) {
	var parent = aura.getSlot().getParent();

	if (parent.getFaction().isHostile(target)) {
		var targetEffect = aura.getChildEffectWithTarget(target);
   
		target.removeEffect(targetEffect);
		aura.removeChildEffect(targetEffect);
	} else if (parent.abilities.has("SongOfAllies") && parent.getFaction().isFriendly(target)) {
		var targetEffect = aura.getChildEffectWithTarget(target);
   
		target.removeEffect(targetEffect);
		aura.removeChildEffect(targetEffect);
	}
}
