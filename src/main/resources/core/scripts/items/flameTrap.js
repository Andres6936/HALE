function canUse(game, trap, parent) {
    return trap.canAttemptPlace(parent);
}

function onUse(game, trap, parent) {
    trap.attemptPlace(parent);
}

function onSpringTrap(game, trap, target) {
    
}

function onTrapReflexFailed(game, trap, target) {	var effect = target.createEffect("effects/damageOverTime");
    effect.setDuration(2);
    effect.setTitle("Flame Trap");
    effect.put("damagePerRound", trap.modifyValueByQuality(3) );
    effect.put("damageType", "Fire");
	
	var pos = target.getLocation().getCenteredScreenPoint();
	
	var g1 = game.getBaseParticleGenerator("flame");
	g1.setPosition(pos.x - game.dice().rand(2, 8), pos.y + game.dice().rand(0, 10));
	effect.addAnimation(g1);
	
	var g2 = game.getBaseParticleGenerator("flame");
	g2.setPosition(pos.x + game.dice().rand(2, 8), pos.y + game.dice().rand(0, 10));
	effect.addAnimation(g2);
	
	var g3 = game.getBaseParticleGenerator("flame");
	g3.setPosition(pos.x + game.dice().rand(-2, 2), pos.y + game.dice().rand(10, 23));
	effect.addAnimation(g3);
	
    target.applyEffect(effect);
    game.addMessage("red", target.getName() + " is on fire.");
}
