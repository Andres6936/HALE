function onDefenseHit(game, attack, damage, effect) {
	var attacker = attack.getAttacker();
	var defender = attack.getDefender();
	var casterLevel = defender.stats.getCasterLevel();
	
	if (!attack.isMeleeWeaponAttack()) return;
	
	var spell = effect.getSlot().getAbility();
	
	var damage = game.dice().d6() + parseInt(casterLevel / 4);
	
	spell.applyDamage(defender, attacker, damage, effect.get("type"));
}