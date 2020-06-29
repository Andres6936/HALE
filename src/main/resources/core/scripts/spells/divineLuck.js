function onActivate(game, slot) {
	var targeter = game.createCircleTargeter(slot);
	targeter.setRadius(4);
	targeter.setRelationshipCriterion("Friendly");
	targeter.addAllowedPoint(slot.getParent().getLocation());
	targeter.activate();
}

function onTargetSelect(game, targeter) {
	var spell = targeter.getSlot().getAbility();
	var parent = targeter.getParent();
	var casterLevel = parent.stats.getCasterLevel();
	
	var duration = parseInt(game.dice().randInt(5, 10));
	
	targeter.getSlot().setActiveRoundsLeft(duration);
	targeter.getSlot().activate();
	
	if (!spell.checkSpellFailure(parent)) return;
	
	if (parent.abilities.has("DivineAura")) {
		// do the divine luck aura
		
		var aura = targeter.getSlot().createAura("effects/divineAura");
		aura.setHasDescription(false);
		aura.setAuraMaxRadius(4);
		aura.setDuration(duration);
	
		// the flashing cross effect
		var g1 = game.getBaseParticleGenerator("haloCross");
		g1.setPosition(parent.getLocation());
		g1.setDurationInfinite();
		aura.addAnimation(g1);
	
		parent.applyEffect(aura);
		
	} else {
		// do the normal divine luck spell with optional "resistance"
	
		var attackBonus = 5 + casterLevel;
		var damageBonus = 10 + 2 * casterLevel;
		var resistanceBonus = 10 + casterLevel;
	
		var creatures = targeter.getAffectedCreatures();
		for (var i = 0; i < creatures.size(); i++) {
			var effect = targeter.getSlot().createEffect();
			effect.setDuration(duration);
			effect.addPositiveIcon("items/enchant_attack_small");
			effect.addPositiveIcon("items/enchant_damage_small");
			effect.setTitle(spell.getName());
			effect.getBonuses().addBonus('Attack', 'Morale', attackBonus);
			effect.getBonuses().addBonus('Damage', 'Morale', damageBonus);
		
			if (parent.abilities.has("Resistance")) {
				effect.getBonuses().addBonus('MentalResistance', 'Morale', resistanceBonus);
				effect.getBonuses().addBonus('PhysicalResistance', 'Morale', resistanceBonus);
				effect.getBonuses().addBonus('ReflexResistance', 'Morale', resistanceBonus);
			}
		
			// create the animation
			var anim = game.getBaseAnimation("sparkleAnim");
			var position = creatures.get(i).getLocation().getCenteredScreenPoint();
			anim.setPosition(position.x, position.y);
			game.runAnimationNoWait(anim);
	   
			creatures.get(i).applyEffect(effect);
		}
		
		game.lockInterface(anim.getSecondsRemaining());
	}
}
