function onAttack(game, attack, effect) {
	if (!attack.isMeleeWeaponAttack()) return;

	var attacker = attack.getAttacker();
	var defender = attack.getDefender();
	
	var parent = effect.getSlot().getParent();
	var lvls = parent.roles.getLevel("Duelist");
	
	if (parent == defender) {
		attack.setDefenderAC(attack.getDefenderAC() + 10 + 4 * lvls);
	}
	
	if (parent.abilities.has("Parry")) {
		var dist = parent.getLocation().getDistance(attacker);
		if (dist <= 1)
			attack.addExtraAttack(-10 - 2 * lvls);
	}
}

function onDefense(game, attack, effect) {
	if (!attack.isMeleeWeaponAttack()) return;

	var attacker = attack.getAttacker();
	var defender = attack.getDefender();
	
	var parent = effect.getSlot().getParent();
	var lvls = parent.roles.getLevel("Duelist");
	
	if (parent == attacker) {
		attack.addExtraAttack(10 + 4 * lvls);
		
		if (parent.abilities.has("DeadlyDuel"))
			attack.addExtraDamage(lvls);
	}
}
