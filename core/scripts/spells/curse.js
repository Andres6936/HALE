function onActivate(game, slot) {
	if (slot.getParent().abilities.has("MassCurse")) {
		var targeter = game.createCircleTargeter(slot);
		targeter.setRadius(4);
		targeter.setRelationshipCriterion("Hostile");
		targeter.addAllowedPoint(slot.getParent().getLocation());
		targeter.activate();
	} else {
		var creatures = game.ai.getTouchableCreatures(slot.getParent(), "Hostile");
	
		var targeter = game.createListTargeter(slot);
		targeter.addAllowedCreatures(creatures);
		targeter.activate();
	}
}

function onTargetSelect(game, targeter) {
	var spell = targeter.getSlot().getAbility();
	var parent = targeter.getParent();
	var duration = game.dice().randInt(4, 8);
	
	targeter.getSlot().setActiveRoundsLeft(duration);
	targeter.getSlot().activate();
	
	if (parent.abilities.has("MassCurse")) {
		if (!spell.checkSpellFailure(parent)) return;
	
		var targets = targeter.getAffectedCreatures();
		
		for (var i = 0; i < targets.size(); i++) {
			var target = targets.get(i);
			
			applyCurse(game, targeter, target, duration);
		}
		
		var targetsCursed = targets.size();
		
		if (parent.abilities.has("Drain")) {
			targeter.setRelationshipCriterion("Friendly");
			for (var i = 0; i < targets.size() && i < targetsCursed; i++) {
				var target = targets.get(i);
				
				bolsterAlly(game, targeter, target, duration);
			}
		}
		
	} else {
		var target = targeter.getSelectedCreature();
	
		if (!spell.checkSpellFailure(parent, target)) return;
	
		// perform the touch attack in a new thread as it will block
		var cb = spell.createDelayedCallback("performTouch");
		cb.addArgument(targeter);
		cb.addArgument(duration);
		cb.start();
	}
}

function performTouch(game, targeter, duration) {
	var parent = targeter.getParent();
	var target = targeter.getSelectedCreature();
	
	if (!game.meleeTouchAttack(parent, target)) return;
	
	applyCurse(game, targeter, target, duration);
	
	if (parent.abilities.has("Drain"))
		bolsterAlly(game, targeter, parent, duration);
}

function bolsterAlly(game, targeter, target, duration) {
	var spell = targeter.getSlot().getAbility();
	var parent = targeter.getParent();
	var casterLevel = parent.stats.getCasterLevel();
	
	if (parent.abilities.has("Enfeeble")) {
		var attrPenalty = 1 + parseInt(casterLevel / 6);
	
		var effect = targeter.getSlot().createEffect();
		effect.setDuration(duration);
		effect.setTitle(spell.getName() + " Drain");
		effect.getBonuses().addBonus('Con', attrPenalty);
		effect.getBonuses().addBonus('Dex', attrPenalty);
		effect.addPositiveIcon("items/enchant_constitution_small");
		effect.addPositiveIcon("items/enchant_dexterity_small");
		
		var g1 = game.getBaseParticleGenerator("sparkle");
		g1.setDuration(1.0);
		g1.setRotationSpeedDistribution(game.getUniformDistribution(100.0, 200.0));
		g1.setPosition(target.getLocation());
		g1.setRedDistribution(game.getFixedDistribution(0.0));
		g1.setBlueDistribution(game.getFixedDistribution(0.0));
		g1.setGreenDistribution(game.getFixedDistribution(1.0));
		effect.addAnimation(g1);
		
		target.applyEffect(effect);
	}
}

function applyCurse(game, targeter, target, duration) {
	var spell = targeter.getSlot().getAbility();
	var parent = targeter.getParent();
	var casterLevel = parent.stats.getCasterLevel();

	var acPenalty = -10 - casterLevel;
	
	var effect = targeter.getSlot().createEffect();
	effect.setDuration(duration);
	effect.setTitle(spell.getName());
	effect.getBonuses().addPenalty('ArmorClass', 'Stackable', acPenalty);
	effect.addNegativeIcon("items/enchant_armor_small");
	
	if (parent.abilities.has("Enfeeble")) {
		var attrPenalty = -1 - parseInt(casterLevel / 6);
	
		effect.getBonuses().addPenalty('Con', attrPenalty);
		effect.getBonuses().addPenalty('Dex', attrPenalty);
		effect.addNegativeIcon("items/enchant_constitution_small");
		effect.addNegativeIcon("items/enchant_dexterity_small");
	}
	
	var anim = game.getBaseAnimation("rune");
	anim.addFrames("animations/rune2-", 1, 4);
	anim.setDurationInfinite();
	var position = target.getLocation().getCenteredScreenPoint();
	anim.setPosition(position.x, position.y + 15.0);
	effect.addAnimation(anim);
	   
	target.applyEffect(effect);
}