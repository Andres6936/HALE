function onActivate(game, slot) {
	var creatures = game.ai.getVisibleCreaturesWithinRange(slot.getParent(), "Hostile", 50);

	var targeter = game.createListTargeter(slot);
	targeter.addAllowedCreatures(creatures);
	targeter.activate();
}

function onTargetSelect(game, targeter) {
	var spell = targeter.getSlot().getAbility();
	var parent = targeter.getParent();
	var target = targeter.getSelectedCreature();
	var casterLevel = parent.stats.getCasterLevel();
	
	var duration = game.dice().randInt(5, 10);
	
	targeter.getSlot().setActiveRoundsLeft(duration);
	targeter.getSlot().activate();
	
	// don't use the targeted version of checkSpellFailure since
	// breach ignores concealment
	if (!spell.checkSpellFailure(parent)) return;
	
	var targetCenter = targeter.getMouseGridPosition();
	
	var g1 = game.getBaseParticleGenerator("ray");
	g1.setVelocityDurationBasedOnSpeed(parent.getLocation().toPoint(), targetCenter, 600.0);
	
	var g2 = game.getBaseParticleGenerator("sparkle");
	g2.setDuration(1.0);
	g2.setRotationSpeedDistribution(game.getUniformDistribution(100.0, 200.0));
	g1.addSubGeneratorAtEnd(g2);
	
	game.runParticleGeneratorNoWait(g1);
	game.lockInterface(g1.getTimeLeft());
	
	var callback = spell.createDelayedCallback("applyEffects");
	callback.setDelay(g1.getTimeLeft());
	callback.addArguments([targeter, duration]);
	callback.start();
}

function applyEffects(game, targeter, duration) {
	applySpellResistanceEffect(game, targeter, duration);
	breachDefense(game, targeter);
}

function applySpellResistanceEffect(game, targeter, duration) {
	var spell = targeter.getSlot().getAbility();
	var parent = targeter.getParent();
	var target = targeter.getSelectedCreature();
	var casterLevel = parent.stats.getCasterLevel();

	var lvls = parent.roles.getLevel("Avenger");
	
	var effect = targeter.getSlot().createEffect();
	effect.setDuration(duration);
	effect.setTitle(spell.getName());
	
	effect.getBonuses().addPenalty("SpellResistance", -casterLevel - 3 * lvls);
	
	target.applyEffect(effect);
}

function breachDefense(game, targeter) {
	var target = targeter.getSelectedCreature();
	
	var lvls = targeter.getParent().roles.getLevel("Avenger");
	
	// go through the spells in a random order until we find one that is in effect
	var spellsToRemove = shuffle( [ "HardenArmor", "AbsorbEnergy", "DeflectProjectiles", "Shield", "Chameleon", "Ward", "FortifyHealth", "LayerOfBark", "EnergyImmunity", "Invulnerability" ] );
	
	var spellsRemoved = 0;
	var totalToRemove = 2;
	
	if (lvls > 0) totalToRemove++;
	if (lvls > 4) totalToRemove++;
	if (lvls > 9) totalToRemove++;
	
	for (var i = 0; i < spellsToRemove.length; i++) {
		var spellID = spellsToRemove[i];
		
		// check for harden armor on the armor, all others on the target itself
		if (spellID.equals("HardenArmor")) {
			var effectTarget = target.inventory.getEquippedArmor();
	
			// if target is not wearing armor, then harden armor cannot be in effect
			if (effectTarget == null) continue;
		} else {
			var effectTarget = target;
		}
		
		var effect = effectTarget.getEffects().getEffectCreatedBySlot(spellID);
		
		if (effect != null) {
			// we found a valid effect and can remove it
			effectTarget.removeEffect(effect);
			spellsRemoved++;
			
			if (spellsRemoved == totalToRemove) return;
		}
	}
}

function shuffle(array) {
    var tmp, current, top = array.length;

    if (top) while(--top) {
        current = Math.floor(Math.random() * (top + 1));
        tmp = array[current];
        array[current] = array[top];
        array[top] = tmp;
    }

    return array;
}
