function onDefense(game, attack, effect) {
	// no effect on non ranged attacks
	if (attack.isRangedWeaponAttack())
		attack.negateDamage();
}

function onDefenseHit(game, attack, damage, effect) {
	if (attack.isRangedWeaponAttack())
		game.addMessage("blue", "Damage negated by Deflect Projectiles.");
}