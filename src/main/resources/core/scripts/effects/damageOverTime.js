function onRoundElapsed(game, effect) {
	var damage = effect.get("damagePerRound");
	var type = effect.get("damageType");
	
	var minDamage = effect.get("minDamagePerRound");
	var maxDamage = effect.get("maxDamagePerRound");
	
	// optionally support random amount of damage
	if (minDamage != null && maxDamage != null) {
		damage = game.dice().randInt(minDamage, maxDamage);
	}
	
	var slot = effect.getSlot();
	
	var spell = null;
	if (slot != null) {
		var ability = slot.getAbility();
		if (ability.getSpellLevel() > 0)
			spell = ability;
	}

	if (spell != null) {
		var parent = slot.getParent();
	
		spell.applyDamage(parent, effect.getTarget(), damage, type);
	} else {
		effect.getTarget().takeDamage(damage, type);
	}
}