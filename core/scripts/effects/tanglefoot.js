function onTargetEnter(game, target, effect) {
	var spell = effect.getSlot().getAbility();
	var parent = effect.getSlot().getParent();
	
	// deadly and crushing vines only affect hostiles
	if (!parent.getFaction().isHostile(target)) return;
	
	checkCrushingVines(game, target, spell, parent, effect);
	
	checkDeadlyVines(game, target, spell, parent);
}

function checkDeadlyVines(game, target, spell, parent) {
	if (!parent.abilities.has("DeadlyVines")) return;

	var casterLevel = parent.stats.getCasterLevel();
	
	var damage = parseInt(game.dice().d6() + casterLevel / 4);
	
	spell.applyDamage(parent, target, damage, "Piercing");
}

function checkCrushingVines(game, target, spell, parent, parentEffect) {
	if (!parent.abilities.has("CrushingVines")) return;
	
	// 50% chance of crushing vines
	if (game.dice().d2() == 1) return;
	
	if ( target.stats.getPhysicalResistanceCheck(spell.getCheckDifficulty(parent)) ) return;
	
	var effect = parentEffect.getSlot().createEffect();
	effect.setDuration(1);
	effect.setTitle("Crushing Vines");
	effect.addNegativeIcon("items/enchant_fastMovement_small");
	effect.getBonuses().add("Immobilized");
			
	var position = target.getLocation().getCenteredScreenPoint();
		
	var g2 = game.getBaseParticleGenerator("paralysis");
	g2.setPosition(position.x, position.y + 10.0);
	g2.setLineStart(-18.0, 0.0);
	g2.setLineEnd(18.0, 0.0);
	g2.setRedDistribution(game.getFixedDistribution(0.6));
	effect.addAnimation(g2);
			
	target.applyEffect(effect);
	
	spell.applyDamage(parent, target, game.dice().d6(), "Blunt");
}

function onRoundElapsed(game, effect) {
	var spell = effect.getSlot().getAbility();
	var parent = effect.getSlot().getParent();
	
	var creatures = effect.getTarget().getAffectedCreatures(effect);
	for (var i = 0; i < creatures.size(); i++) {
		var target = creatures.get(i);
		
		// deadly and crushing vines only affect hostiles
		if (!parent.getFaction().isHostile(target)) continue;
		
		checkCrushingVines(game, target, spell, parent, effect);
		
		checkDeadlyVines(game, target, spell, parent);
	}
}