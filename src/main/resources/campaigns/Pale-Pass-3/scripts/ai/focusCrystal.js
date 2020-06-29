function startConvo(game, player) {
    game.scrollToCreature("theMaster");
    
    game.sleep(2000);
    
    game.currentArea().getEntityWithID("theMaster").startConversation(player);
}

function onCreatureDeath(game, parent) {
	// check to make sure the player hasn't already somehow killed the master
	var target = game.currentArea().getEntityWithID("theMaster");
	if (target == null) return;

	game.lockInterface(3.0);
    game.runExternalScriptWait("ai/focusCrystal", "startConvo", 1.0, game.getParty().get(0));
}

function runTurn(game, parent) {
	var target = game.currentArea().getEntityWithID("theMaster");
	
	// check to make sure the player hasn't already somehow killed the master
	if (target == null) return;
	
	var damage = target.stats.getMaxHP() - target.getCurrentHitPoints();
	
	if (damage > 0)
		target.healDamage(damage);
}