function canActivate(game, parent) {
	var apCost = parent.stats.getAttackCost() * 3;

	if (!parent.timer.canPerformAction(apCost)) return false;
	
	var weapon = parent.getMainHandWeapon();
	if (!weapon.isMelee()) return false;
	
	var offHand = parent.getOffHandWeapon();
	if (offHand == null) return false;
	
	return true
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

   // perform the attack in a new thread as the animating attack will block
   var cb = ability.createDelayedCallback("performAttack");
   cb.addArgument(targeter);
   cb.start();
   
   var effect = targeter.getSlot().createEffect();
   effect.setTitle(targeter.getSlot().getAbility().getName());
   effect.setDuration(1);
   effect.getBonuses().addPenalty('ArmorClass', -20);
   targeter.getParent().applyEffect(effect);
}

function performAttack(game, targeter) {
	var parent = targeter.getParent();
	var target = targeter.getSelectedCreature();
	
	// perform the 3 attacks
	game.standardAttack(parent, target);
	game.sleepStandardDelay(1);
	
	if (target.isDead()) return;
	
	game.standardAttack(parent, target);
	game.sleepStandardDelay(1);
	
	if (target.isDead()) return;
	
	game.standardAttack(parent, target);
	
	var effect = targeter.getSlot().createEffect();
	effect.setTitle(targeter.getSlot().getAbility().getName());
	effect.setDuration(1);
	effect.getBonuses().addPenalty('ActionPoint', -80);
	
	var g1 = game.getBaseParticleGenerator("sparkle");
	g1.setDurationInfinite();
	g1.setRotationSpeedDistribution(game.getUniformDistribution(100.0, 200.0));
	g1.setPosition(target.getLocation());
	g1.setBlueDistribution(game.getFixedDistribution(0.0));
	g1.setGreenDistribution(game.getFixedDistribution(0.0));
	effect.addAnimation(g1);
	
	target.applyEffect(effect);
}