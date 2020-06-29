function onActivate(game, slot) {
	var effect = slot.createEffect();
	effect.addPositiveIcon("items/enchant_damage_small");
	effect.setTitle(slot.getAbility().getName());
	
	var parent = slot.getParent();
	
	effect.getBonuses().addBaseWeaponBonus("Unarmed", "Damage", 3 * parent.roles.getLevel("Monk") + 3 * (parent.stats.getWis() - 10));
	effect.getBonuses().addBaseWeaponPenalty("Unarmed", "CriticalChance", -4);
	effect.getBonuses().addBaseWeaponPenalty("Unarmed", "Attack", -20);
	
	effect.setRemoveOnDeactivate(true);
	slot.getParent().applyEffect(effect);
	slot.activate();
}

function onDeactivate(game, slot) {
	slot.deactivate();
}