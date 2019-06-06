
function onAttackHit(game, weapon, attack, damage) {
    var parent = attack.getAttacker();
    var target = attack.getDefender();
    
    if (!target.stats.getPhysicalResistanceCheck(60)) {
        var effect = target.createEffect();
        effect.setDuration(10);
        effect.getBonuses().addPenalty('Str', 'Stackable', -1);
        effect.setTitle("Ghoul Rot");
        target.applyEffect(effect);
        
        var g1 = game.getBaseParticleGenerator("inwardBurst");
	        g1.setPosition(target.getLocation());
	        g1.setRedDistribution(game.getUniformDistribution(0.8 - 0.05, 0.8 + 0.05));
	        g1.setGreenDistribution(game.getFixedDistribution(0.0));
	        g1.setBlueDistribution(game.getFixedDistribution(0.0));
	        g1.setVelocityDistribution(game.getVelocityTowardsPointDistribution(target.getLocation().getCenteredScreenPoint(), g1.getTimeLeft()));
        game.runParticleGeneratorNoWait(g1);
    }
}
