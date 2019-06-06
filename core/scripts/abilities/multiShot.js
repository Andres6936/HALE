function canActivate(game, parent) {
	if (!parent.timer.canAttack()) return false;
	
	var weapon = parent.getMainHandWeapon();
	
	var baseWeaponName = weapon.getTemplate().getBaseWeapon().getName();
	return (baseWeaponName.equals("Longbow") || baseWeaponName.equals("Shortbow") ||
		baseWeaponName.equals("Crossbow"))
}

function onActivate(game, slot) {
	if (slot.getParent().abilities.has("Scattershot")) {
		var creatures = game.ai.getAttackableCreatures(slot.getParent());
	
		var targeter = game.createCircleTargeter(slot);
		targeter.setRadius(2);
		targeter.addAllowedCreatures(creatures);
		targeter.activate();
	} else {
		var creatures = game.ai.getAttackableCreatures(slot.getParent());
	
		var targeter = game.createListTargeter(slot);
		targeter.addAllowedCreatures(creatures);
		targeter.activate();
	}
}

function onTargetSelect(game, targeter) {
	targeter.getSlot().activate();
	
	var ability = targeter.getSlot().getAbility();
	
	// perform the attack in a new thread as the standardAttack will
	// block
	var cb = ability.createDelayedCallback("performAttack");
	cb.addArgument(targeter);
	cb.start();
}

function performAttack(game, targeter) {
	var parent = targeter.getParent();
	var center = targeter.getMouseGridPosition();
	var target = game.currentArea().getCreatureAtGridPoint(center);
	
	var numAttacks = parseInt(parent.stats.getLevelAttackBonus() / 25) + 2;
	
	game.standardAttack(parent, target);
	numAttacks--;
	
	if (parent.abilities.has("Scattershot")) {
		var g1 = game.getBaseParticleGenerator("explosion");
		g1.setNumParticles(100.0);
		g1.setDurationDistribution(game.getFixedDistribution(1.0));
		g1.setPosition(center);
		g1.setVelocityDistribution(game.getEquallySpacedAngleDistribution(150.0, 200.0, 5.0, 100.0, 1.0));
		g1.setRedDistribution(game.getUniformDistribution(0.3, 0.5));
		g1.setGreenDistribution(game.getUniformDistribution(0.2, 0.3));
		g1.setBlueDistribution(game.getUniformDistribution(0.1, 0.2));
		g1.setAlphaSpeedDistribution(game.getFixedDistribution(-1.0));
		game.runParticleGeneratorNoWait(g1);
		game.lockInterface(g1.getTimeLeft());
		
		var targets = targeter.getAffectedCreatures();
		for (var i = 0; i < targets.size(); i++) {
			// don't apply shrapnel to the main target
			if (targets.get(i) == target) continue;
			
			var delay = targets.get(i).getLocation().getScreenDistance(center) / 200.0;
			
			var damage = game.dice().d10() + parseInt(parent.stats.getCreatureLevel() / 3);
			
			var callback = targeter.getSlot().getAbility().createDelayedCallback("applyScattershot");
			callback.setDelay(delay);
			callback.addArguments([targets.get(i), damage]);
			callback.start();
		}
	}
	
	while (numAttacks > 0) {
		game.singleAttack(parent, target);
		
		if (target.isDead()) break;
		numAttacks--;
	}
}

function applyScattershot(game, target, damage) {
	target.takeDamage(damage, "Piercing");
}
