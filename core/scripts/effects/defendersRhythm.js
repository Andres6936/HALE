function onTargetEnter(game, target, aura) {
	var slot = aura.getSlot();
	var parent = slot.getParent();
	var casterLevel = parent.stats.getCasterLevel();
	
	if (parent.getFaction().isFriendly(target)) {
		var targetEffect = slot.createEffect();
		targetEffect.setTitle("Defender's Rhythm");
		targetEffect.addPositiveIcon("items/enchant_armor_small");
		targetEffect.setRemoveOnDeactivate(true);
		aura.addChildEffect(targetEffect);
	
		var chaBonus = (parent.stats.getCha() - 10);
		var lvls = parent.roles.getLevel("Bard");
	
		var bonus = 10 + 2 * lvls + 2 * chaBonus;
	
		targetEffect.getBonuses().addBonus('ArmorClass', 'Stackable', bonus);
		targetEffect.getBonuses().addBonus('ArmorPenalty', 'Luck', -bonus);
		
		if (parent.abilities.has("SongOfAllies"))
			targetEffect.getBonuses().addBonus('Con', 'Luck', parseInt(chaBonus / 2) );
		
		target.applyEffect(targetEffect);
	} else if (parent.abilities.has("SongOfEnemies") && parent.getFaction().isHostile(target)) {
	
		var targetEffect = slot.createEffect();
		targetEffect.setTitle("Defender's Rhythm");
		targetEffect.setRemoveOnDeactivate(true);
		aura.addChildEffect(targetEffect);
	
		var chaBonus = (parent.stats.getCha() - 10);
		
		targetEffect.getBonuses().addPenalty('Attack', 'Luck', -10 - chaBonus);
		
		target.applyEffect(targetEffect);
	}
}

function onTargetExit(game, target, aura) {
	var parent = aura.getSlot().getParent();

	if (parent.getFaction().isFriendly(target)) {
		var targetEffect = aura.getChildEffectWithTarget(target);
   
		target.removeEffect(targetEffect);
		aura.removeChildEffect(targetEffect);
	} else if (parent.abilities.has("SongOfEnemies") && parent.getFaction().isHostile(target)) {
		var targetEffect = aura.getChildEffectWithTarget(target);
   
		target.removeEffect(targetEffect);
		aura.removeChildEffect(targetEffect);
	}
}
