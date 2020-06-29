function onDefense(game, attack, effect) {
	var curAC = attack.getDefenderAC();
	
	var parent = attack.getDefender();
	
	attack.setDefenderAC(curAC + 2 * parent.roles.getLevel("Monk") + 2 * (parent.stats.getWis() - 10));
}