function enableStance(game, slot, stance) {
	var effect = slot.createEffect("effects/stormStanceEffect");
	effect.setTitle(slot.getAbility().getName() + " - " + stance);
	
	var parent = slot.getParent();
	var level = parent.roles.getLevel("Storm Hand");
	var wisBonus = parent.stats.getWis() - 10;
	
	if (stance == "Burning Palm") {
		effect.addIcon("items/enchant_fire_small");
		effect.getBonuses().addStandaloneDamageBonus("Fire", 1 + level / 3, 3 + level / 3);
		effect.getBonuses().addDamageReduction("Cold", "Stackable", 4);
		
		effect.put("stance", "Burning Palm");
		
	} else if (stance == "Flowing Body") {
		effect.addIcon("items/enchant_ice_small");
		effect.getBonuses().addBonus("Attack", "Stackable", 2 * (level + wisBonus));
		effect.getBonuses().addBonus("ArmorClass", "Stackable", 2 * (level + wisBonus));
		
		if (parent.abilities.has("TideWalker")) {
			effect.getBonuses().addBonus("ActionPoint", "Stackable", (level + wisBonus));
			effect.getBonuses().addBonus("Movement", "Stackable", (level + wisBonus));
		}
		
		effect.put("stance", "Flowing Body");
	} else if (stance == "Stone Form") {
		effect.addIcon("items/enchant_physical_small");
		effect.getBonuses().addDamageReduction("Physical", "Stackable", level + wisBonus);
		
		if (parent.abilities.has("StrengthOfTheEarth")) {
			effect.getBonuses().addBonus("Con", "Stackable", wisBonus);
			effect.addPositiveIcon("items/enchant_spellHealing_small");
		}
		
		effect.put("stance", "Stone Form");
	} else if (stance == "Hurricane Claw") {
		effect.addIcon("items/enchant_lightning_small");
		effect.getBonuses().addBonus("CriticalChance", level + wisBonus);
		effect.getBonuses().addBonus("CriticalMultiplier", 1);
		effect.getBonuses().addDamageReduction("Acid", "Stackable", 5 + wisBonus);
		effect.getBonuses().addDamageReduction("Cold", "Stackable", 5 + wisBonus);
		effect.getBonuses().addDamageReduction("Fire", "Stackable", 5 + wisBonus);
		
		effect.put("stance", "Hurricane Claw");
	}
	
	effect.setRemoveOnDeactivate(true);
	slot.getParent().applyEffect(effect);
	slot.activate();
}

function addButton(game, slot, name) {
	var cb = game.createButtonCallback(slot, "enableStance");
	cb.addArgument(name);
	game.addMenuButton(name, cb);
}

function onActivate(game, slot) {
	var abilities = slot.getParent().abilities;

	if (!game.addMenuLevel("Storm Stance")) return;
	
	if (abilities.has("BurningPalm"))
		addButton(game, slot, "Burning Palm");
	
	if (abilities.has("FlowingBody"))
		addButton(game, slot, "Flowing Body");
	
	if (abilities.has("StoneForm"))
		addButton(game, slot, "Stone Form");
	
	if (abilities.has("HurricaneClaw"))
		addButton(game, slot, "Hurricane Claw");
	
	game.showMenu();
}

function onDeactivate(game, slot) {
   slot.deactivate();
}
