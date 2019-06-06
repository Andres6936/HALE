function onActivate(game, slot) {
	var creatures = game.ai.getVisibleCreaturesWithinRange(slot.getParent(), "Hostile", 40);

	var targeter = game.createListTargeter(slot);
	targeter.addAllowedCreatures(creatures);
	targeter.activate();
}

function onTargetSelect(game, targeter) {
    // cast the spell
	var duration = 3;
	
	targeter.getSlot().setActiveRoundsLeft(duration);
    targeter.getSlot().activate();

    var spell = targeter.getSlot().getAbility();
    var parent = targeter.getParent();
    var target = targeter.getSelectedCreature();
    var casterLevel = parent.stats.getCasterLevel();
	
	// check for spell failure
    if (!spell.checkSpellFailure(parent, target)) return;

	var minDamage = casterLevel + 1;
	var maxDamage = casterLevel + 8;
	
	var callback = spell.createDelayedCallback("applyDamage");
	callback.setDelay(0.75);
	callback.addArguments([parent, target, game.dice().rand(minDamage, maxDamage), spell]);
	callback.start();
	
	var effect = targeter.getSlot().createEffect("effects/damageOverTime");
	effect.setTitle("Acid Splash");
	effect.put("minDamagePerRound", minDamage);
	effect.put("maxDamagePerRound", maxDamage);
	effect.put("damageType", "Acid");
	effect.setDuration(duration);
	effect.addNegativeIcon("items/enchant_acid_small");
	target.applyEffect(effect);
	
	var generator = game.createParticleGenerator("Rect", "Burst", "particles/circle17", 100.0);
	generator.setVelocityDistribution(game.getFixedAngleDistribution(25.0, 50.0, 3.14159 / 2.0));
	generator.setRectBounds(-22.0, 22.0, -30.0, -15.0);
	generator.setDuration(1.5);
	generator.setPosition(target.getLocation());
	generator.setGreenDistribution(game.getFixedDistribution(1.0));
	generator.setAlphaDistribution(game.getUniformDistribution(0.8, 1.0));
	generator.setAlphaSpeedDistribution(game.getFixedDistribution(-0.9));
	game.runParticleGeneratorNoWait(generator);
	game.lockInterface(generator.getTimeLeft());
}

function applyDamage(game, parent, target, damage, spell) {
    spell.applyDamage(parent, target, damage, "Acid");
}