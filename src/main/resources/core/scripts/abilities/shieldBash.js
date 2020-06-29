function canActivate(game, parent) {
	if (!parent.timer.canAttack()) return false;
	
	return parent.inventory.getEquippedShield() != null;
}

function onActivate(game, slot) {
   var creatures = game.ai.getTouchableCreatures(slot.getParent(), "Hostile");

   var targeter = game.createListTargeter(slot);
   targeter.addAllowedCreatures(creatures);
   targeter.activate();
}

function onTargetSelect(game, targeter) {
	targeter.getSlot().activate();

	var ability = targeter.getSlot().getAbility();

	// perform the attack in a new thread as the melee touch attack will block
	var cb = ability.createDelayedCallback("performAttack");
	cb.addArgument(targeter);
	cb.start(); 
}

function performAttack(game, targeter) {
	var parent = targeter.getParent();
	var target = targeter.getSelectedCreature();

	var improved = parent.abilities.has("ImprovedShieldBash");
	
	parent.timer.performAttack();
	
	// apply a temporary effect with the bonuses
	var parentEffect = parent.createEffect();
	parentEffect.setTitle(targeter.getSlot().getAbility().getName());
	
	if (improved) {
		parentEffect.getBonuses().addBonus('Attack', 'Stackable', 10);
	}
	
	parent.applyEffect(parentEffect);
	
	if (game.meleeTouchAttack(parent, target)) {
		// touch attack succeeded
		
		var checkDC = 50 + 2 * (parent.stats.getStr() - 10);
		
		if (improved) {
			checkDC += parent.stats.getLevelAttackBonus();
		} else {
			checkDC += parent.stats.getLevelAttackBonus() / 2;
		}
		
		if (!target.stats.getPhysicalResistanceCheck(checkDC)) {
			// target failed check
			
			var targetEffect = targeter.getSlot().createEffect();
			
			if (improved) {
				targetEffect.setDuration(3);
			} else {
				targetEffect.setDuration(2);
			}
			
			targetEffect.setTitle(targeter.getSlot().getAbility().getName());
			targetEffect.getBonuses().add("Immobilized");
			targetEffect.getBonuses().add("Helpless");
			targetEffect.addNegativeIcon("items/enchant_death_small");
			
			var g1 = game.getBaseParticleGenerator("sparkle");
			g1.setDurationInfinite();
			g1.setRotationSpeedDistribution(game.getUniformDistribution(100.0, 200.0));
			g1.setPosition(target.getLocation());
			g1.setBlueDistribution(game.getFixedDistribution(0.0));
			g1.setGreenDistribution(game.getFixedDistribution(0.0));
			targetEffect.addAnimation(g1);
			
			target.applyEffect(targetEffect);
			
			game.addMessage("red", parent.getName() + " stuns " + target.getName() + ".");
		} else {
			game.addMessage("red", parent.getName() + " fails to stun " + target.getName() + ".");
		}
	} else {
		game.addMessage("red", parent.getName() + " misses shield bash attempt.");
	}
	
	parent.removeEffect(parentEffect);
}