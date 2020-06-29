function runTurn(game, parent) {
	var turnNum = parent.get("turnNum");
	if (turnNum == null) turnNum = 0;

	if (turnNum != 0) {
		tryRoar(game, parent);
	}
	
    game.runExternalScript("ai/aiStandard", "runTurn", parent);
	
	parent.put("turnNum", (turnNum + 1));
}

function tryRoar(game, parent) {
	// attempt to use the Grimbok's "roar" ability

	var slots = parent.abilities.getSlotsWithReadiedAbility("GrimbokRoar");
	for (var i = 0; i < slots.size(); i++) {
		var slot = slots.get(i);
		
		if (slot.canActivate()) {
			// the ability can be used, so use it
			var allSlots = parent.abilities.createAISet();
			
			var targeter = allSlots.activateAndGetTargeter(slot);
			
			var selectable = targeter.getAllowedPoints();
			targeter.setMousePosition(selectable.get(0));
            targeter.performLeftClickAction();
			
			game.sleepStandardDelay(4);
			return;
		}
	}
}

function takeAttackOfOpportunity(game, parent, target) {
    return true;
}
