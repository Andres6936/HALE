function onActivate(game, slot) {
	if (!game.addMenuLevel("Absorb Energy")) return;

	var types = ["Fire", "Cold", "Electrical", "Acid"];
	
	for (var index = 0; index < types.length; index++ ) {
		var type = types[index];
	
		var cb = game.createButtonCallback(slot, "castSpell");
		cb.addArgument(type);
		
		game.addMenuButton(type, cb);
	}
	
	game.showMenu();
}

function castSpell(game, slot, type) {
	var creatures = game.ai.getTouchableCreatures(slot.getParent(), "Friendly");
	
	var targeter = game.createListTargeter(slot);
	targeter.addAllowedCreatures(creatures);
	targeter.addCallbackArgument(type);
	targeter.activate();
}

function onTargetSelect(game, targeter, type) {
	var spell = targeter.getSlot().getAbility();
	var parent = targeter.getParent();
	var target = targeter.getSelectedCreature();
	var casterLevel = parent.stats.getCasterLevel();
	
	var duration = game.dice().randInt(5, 10);
	
	targeter.getSlot().setActiveRoundsLeft(duration);
	targeter.getSlot().activate();
	
	if (!spell.checkSpellFailure(parent, target)) return;
	
	var dr = parseInt(5 + casterLevel / 2);
	
	var effect = targeter.getSlot().createEffect();
	effect.setDuration(duration);
	effect.setTitle(spell.getName());
	effect.getBonuses().addDamageReduction(type, dr);
	
	if (parent.abilities.has("ResistSpells")) {
		effect.getBonuses().addBonus("SpellResistance", 15 + casterLevel);
	}
	
	var g1 = game.getBaseParticleGenerator("rotatingRing");
	g1.setDurationInfinite();
	g1.setPosition(target.getLocation());
	
	if (type == "Fire") {
		effect.addPositiveIcon("items/enchant_fire_small");
		g1.setBlueDistribution(game.getFixedDistribution(0.0));
		g1.setGreenDistribution(game.getFixedDistribution(0.6));
	} else if (type == "Cold") {
		effect.addPositiveIcon("items/enchant_ice_small");
		// default white color is fine
	} else if (type == "Electrical") {
		effect.addPositiveIcon("items/enchant_lightning_small");
		g1.setRedDistribution(game.getFixedDistribution(0.0));
		g1.setGreenDistribution(game.getFixedDistribution(0.0));
	} else if (type == "Acid") {
		effect.addPositiveIcon("items/enchant_acid_small");
		g1.setRedDistribution(game.getFixedDistribution(0.0));
		g1.setBlueDistribution(game.getFixedDistribution(0.0));
	}

	effect.addAnimation(g1);	
	target.applyEffect(effect);
}
