function onAttack(game, attack, effect) {
	var offHand = game.getOffHandAttack(attack.getAttacker(), attack.getDefender());
	
	// stack the damage from the two attacks together
	attack.addDamage(offHand.getDamage());
}
