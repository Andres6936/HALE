function onRoundElapsed(game, effect) {
	var damage = effect.get("healingPerRound");
	
	// don't heal if target is at full health
	if (effect.getTarget().stats.getMaxHP() == effect.getTarget().getCurrentHitPoints())
		return;
		
	// don't attempt heal if target is dead
	if (effect.getTarget().isDead())
		return;
	
	effect.getTarget().healDamage(damage);
}
