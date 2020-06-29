function canActivate(game, parent) {
	return parent.inventory.getEquippedShield() != null;
}

function onActivate(game, slot) {
	var parent = slot.getParent();
	
	slot.setActiveRoundsLeft(1);
	
	var effect = slot.createEffect();
	effect.addPositiveIcon("items/enchant_armor_small");
	effect.setTitle(slot.getAbility().getName());
	effect.getBonuses().addBonus('ArmorClass', 'Stackable', 40);
	parent.applyEffect(effect);
	
	slot.activate();
}
