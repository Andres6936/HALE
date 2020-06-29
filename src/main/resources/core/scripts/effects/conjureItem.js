function onRemove(game, effect) {
	var target = effect.getTarget();

	target.timer.setFreeMode(true);
	
	target.inventory.remove(effect.get("itemID"));
	
	// if possible, re-equip the old item
	if (effect.get("oldItemID") != null) {
		reequip(game, target, effect.get("oldItemID"), effect.get("oldItemQuality"));
	}
	
	// if possible, re-equip old item 2
	if (effect.get("oldItem2ID") != null) {
		reequip(game, target, effect.get("oldItem2ID"), effect.get("oldItem2Quality"));
	}
	
	target.timer.setFreeMode(false);
}

function reequip(game, target, id, quality) {
	if (target.inventory.getUnequippedItems().contains(id, quality)) {
		target.inventory.equipItem(id, quality);
	}
}
