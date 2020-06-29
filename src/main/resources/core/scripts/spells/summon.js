function onActivate(game, slot) {
	if (!game.addMenuLevel(slot.getAbility().getName())) return;
	
	var casterLevel = slot.getParent().stats.getCasterLevel();
	
	// add the highest level version of the wolf that is available
	if (casterLevel >= 13) addButton(game, "summon_wolfgiant", slot);
	else if (casterLevel >= 10) addButton(game, "summon_wolflarge", slot);
	else if (casterLevel >= 7) addButton(game, "summon_wolfmedium", slot);
	else if (casterLevel >= 4) addButton(game, "summon_wolfsmall", slot);
	
	// add the highest level version of the tiger available
	if (casterLevel >= 9) addButton(game, "summon_sabretooth", slot);
	else if (casterLevel >= 6) addButton(game, "summon_tiger", slot);
	
	if (casterLevel >= 8) addButton(game, "summon_bear", slot);
	
	if (casterLevel >= 10) addButton(game, "summon_spidergiant", slot);
	
	if (casterLevel >= 12) addButton(game, "summon_yeti", slot);
	
	// add the elementals if applicable
	if (slot.getParent().abilities.has("SummonElemental")) {
		addButton(game, "summon_elementalAir", slot);
		addButton(game, "summon_elementalEarth", slot);
		addButton(game, "summon_elementalFire", slot);
		addButton(game, "summon_elementalWater", slot);
	}
	
	game.showMenu();
}

function addButton(game, creatureID, slot) {
	var cb = game.createButtonCallback(slot, "castSpell");
	cb.addArgument(creatureID);
		
	var name = game.getNPC(creatureID).getName();
		
	game.addMenuButton(name, cb);
}

function castSpell(game, slot, id) {
   var targeter = game.createCircleTargeter(slot);
   targeter.setAllowOccupiedTileSelection(false);
   targeter.setRadius(0);
   targeter.setMaxRange(4);
   
   targeter.addCallbackArgument(id);
   targeter.activate();
}

function onTargetSelect(game, targeter, id) {
	var spell = targeter.getSlot().getAbility();
	var parent = targeter.getParent();
	var casterLevel = parent.stats.getCasterLevel();
	
	var position = targeter.getAffectedPoints().get(0);
	
	var duration = parseInt(3 + casterLevel / 4);
	
	targeter.getSlot().setActiveRoundsLeft(duration);
	targeter.getSlot().activate();
	
	if (!spell.checkSpellFailure(parent)) return;
	
	var creature = game.createSummon(id, parent, duration);

	if (parent.abilities.has("ImprovedSummon")) {
		var bonusLevels = parseInt(casterLevel - 1);
	} else {
		var bonusLevels = parseInt(casterLevel - 5);
	}
	
	if (bonusLevels > 0) {
		creature.roles.addLevels(creature.roles.getBaseRole(), bonusLevels);
	}
	
	game.finishSummon(creature, position);
}
