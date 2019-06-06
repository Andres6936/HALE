function onActivate(game, slot) {
	var ability = slot.getAbility();
	var parent = slot.getParent();
	
	var lvls = parent.roles.getLevel("Berserker");
	
	var duration = 3 + parseInt(lvls / 2);
	
	if (parent.abilities.has("ImprovedRage"))
		duration += 2;
	
	if (parent.abilities.has("EpicRage"))
		var strBonus = 8;
	else if (parent.abilities.has("ImprovedRage"))
		var strBonus = 6;
	else
		var strBonus = 3;
	
	slot.setActiveRoundsLeft(duration);
	slot.activate();
	
	var effect = slot.createEffect("effects/rage");
	effect.setDuration(duration);
	effect.addPositiveIcon("items/enchant_damage_small");
	effect.addPositiveIcon("items/enchant_strength_small");
	effect.setTitle(ability.getName());
	
	effect.getBonuses().addBonus('Str', 'Stackable', strBonus);
	
	effect.getBonuses().addBonus('OneHandedMeleeWeaponDamage', 'Morale', 25);
	effect.getBonuses().addBonus('TwoHandedMeleeWeaponDamage', 'Morale', 25);
	
	if (parent.abilities.has("UnstoppableRage")) {
		effect.getBonuses().add('ImmobilizationImmunity');
		effect.getBonuses().add('CriticalHitImmunity');
		
		effect.getBonuses().addBonus('SpellResistance', 10 + 2 * lvls);
	}
	
	if (parent.abilities.has("EpicRage")) {
		effect.getBonuses().addBonus('ActionPoint', 5 + 2 * lvls);
	}
	
	effect.getBonuses().addPenalty('Attack', 'Stackable', -25);
	effect.getBonuses().addPenalty('ArmorClass', 'Stackable', -25);
	
	var anim = game.getBaseAnimation("rune");
	anim.addFrames("animations/rune1-", 1, 4);
	anim.setDurationInfinite();
	var position = parent.getLocation().getCenteredScreenPoint();
	anim.setPosition(position.x, position.y + 15.0);
	effect.addAnimation(anim);
	
	parent.applyEffect(effect);
	
	var anim = game.getBaseAnimation("blast");
	var position = parent.getLocation().getCenteredScreenPoint();
	anim.setPosition(position.x, position.y - 20);
	anim.setRed(1.0);
	anim.setGreen(0.2);
	anim.setBlue(0.2);
		
	game.runAnimationNoWait(anim);
	game.lockInterface(anim.getSecondsRemaining());
}
