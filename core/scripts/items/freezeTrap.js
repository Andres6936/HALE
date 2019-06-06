function canUse(game, trap, parent) {
    return trap.canAttemptPlace(parent);
}

function onUse(game, trap, parent) {
    trap.attemptPlace(parent);
}

function onSpringTrap(game, trap, target) {
    
}

function onTrapReflexFailed(game, trap, target) {
    var effect = target.createEffect();
    effect.setTitle("Freeze Trap");
    effect.setDuration(3);
    
    if ( !target.stats.getPhysicalResistanceCheck(trap.modifyValueByQuality(100)) ) {
        effect.getBonuses().addPenalty("ActionPoint", -trap.modifyValueByQuality(20) );
    } else {
        effect.getBonuses().addPenalty("ActionPoint", -trap.modifyValueByQuality(20) / 2);   
    }
    
	var pos = target.getLocation().getCenteredScreenPoint();
	
	var g1 = game.getBaseParticleGenerator("paralysis");
	g1.setPosition(pos.x, pos.y + 10.0);
	g1.setRedDistribution(game.getFixedDistribution(0.0));
	g1.setGreenDistribution(game.getFixedDistribution(0.2));
	g1.setAlphaDistribution(game.getFixedDistribution(0.5));
	effect.addAnimation(g1);
	    target.applyEffect(effect);
	game.addMessage("red", target.getName() + " is partially frozen.");
}
