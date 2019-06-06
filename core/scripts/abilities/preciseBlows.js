function onActivate(game, slot) {
	var effect = slot.createEffect();
	effect.addPositiveIcon("items/enchant_attack_small");
	effect.setTitle(slot.getAbility().getName());
	
	var parent = slot.getParent();
	
	effect.getBonuses().addBaseWeaponBonus("Unarmed", "CriticalChance", parent.roles.getLevel("Monk") + (parent.stats.getWis() - 10));
	effect.getBonuses().addBaseWeaponPenalty("Unarmed", "Damage", -20);
	
	effect.setRemoveOnDeactivate(true);
	slot.getParent().applyEffect(effect);
	slot.activate();
}

function onDeactivate(game, slot) {
	slot.deactivate();
}