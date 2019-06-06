function canActivate(game, parent) {
	if (!parent.timer.canAttack()) return false;
	
	var weapon = parent.getMainHandWeapon();
	
	return weapon.isMelee();
}

function onActivate(game, slot) {
	var creatures = game.ai.getAttackableCreatures(slot.getParent());

	var targeter = game.createListTargeter(slot);
	targeter.addAllowedCreatures(creatures);
	targeter.activate();
}

function onTargetSelect(game, targeter) {
	targeter.getSlot().setActiveRoundsLeft(2);
	targeter.getSlot().activate();

	var parent = targeter.getParent();
	var target = targeter.getSelectedCreature();
	
	var rogueLevel = parent.roles.getLevel("Rogue");
	
	var checkDC = 50 + 2 * (parent.stats.getInt() - 10) + 3 * rogueLevel;
	
	if ( target.stats.getMentalResistanceCheck(checkDC) )
		return;
	
	var effect = targeter.getSlot().createEffect();
	effect.setDuration(2);
	effect.addNegativeIcon("items/enchant_armor_small");
	effect.setTitle(targeter.getSlot().getAbility().getName());
	effect.getBonuses().addPenalty("ArmorClass", "Stackable", -20 - rogueLevel);
	
	var g1 = game.getBaseParticleGenerator("sparkle");
	g1.setDurationInfinite();
	g1.setRotationSpeedDistribution(game.getUniformDistribution(100.0, 200.0));
	g1.setPosition(target.getLocation());
	g1.setBlueDistribution(game.getFixedDistribution(0.0));
	g1.setGreenDistribution(game.getFixedDistribution(0.0));
	effect.addAnimation(g1);
	
	parent.timer.performAttack();
	
	target.applyEffect(effect);
}