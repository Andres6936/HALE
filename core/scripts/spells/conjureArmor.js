function onActivate(game, slot) {
	if (!game.addMenuLevel("Conjure Armor")) return;
	
	var types = [ "Leather", "Mail", "Plate", "Heavy Plate" ];
		
	var ids = [ [ "boots_leather_base", "gloves_leather_base", "helmet_leather_base", "armor_leatherhard_base" ],
				[ "boots_mail_base", "gloves_mail_base", "helmet_mail_base", "armor_mail_base" ],
				[ "boots_plate_base", "gloves_plate_base", "helmet_plate_base", "armor_plate_base" ],
				[ "boots_plate_base", "gloves_plate_base", "helmet_plate_base", "armor_plateheavy_base" ] ];
		
	for (var index = 0; index < types.length; index++) {
		var cb = game.createButtonCallback(slot, "conjureArmor");
		cb.addArgument(ids[index]);
		game.addMenuButton(types[index], cb);
	}
	
	game.showMenu();
}

function conjureArmor(game, slot, itemIDs) {
	var creatures = game.ai.getTouchableCreatures(slot.getParent(), "Friendly");
	
	// make sure all targets can equip all of the items
	for (var itemIndex = 0; itemIndex < itemIDs.length; itemIndex++) {
		var baseItem = game.getItem(itemIDs[itemIndex], game.ruleset().getItemQuality("Good"));
	
		for (var i = 0; i < creatures.size(); i++) {
			if ( !creatures.get(i).inventory.hasPrereqsToEquip(baseItem) ) {
				creatures.remove(i);
				i--;
			}
		}
	}
	
	if (creatures.size() == 0) {
		game.addMessage("red", "No available targets can use that armor.");
	} else {
		var targeter = game.createListTargeter(slot);
		targeter.addAllowedCreatures(creatures);
		targeter.addCallbackArgument( itemIDs );
		targeter.activate();
	}
}

function onTargetSelect(game, targeter, itemIDs) {
	var spell = targeter.getSlot().getAbility();
	var parent = targeter.getParent();
	var target = targeter.getSelectedCreature();
	var casterLevel = parent.stats.getCasterLevel();
	
	var duration = game.dice().d5(2);
	
	targeter.getSlot().setActiveRoundsLeft(duration);
	targeter.getSlot().activate();
	
	if (!spell.checkSpellFailure(parent, target)) return;
	
	// create the items, set its properties, and equip it
	var allQualities = [ "Mediocre", "Decent", "Good", "Fine", "Superb", "Exceptional", "Phenomenal", "Masterwork" ];
	
	var qualityIndex = parseInt(casterLevel / 3) + 1;
	if (qualityIndex >= allQualities.length)
		qualityIndex = allQualities.length - 1;
	
	for (var index = 0; index < itemIDs.length; index++) {
		createItem(game, itemIDs[index], allQualities[qualityIndex], target, duration, targeter);
	}
}

function createItem(game, itemID, quality, target, duration, targeter) {
	// create the item to be conjured
	var conjuredID = "__" + itemID + "Conjured";
	var model = game.getCreatedItemModel(itemID, conjuredID);
	model.setNamePrefix("Conjured ");
	model.setForceNotUnequippable(true);
	game.campaign().addCreatedItem(model.getCreatedItem());
	
	// get a copy of the item
	var item = game.getItem(conjuredID, game.ruleset().getItemQuality(quality));
	
	if ( target.getTemplate().getRace().isSlotRestricted(item.getTemplate().getType().toString()) ) {
		// the target cannot equip items to this slot
		return;
	}
	
	// create an effect to keep track of the item
	var effect = targeter.getSlot().createEffect("effects/conjureItem");
	effect.setDuration(duration);
	effect.setTitle("Conjured Armor Piece");
	effect.put("itemID", conjuredID);
	
	// keep track of the old item to re-equip it at the end of the effect, if possible
	// also figure out the sub icon slot of the item
	if (item.getTemplate().getType().name().equals("Armor")) {
		var subIconSlot = "Torso";
		var oldItem = target.inventory.getEquippedArmor();
	} else if (item.getTemplate().getType().name().equals("Gloves")) {
		var subIconSlot = "Gloves";
		var oldItem = target.inventory.getEquippedGloves();
	} else if (item.getTemplate().getType().name().equals("Boots")) {
		var subIconSlot = "Boots";
		var oldItem = target.inventory.getEquippedBoots();
	} else {
		var subIconSlot = "Head";
		var oldItem = target.inventory.getEquippedHelmet();
	}
	
	// if there was an item in this slot, save it to re-equip after the spell ends
	if (oldItem != null) {
		effect.put("oldItemID", oldItem.getTemplate().getID());
		
		if (oldItem.getTemplate().hasQuality()) {
			effect.put("oldItemQuality", oldItem.getQuality().getName());
		}
	}
	
	target.applyEffect(effect);
	
	// equip the new item
	target.timer.setFreeMode(true);
	target.inventory.addAndEquip(item);
	target.timer.setFreeMode(false);
	
	var anim = game.getBaseAnimation("subIconFlash");
	anim.addFrame(target.getIconRenderer().getIcon(subIconSlot));
	anim.setColor(target.getIconRenderer().getColor(subIconSlot));
	
	var pos = target.getSubIconScreenPosition(subIconSlot);
	anim.setPosition(pos.x, pos.y);
	
	game.runAnimationNoWait(anim);
	game.lockInterface(anim.getSecondsRemaining());
}
