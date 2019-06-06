function onActivate(game, slot) {
	if (slot.getParent().abilities.has("LightningBolt")) {
		var creatures = game.ai.getVisibleCreaturesWithinRange(slot.getParent(), "Hostile", 20);

		var targeter = game.createCircleTargeter(slot);
		targeter.setRadius(1);
		targeter.addAllowedCreatures(creatures);
		targeter.activate();
	} else {
		var creatures = game.ai.getTouchableCreatures(slot.getParent(), "Hostile");
	
		var targeter = game.createListTargeter(slot);
		targeter.addAllowedCreatures(creatures);
		targeter.activate();
	}
}

function onTargetSelect(game, targeter) {
	// cast the spell
	targeter.getSlot().activate();

	if (targeter.getSlot().getParent().abilities.has("LightningBolt")) {
	    lightningBolt(game, targeter);
    } else {
	    shock(game, targeter);
    }
}

function lightningBolt(game, targeter) {
	var spell = targeter.getSlot().getAbility();
	var parent = targeter.getParent();
	var casterLevel = parent.stats.getCasterLevel();

	// check for spell failure
	if (!spell.checkSpellFailure(parent)) return;
	
	var center = targeter.getMouseGridPosition();
	var centerScreen = targeter.getMouseScreenPosition();
	
	var g1 = game.getBaseParticleGenerator("explosion");
	g1.setPosition(center);
	g1.setDurationDistribution(game.getFixedDistribution(1.0));
	g1.setAlphaSpeedDistribution(game.getFixedDistribution(-1.0));
	g1.setVelocityDistribution(game.getEquallySpacedAngleDistribution(0.0, 100.0, 5.0, 1000.0, 10.0));
	g1.setGreenSpeedDistribution(game.getUniformDistributionWithBase(game.getSpeedDistributionBase(), -0.005, 0.0, 0.05));
	g1.setRedSpeedDistribution(game.getUniformDistributionWithBase(game.getSpeedDistributionBase(), -0.01, 0.0, 0.05));
	game.runParticleGeneratorNoWait(g1);
	game.lockInterface(g1.getTimeLeft());
	
	var targets = targeter.getAffectedCreatures();
	for (var i = 0; i < targets.size(); i++) {
		var target = targets.get(i);
		
		var damage = game.dice().d12(2) + casterLevel;
		
		var targetPosition = target.getLocation();
		var targetDistance = targetPosition.getScreenDistance(center);
		var targetAngle = center.angleTo(targetPosition.toPoint());
		var speed = 288.0;
		
		var anim = game.getBaseAnimation("lightning");
		
		anim.setPosition(centerScreen.x, centerScreen.y);
		anim.setVelocityMagnitudeAngle(speed, targetAngle);
		anim.setDuration(targetDistance / speed);
		anim.setRotation(targetAngle * 180.0 / 3.14159);
	
		var callback = spell.createDelayedCallback("applyDamage");
		callback.setDelay(anim.getSecondsRemaining());
		callback.addArguments([parent, target, damage, targeter]);
		callback.start();
	
		game.runAnimationNoWait(anim);
		game.lockInterface(anim.getSecondsRemaining());
	}
}

function shock(game, targeter) {
    var spell = targeter.getSlot().getAbility();
	var parent = targeter.getParent();
	var target = targeter.getSelectedCreature();

	// check for spell failure
	if (!spell.checkSpellFailure(parent, target)) return;
	
	// perform the touch attack in a new thread as it will block
	var cb = spell.createDelayedCallback("performTouch");
	cb.addArgument(targeter);
	cb.start();
}

function performTouch(game, targeter) {
	var spell = targeter.getSlot().getAbility();
	var parent = targeter.getParent();
	var target = targeter.getSelectedCreature();
	var casterLevel = parent.stats.getCasterLevel();
	
	var damage = game.dice().d8(2) + casterLevel;
	
	if (game.meleeTouchAttack(parent, target)) {
		// create the callback that will apply damage partway through the animation
		var callback = spell.createDelayedCallback("applyDamage");
		callback.setDelay(0.2);
		callback.addArguments([parent, target, damage, targeter]);
		
		// create the animation
		var anim = game.getBaseAnimation("blast");
		var position = target.getLocation().getCenteredScreenPoint();
		anim.setPosition(position.x, position.y);
		
		game.runAnimationNoWait(anim);
		game.lockInterface(anim.getSecondsRemaining());
		callback.start();
	}
}

function applyDamage(game, parent, target, damage, targeter) {
	var spell = targeter.getSlot().getAbility();

	spell.applyDamage(parent, target, damage, "Electrical");
	
	if (parent.abilities.has("DisablingLightning")) {
		var magnitude = 10 + parent.stats.getCasterLevel();
   
		var effect = targeter.getSlot().createEffect();
		effect.setTitle("Disabling Lightning");
	
		effect.getBonuses().addPenalty("SpellFailure", -magnitude);
		effect.getBonuses().addPenalty("Attack", -magnitude);
		effect.setDuration(3);
	
		target.applyEffect(effect);
	}
}