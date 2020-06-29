function onActivate(game, slot) {
	var targeter = game.createListTargeter(slot);
	targeter.addAllowedCreature(slot.getParent());
	targeter.activate();
}

function onTargetSelect(game, targeter) {
	var slot = targeter.getSlot();
	var parent = slot.getParent();

	var lvls = parent.roles.getLevel("Ranger");

	if (parent.abilities.has("GreaterFocus"))
		lvls = lvls * 2;
	
	var duration = parseInt(3 + lvls / 3);
	
	var effect = slot.createEffect();
	effect.addPositiveIcon("items/enchant_attack_small");
	effect.addPositiveIcon("items/enchant_damage_small");
	effect.setDuration(duration);
	effect.setTitle(slot.getAbility().getName());
	
	if (parent.abilities.has("DefensiveFocus"))
		effect.getBonuses().addBonus('ArmorClass', 'Stackable', 10 + lvls);
	
	effect.getBonuses().addBonus('RangedAttack', 10 + lvls);
	effect.getBonuses().addBonus('RangedDamage', 20 + 2 * lvls);
	slot.getParent().applyEffect(effect);

	slot.setActiveRoundsLeft(duration);
	slot.activate();
}
