function canUse(game, trap, parent) {
    return trap.canAttemptPlace(parent);
}

function onUse(game, trap, parent) {
    trap.attemptPlace(parent);
}

function onSpringTrap(game, trap, target) {
    
}

function onTrapReflexFailed(game, trap, target) {    var effect = target.createEffect("effects/damageOverTime");
    effect.setDuration(6);
    effect.setTitle("Acid Trap");
    effect.put("damagePerRound", trap.modifyValueByQuality(6) );
    effect.put("damageType", "Acid");
	
	var pos = target.getLocation().getCenteredScreenPoint();
	
	var g1 = game.getBaseParticleGenerator("flame");
	g1.setPosition(pos.x - game.dice().rand(2, 8), pos.y + game.dice().rand(0, 10));
	g1.setVelocityDistribution(game.getFixedAngleDistribution(10.0, 20.0, 3.14159 / 2));
	g1.setRedSpeedDistribution(game.getGaussianDistribution(-1.0, 0.05));
	g1.setBlueSpeedDistribution(game.getGaussianDistribution(-6.0, 0.05));
	effect.addAnimation(g1);
	
	var g2 = game.getBaseParticleGenerator("flame");
	g2.setVelocityDistribution(game.getFixedAngleDistribution(10.0, 20.0, 3.14159 / 2));
	g2.setRedSpeedDistribution(game.getGaussianDistribution(-1.0, 0.05));
	g2.setBlueSpeedDistribution(game.getGaussianDistribution(-6.0, 0.05));
	g2.setPosition(pos.x + game.dice().rand(2, 8), pos.y + game.dice().rand(0, 10));
	effect.addAnimation(g2);
	
    target.applyEffect(effect);
    game.addMessage("red", target.getName() + " is covered in burning acid.");
}
