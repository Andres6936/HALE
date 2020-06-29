function canActivate(game, parent) {
	if (!parent.timer.canAttack()) return false;
	
	var weapon = parent.getMainHandWeapon();
	
	return weapon.getTemplate().getBaseWeapon().getName() == "Unarmed";
}

function onActivate(game, slot) {
   var creatures = game.ai.getAttackableCreatures(slot.getParent());

   var targeter = game.createListTargeter(slot);
   targeter.addAllowedCreatures(creatures);
   targeter.activate();
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
	var target = targeter.getSelectedCreature();
	
	if (game.standardAttack(parent, target)) {
		// attack suceeded
		var checkDC = 50 + 4 * (parent.stats.getWis() - 10) +
			parent.roles.getLevel("Monk") * 4;
		
		if (!target.stats.getReflexResistanceCheck(checkDC)) {
			// target failed check
			applyEffect(game, parent, target, targeter.getSlot());
			
			game.addMessage("red", parent.getName() + " succeeded on Stunning Blow against " + target.getName() + ".");
		} else {
			game.addMessage("red", parent.getName() + " failed on Stunning Blow against " + target.getName() + ".");
			game.addFadeAway("Failed", target.getLocation().getX(), target.getLocation().getY(), "gray");
		}
	} else {
		game.addMessage("red", parent.getName() + " missed on Stunning Blow attempt.");
	}
}

function applyEffect(game, parent, target, slot) {
	if ( target.stats.has("ImmobilizationImmunity")) {
		game.addMessage("blue", target.getName() + " is immune.");
		return;
	}
		
	var effect = slot.createEffect();
	effect.setDuration(2);
	effect.setTitle("Stunning Blow");
	effect.getBonuses().add("Immobilized");
	effect.getBonuses().add("Helpless");
	effect.addNegativeIcon("items/enchant_death_small");
	
	var g1 = game.getBaseParticleGenerator("sparkle");
	g1.setDurationInfinite();
	g1.setRotationSpeedDistribution(game.getUniformDistribution(100.0, 200.0));
	g1.setPosition(target.getLocation());
	g1.setBlueDistribution(game.getFixedDistribution(0.0));
	g1.setGreenDistribution(game.getFixedDistribution(0.0));
	effect.addAnimation(g1);
	
	target.applyEffect(effect);
}