function onTargetEnter(game, target, aura) {
	createEffect(aura, target, false);
}

function createEffect(aura, target, roundElapsed) {
	var slot = aura.getSlot();
	var parent = slot.getParent();
	
	var chaBonus = (parent.stats.getCha() - 10) * 2;
	if (parent.abilities.has("PersonalMagnetism"))
		chaBonus = chaBonus * 2;
	
	var lvlBonus = parent.roles.getLevel("Paladin");
	
	var amount = 10 + chaBonus + lvlBonus;

	var targetEffect = slot.createEffect();
	targetEffect.setTitle(slot.getAbility().getName());
	aura.addChildEffect(targetEffect);
	
	if (parent.getFaction().isFriendly(target)) {
		targetEffect.getBonuses().addBonus('Attack', amount);
		targetEffect.getBonuses().addBonus('SpellFailure', amount);
		targetEffect.getBonuses().addBonus('ConcealmentIgnoring', amount);
		
		if (parent.abilities.has("PositiveEnergy")) {
			targetEffect.getBonuses().addAttackBonusVsRacialType("Undead", amount);
			targetEffect.getBonuses().addDamageBonusVsRacialType("Undead", 2 * amount);
			
			if (roundElapsed) {
				target.healDamage(chaBonus);
			}
		}
	}
	
	targetEffect.setDuration(1);
	
	target.applyEffect(targetEffect);
}

function onTargetExit(game, target, aura) {
    var targetEffect = aura.getChildEffectWithTarget(target);
   
	target.removeEffect(targetEffect);
	aura.removeChildEffect(targetEffect);
}

function onRoundElapsed(game, aura) {
    var targets = game.currentArea().getAffectedCreatures(aura);
	for (var i = 0; i < targets.size(); i++) {
		var target = targets.get(i);
		
		var targetEffect = aura.getChildEffectWithTarget(target);
		target.removeEffect(targetEffect);
		aura.removeChildEffect(targetEffect);
		
		createEffect(aura, target, true);
	}
}
