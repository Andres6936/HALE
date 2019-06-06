function onTargetEnter(game, target, aura) {
	var slot = aura.getSlot();
	var parent = slot.getParent();
	var casterLevel = parent.stats.getCasterLevel();
	
	if (parent.getFaction().isFriendly(target)) {
		var targetEffect = slot.createEffect();
		targetEffect.setTitle("Song of Slaying");
		targetEffect.setRemoveOnDeactivate(true);
		aura.addChildEffect(targetEffect);
	
		var chaBonus = (parent.stats.getCha() - 10);
		var lvls = parent.roles.getLevel("Bard");
	
		var bonus = 20 + 4 * lvls + 4 * chaBonus;
		var halfBonus = parseInt(bonus / 2);
	
		targetEffect.getBonuses().addBonus('Damage', 'Luck', bonus);
		targetEffect.getBonuses().addBonus('SpellDamage', 'Luck', halfBonus);
		
		targetEffect.addPositiveIcon("items/enchant_damage_small");
		targetEffect.addPositiveIcon("items/enchant_spellDamage_small");
		
		if (parent.abilities.has("SongOfAllies"))
			targetEffect.getBonuses().addBonus('Con', 'Luck', parseInt(chaBonus / 2) );
		
		target.applyEffect(targetEffect);
	} else if (parent.abilities.has("SongOfEnemies") && parent.getFaction().isHostile(target)) {
	
		var targetEffect = slot.createEffect();
		targetEffect.setTitle("Song of Slaying");
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
