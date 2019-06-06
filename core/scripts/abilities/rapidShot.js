function onActivate(game, slot) {
	var effect = slot.createEffect();
	effect.addPositiveIcon("items/enchant_actionPoints_small");
	effect.setTitle(slot.getAbility().getName());
	
	effect.getBonuses().addBaseWeaponBonus("Longbow", "Speed", +15);
	effect.getBonuses().addBaseWeaponBonus("Shortbow", "Speed", +15);
	effect.getBonuses().addBaseWeaponBonus("Sling", "Speed", +15);
	
	effect.getBonuses().addBaseWeaponPenalty("Longbow", "Attack", -20);
	effect.getBonuses().addBaseWeaponPenalty("Shortbow", "Attack", -20);
	effect.getBonuses().addBaseWeaponPenalty("Sling", "Attack", -20);
	
	effect.setRemoveOnDeactivate(true);
	slot.getParent().applyEffect(effect);
	slot.activate();
}

function onDeactivate(game, slot) {
	slot.deactivate();
}