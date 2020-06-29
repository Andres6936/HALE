function onCreatureDeath(game, parent) {
	var numDead = game.get("demonGateMageKilled");
    if (numDead == null) numDead = 0;
    
    numDead = parseInt(numDead);
	
	if (numDead < 3) {
        // still more hostiles to kill
        game.put("demonGateMageKilled", parseInt(numDead + 1));
    } else {
        // all enemies dead
        game.runExternalScript("quests/theMaster", "gateComplete");
		game.put("demonGateComplete", true);
		game.addPartyXP(30 * game.ruleset().getValue("EncounterXPFactor"));
		game.addMessage("You have defeated the mages and shut down the demon conduit.");
    }
}

function runTurn(game, parent) {
	var curTurn = parent.get("combatTurn");
	if (curTurn == null) {
		curTurn = 0;
	} else {
		curTurn = parseInt(curTurn) + 1;
	}
	
	parent.put("combatTurn", curTurn);
	
	if (curTurn == 0) {
		// always summon something on the first turn
		summon(game, parent);
	} else {
		// summon one out of six turns
		if (game.dice().d6() == 6) {
			summon(game, parent);
		}
	}
}

function summon(game, parent) {
	var choices = [ "demonEye", "demonBlue", "demonBlue", "hellHound", "hellHound" ];
	
	var index = game.dice().randInt(0, choices.length - 1);
	var summonID = choices[index];

	// first, find a location to summon to
	var position = game.ai.findClosestEmptyTile(parent.getLocation().toPoint(), 5);
	
	if (position == null) {
		// if there are no positions available, something is horribly wrong
		return;
	}
	
	var creature = game.createSummon(summonID, parent, 999999);
	game.finishSummon(creature, position);
	game.addMessage(parent.getName() + " summons " + creature.getName() + ".");
}

function shuffle(array) {
    var tmp, current, top = array.length;

    if (top) while(--top) {
        current = Math.floor(Math.random() * (top + 1));
        tmp = array[current];
        array[current] = array[top];
        array[top] = tmp;
    }

    return array;
}