function startConvo(game, player) {
    game.scrollToCreature("masterArmyLeader");
    
    game.sleep(2000);
    
    game.currentArea().getEntityWithID("masterArmyLeader").startConversation(player);
}

function onPlayerEnterFirstTime(game, player, trigger) {
    game.lockInterface(3.0);
    game.runExternalScriptWait("triggers/armyEncampment", "startConvo", 1.0, player);
    
    game.revealArea(48, 36, 4, 0);
}
