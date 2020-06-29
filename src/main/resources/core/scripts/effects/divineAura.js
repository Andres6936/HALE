function onTargetEnter(game, target, aura) {
	var slot = aura.getSlot();
	var parent = slot.getParent();
	var casterLevel = parent.stats.getCasterLevel();
	
	var targetEffect = slot.createEffect();
	targetEffect.setDuration(slot.getActiveRoundsLeft());
	targetEffect.setTitle(slot.getAbility().getName());
	aura.addChildEffect(targetEffect);
	
	var attackBonus = 5 + casterLevel;
	var damageBonus = 10 + 2 * casterLevel;
	var resistanceBonus = 10 + casterLevel;
	
	if (parent.getFaction().isFriendly(target)) {
		targetEffect.addPositiveIcon("items/enchant_attack_small");
		targetEffect.addPositiveIcon("items/enchant_damage_small");
	
		targetEffect.getBonuses().addBonus('Attack', 'Morale', attackBonus);
		targetEffect.getBonuses().addBonus('Damage', 'Morale', damageBonus);
		targetEffect.getBonuses().addBonus('MentalResistance', 'Morale', resistanceBonus);
		targetEffect.getBonuses().addBonus('PhysicalResistance', 'Morale', resistanceBonus);
		targetEffect.getBonuses().addBonus('ReflexResistance', 'Morale', resistanceBonus);
		
		var anim = game.getBaseAnimation("sparkleAnim");
		var position = target.getLocation().getCenteredScreenPoint();
		anim.setPosition(position.x, position.y);
		game.runAnimationNoWait(anim);
		
	} else if (parent.getFaction().isHostile(target)) {
		targetEffect.addNegativeIcon("items/enchant_attack_small");
		targetEffect.addNegativeIcon("items/enchant_damage_small");
		
		targetEffect.getBonuses().addPenalty('Attack', 'Morale', -attackBonus);
		targetEffect.getBonuses().addPenalty('Damage', 'Morale', -damageBonus);
		targetEffect.getBonuses().addPenalty('MentalResistance', 'Morale', -resistanceBonus);
		targetEffect.getBonuses().addPenalty('PhysicalResistance', 'Morale', -resistanceBonus);
		targetEffect.getBonuses().addPenalty('ReflexResistance', 'Morale', -resistanceBonus);
		
		var anim = game.getBaseAnimation("sparkleAnim");
		var position = target.getLocation().getCenteredScreenPoint();
		anim.setGreen(0.0);
		anim.setBlue(0.0);
		anim.setPosition(position.x, position.y);
		game.runAnimationNoWait(anim);
	}
	
	target.applyEffect(targetEffect);
}

function onTargetExit(game, target, aura) {
	var targetEffect = aura.getChildEffectWithTarget(target);
   
	target.removeEffect(targetEffect);
	aura.removeChildEffect(targetEffect);
}
