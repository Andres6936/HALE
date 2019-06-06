function onAttackHit(game, attack, damage, effect) {
	var attacker = attack.getAttacker();
	var target = attack.getDefender();
	
	var lvls = attacker.roles.getLevel("Assassin");
	
	var dc = 75 + lvls * 5;
	
	if (!target.stats.getPhysicalResistanceCheck(dc)) {
		var targetEffect = attacker.createEffect("effects/damageOverTime");
		
		if (attacker.abilities.has("LingeringPoison"))
			targetEffect.setDuration(3);
		else
			targetEffect.setDuration(2);
		
		targetEffect.put("damageType", "Poison");
		
		if (attacker.abilities.has("LethalPoison")) {
			targetEffect.put("minDamagePerRound", 1 + parseInt(lvls / 2));
			targetEffect.put("maxDamagePerRound", 6 + parseInt(lvls / 2));
		} else {
			targetEffect.put("minDamagePerRound", 1);
			targetEffect.put("maxDamagePerRound", 6);
		}
		
		targetEffect.addNegativeIcon("items/enchant_acid_small");
		
		target.applyEffect(targetEffect);
		
		game.addMessage("red", target.getName() + " has been poisoned.");
	}
	
	var attacksLeft = effect.get("attacksLeft");
	attacksLeft -= 1;
	
	effect.put("attacksLeft", attacksLeft);
	
	if (attacksLeft == 0) {
		effect.getTarget().removeEffect(effect);
	}
}
