function onActivate(game, slot) {
    if (!game.addMenuLevel("Conjure Weapon")) return;
	
	var ids = [ "dagger", "shortSword", "longsword", "greatsword", "mace",
	    "morningstar", "lighthammer", "warhammer", "maul", "javelin",
		"shortspear", "longspear", "longbow", "shortbow", "handaxe",
		"battleaxe", "greataxe", "quarterstaff", "sling", "club", "crossbow",
		"bastardSword", "flail", "waraxe" ];
	
	var names = [ "Dagger", "Short Sword", "Longsword", "Greatsword", "Mace",
		"Morningstar", "Light Hammer", "Warhammer", "Maul", "Javelin",
		"Short Spear", "Longspear", "Longbow", "Shortbow", "Handaxe",
		"Battleaxe", "Greataxe", "Quarterstaff", "Sling", "Club", "Crossbow",
		"Bastard Sword", "Flail", "Waraxe" ];
	
	for (var index = 0; index < ids.length; index++) {
		var cb = game.createButtonCallback(slot, "castSpell");
		cb.addArgument(ids[index]);
		
		game.addMenuButton(names[index], cb);
	}
	
	game.showMenu();
}

function castSpell(game, slot, weaponID) {
	var creatures = game.ai.getTouchableCreatures(slot.getParent(), "Friendly");
	
	var baseItem = game.getItem(weaponID, game.ruleset().getItemQuality("Good"));
	
	for (var i = 0; i < creatures.size(); i++) {
		if (!creatures.get(i).inventory.hasPrereqsToEquip(baseItem)) {
			creatures.remove(i);
			i--;
		}
	}
	
	if (creatures.size() == 0) {
		game.addMessage("red", "No available targets can use that weapon.");
	} else {
		var targeter = game.createListTargeter(slot);
		targeter.addAllowedCreatures(creatures);
		targeter.addCallbackArgument(weaponID);
		targeter.activate();
	}
}

function onTargetSelect(game, targeter, weaponID) {
	var spell = targeter.getSlot().getAbility();
	var parent = targeter.getParent();
	var target = targeter.getSelectedCreature();
	var casterLevel = parent.stats.getCasterLevel();
	
	var duration = game.dice().d5(2);
	
	targeter.getSlot().setActiveRoundsLeft(duration);
	targeter.getSlot().activate();
	
	if (!spell.checkSpellFailure(parent, target)) return;
	
	// create the weapon, set its properties, and equip it
	var allQualities = [ "Mediocre", "Decent", "Good", "Fine", "Superb", "Exceptional", "Phenomenal", "Masterwork" ];
	
	var qualityIndex = parseInt(casterLevel / 3) + 1;
	if (qualityIndex >= allQualities.length)
		qualityIndex = allQualities.length - 1;
	
	// create the item to be conjured
	var conjuredID = "__" + weaponID + "Conjured";
	var model = game.getCreatedItemModel(weaponID, conjuredID);
	model.addEnchantment("entity.addBonus(\"WeaponAttack\", 10);");
	model.setNamePrefix("Conjured ");
	model.setForceNotUnequippable(true);
	game.campaign().addCreatedItem(model.getCreatedItem());
	
	var weapon = game.getItem( conjuredID, game.ruleset().getItemQuality(allQualities[qualityIndex]) );
	
	// create an effect to keep track of the weapon
	var effect = targeter.getSlot().createEffect("effects/conjureItem");
	effect.setDuration(duration);
	effect.setTitle("Conjured Weapon");
	effect.put("itemID", conjuredID);
	
	// keep track of the old item to re-equip it if possible
	var oldItem = target.inventory.getEquippedMainHand();
	if (oldItem != null) {
		effect.put("oldItemID", oldItem.getTemplate().getID());
		effect.put("oldItemQuality", oldItem.getQuality().getName());
	}
	
	// if weapon is two handed, check the off hand slot to be re-equipped
	if (weapon.isTwoHanded()) {
		var oldItem2 = target.inventory.getEquippedOffHand();
		if (oldItem2 != null) {
			effect.put("oldItem2ID", oldItem2.getTemplate().getID());
			effect.put("oldItem2Quality", oldItem2.getQuality().getName());
		}
	}
	
	target.applyEffect(effect);
	
	target.timer.setFreeMode(true);
	target.inventory.addAndEquip(weapon);
	target.timer.setFreeMode(false);
	
	// animate the new item initially
	if (weaponID == "longbow" || weaponID == "shortbow") {
		var anim = game.getBaseAnimation("subIconFlash");
		anim.addFrame(target.getIconRenderer().getIcon("OffHandWeapon"));
		anim.setColor(target.getIconRenderer().getColor("OffHandWeapon"));
		
		var pos = target.getSubIconScreenPosition("OffHandWeapon");
		anim.setPosition(pos.x, pos.y);
	} else {
		var anim = game.getBaseAnimation("subIconFlash");
		anim.addFrame(target.getIconRenderer().getIcon("MainHandWeapon"));
		anim.setColor(target.getIconRenderer().getColor("MainHandWeapon"));
		
		var pos = target.getSubIconScreenPosition("MainHandWeapon");
		anim.setPosition(pos.x, pos.y);
	}
		
	game.runAnimationNoWait(anim);
	game.lockInterface(anim.getSecondsRemaining());
}
