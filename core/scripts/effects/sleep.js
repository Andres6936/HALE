function onApply(game, effect) {
	effect.getBonuses().add('Immobilized');
	effect.getBonuses().add('Helpless');
	
	var generator = game.createParticleGenerator("Point", "Continuous", "particles/sleepZ", 1.5);
	generator.setDurationInfinite();
	
	var position = effect.getTarget().getLocation().getCenteredScreenPoint();
	generator.setPosition(position.x, position.y - 10.0);
	generator.setStopParticlesAtOpaque(false);
	generator.setDrawParticlesInOpaque(true);
	generator.setVelocityDistribution(game.getFixedAngleDistribution(20.0, 35.0, -3.14159 / 2));
	generator.setDurationDistribution(game.getUniformDistribution(0.8, 1.3));
	generator.setAlphaSpeedDistribution(game.getFixedDistribution(-0.5));
	generator.setAlphaDistribution(game.getFixedDistribution(1.0));
	generator.setRedDistribution(game.getFixedDistribution(0.7));
	generator.setGreenDistribution(game.getFixedDistribution(0.0));
	generator.setBlueDistribution(game.getFixedDistribution(1.0));
	effect.addAnimation(generator);
}

function onDamaged(game, damage, effect) {
	// wake sleeping creatures when they are damaged
	effect.getTarget().removeEffect(effect);
}
