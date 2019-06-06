function onTargetEnter(game, target, aura) {
	var slot = aura.getSlot();
	var parent = slot.getParent();
	var casterLevel = parent.stats.getCasterLevel();
	
	if (parent.getFaction().isFriendly(target)) {
		var targetEffect = slot.createEffect();
		targetEffect.setTitle("Song of Luck");
		targetEffect.addPositiveIcon("items/enchant_attack_small");
		targetEffect.addPositiveIcon("items/enchant_spellFailure_small");
		targetEffect.setRemoveOnDeactivate(true);
		aura.addChildEffect(targetEffect);
	
		var chaBonus = (parent.stats.getCha() - 10);
		var lvls = parent.roles.getLevel("Bard");
	
		var bonus = 10 + 2 * lvls + 2 * chaBonus;
	
		targetEffect.getBonuses().addBonus('Attack', 'Luck', bonus);
		targetEffect.getBonuses().addBonus('SpellFailure', 'Luck', bonus);
		targetEffect.getBonuses().addSkillBonus('Locks', 'Luck', bonus);
		targetEffect.getBonuses().addSkillBonus('Traps', 'Luck', bonus);
		
		if (parent.abilities.has("SongOfAllies"))
			targetEffect.getBonuses().addBonus('Con', 'Luck', parseInt(chaBonus / 2) );
		
		target.applyEffect(targetEffect);
	} else if (parent.abilities.has("SongOfEnemies") && parent.getFaction().isHostile(target)) {
	
		var targetEffect = slot.createEffect();
		targetEffect.setTitle("Song of Luck");
		targetEffect.setRemoveOnDeactivate(true);
		aura.addChildEffect(targetEffect);
	
		var chaBonus = (parent.stats.getCha() - 10);
		
		targetEffect.addNegativeIcon("items/enchant_attack_small");
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
