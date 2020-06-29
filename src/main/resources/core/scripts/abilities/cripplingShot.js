function canActivate(game, parent) {
	if (!parent.timer.canAttack()) return false;
	
	var weapon = parent.getMainHandWeapon();
	
	return !weapon.isMelee();
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
	
	// apply a temporary effect with the bonuses
	var effect = parent.createEffect();
	
	if (parent.abilities.has("DevastatingShot")) {
		effect.getBonuses().addBonus('Attack', 'Stackable', 40);
		effect.getBonuses().addBonus('Damage', 'Stackable', 160);
	} else {
		effect.getBonuses().addBonus('Attack', 'Stackable', 20);
	}
	
	parent.applyEffect(effect);

	// perform the attack
	if (game.standardAttack(parent, target)) {
		var checkDC = 50 + 2 * (parent.stats.getDex() - 10) + parent.stats.getLevelAttackBonus() / 2;
		
		// check the crippling shot effect
		if (!target.stats.getReflexResistanceCheck(checkDC)) {
			var effect2 = parent.createEffect();
			effect2.setDuration(3);
			effect2.addNegativeIcon("items/enchant_fastMovement_small");
			effect2.setTitle(targeter.getSlot().getAbility().getName());
			effect2.getBonuses().add('Immobilized');
			
			var g1 = game.getBaseParticleGenerator("sparkle");
			g1.setDurationInfinite();
			g1.setRotationSpeedDistribution(game.getUniformDistribution(100.0, 200.0));
			g1.setPosition(target.getLocation());
			g1.setBlueDistribution(game.getFixedDistribution(0.0));
			g1.setGreenDistribution(game.getFixedDistribution(0.0));
			effect2.addAnimation(g1);
			
			target.applyEffect(effect2);
		} else {
			var effect2 = parent.createEffect();
			effect2.setDuration(3);
			effect2.addNegativeIcon("items/enchant_fastMovement_small");
			effect2.setTitle(targeter.getSlot().getAbility().getName());
			effect2.getBonuses().addPenalty('Movement', -50);
			target.applyEffect(effect2);
		}
	}

	parent.removeEffect(effect);
}