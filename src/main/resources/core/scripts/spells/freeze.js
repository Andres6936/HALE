function onActivate(game, slot) {
	if (slot.getParent().abilities.has("GlacialWave")) {
		var targeter = game.createCircleTargeter(slot);
		targeter.setRadius(5);
		targeter.addAllowedPoint(slot.getParent().getLocation());
		targeter.activate();
	} else {
		var creatures = game.ai.getVisibleCreaturesWithinRange(slot.getParent(), "Hostile", 25);

		var targeter = game.createListTargeter(slot);
		targeter.addAllowedCreatures(creatures);
		targeter.activate();
	}
}

function onTargetSelect(game, targeter) {
	// cast the spell
	targeter.getSlot().activate();

	if (targeter.getSlot().getParent().abilities.has("GlacialWave")) {
	    glacialWave(game, targeter);
    } else {
	    freeze(game, targeter);
    }
}

function glacialWave(game, targeter) {
	var spell = targeter.getSlot().getAbility();
	var parent = targeter.getParent();
	var casterLevel = parent.stats.getCasterLevel();
	
	if (!spell.checkSpellFailure(parent)) return;
	
	var g1 = game.getBaseParticleGenerator("ring");
	g1.setStopParticlesAtOpaque(false);
	g1.setPosition(parent.getLocation());
	g1.setVelocityDistribution( game.getEquallySpacedAngleDistribution(360.0, 360.0, 0.0, g1.getNumParticles(), 0.0) );
	
	var creatures = targeter.getAffectedCreatures();
	for (var i = 0; i < creatures.size(); i++) {
		var target = creatures.get(i);
		
		if ( target == parent ) continue;
		
		// apply the effect when the ring reaches this target
		var delay = target.getLocation().getScreenDistance(parent.getLocation()) / 360.0;
		
		var cb = spell.createDelayedCallback("applyGlacialWave");
		cb.setDelay(delay);
		cb.addArguments([parent, target, targeter.getSlot()]);
		cb.start();
	}
	
	game.runParticleGeneratorNoWait(g1);
	game.lockInterface(g1.getTimeLeft());
}

function applyGlacialWave(game, parent, target, slot) {
	var casterLevel = parent.stats.getCasterLevel();
	var spell = slot.getAbility();
	var duration = game.dice().d2(2);
	
	var isHostile = parent.getFaction().isHostile(target);
	
	// apply the damage
	var damage = game.dice().d6(2) + casterLevel;
	
	if (!isHostile) {
		damage = parseInt(damage / 2);
	}
	
	spell.applyDamage(parent, target, damage, "Cold");
	
	// apply additional negative effects to hostile targets
	if (!target.isDead() && isHostile) {
		// apply the ap penalty effect
		var effect = slot.createEffect();
		effect.setTitle(spell.getName());
		effect.setDuration(duration);
		effect.getBonuses().addPenalty("ActionPoint", -15 - casterLevel);
		effect.addNegativeIcon("items/enchant_actionPoints_small");
		
		// apply the paralysis if the target fails the check
		if ( !target.stats.getPhysicalResistanceCheck(spell.getCheckDifficulty(parent)) ) {
			effect.getBonuses().add("Immobilized");
			effect.getBonuses().add("Helpless");
		
			var position = target.getLocation().getCenteredScreenPoint();
		
			var g1 = game.getBaseParticleGenerator("paralysis");
			var g2 = game.getBaseParticleGenerator("paralysis");
	
			g1.setPosition(position.x, position.y - 10.0);
			g2.setPosition(position.x, position.y + 10.0);
		
			g2.setLineStart(-18.0, 0.0);
			g2.setLineEnd(18.0, 0.0);
			g2.setRedDistribution(game.getFixedDistribution(0.6));

			effect.addAnimation(g1);
			effect.addAnimation(g2);
		}
		
		target.applyEffect(effect);
	}
}

function freeze(game, targeter) {
	var spell = targeter.getSlot().getAbility();
	var parent = targeter.getParent();
	var target = targeter.getSelectedCreature();
	var casterLevel = parent.stats.getCasterLevel();

	// check for spell failure
	if (!spell.checkSpellFailure(parent, target)) return;
	
	var pos = target.getLocation().getCenteredScreenPoint();
	
	var anim = game.getBaseAnimation("iceCrystal");
	var position = target.getLocation().getCenteredScreenPoint();
	anim.setPosition(position.x, position.y);
	game.runAnimationNoWait(anim);
   
	// create the callback that will apply damage at the appropriate time
	var callback = spell.createDelayedCallback("applyFreeze");
	callback.setDelay(0.4);
	callback.addArgument(targeter);
   
	// run the particle effect and start the callback timer
	callback.start();
	game.lockInterface(anim.getSecondsRemaining());
}

function applyFreeze(game, targeter) {
	var spell = targeter.getSlot().getAbility();
	var parent = targeter.getParent();
	var target = targeter.getSelectedCreature();
	var casterLevel = parent.stats.getCasterLevel();

	// compute the amount of damage to apply
	var damage = game.dice().d10(1) + casterLevel;
	
	spell.applyDamage(parent, target, damage, "Cold");
   
	if ( !target.stats.getPhysicalResistanceCheck(spell.getCheckDifficulty(parent)) ) {
		var effect = targeter.getSlot().createEffect();
		effect.setTitle(spell.getName());
		effect.setDuration(3);
		effect.getBonuses().addPenalty("ActionPoint", -15 - casterLevel);
		target.applyEffect(effect);
	}
}