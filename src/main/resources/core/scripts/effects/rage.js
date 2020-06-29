function onRemove(game, effect) {
	var target = effect.getTarget();

	if (!target.abilities.has("SteadfastRage")) {
		var newEffect = effect.getSlot().createEffect();
		newEffect.setTitle("Rage Exhaustion");
		newEffect.setDuration(game.dice().randInt(3, 6));
	
		newEffect.getBonuses().addPenalty('Str', 'Stackable', -2);
		newEffect.getBonuses().addPenalty('ActionPoint', 'Stackable', -25);
	
		target.applyEffect(newEffect);
	}
}
