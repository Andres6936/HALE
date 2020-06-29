function onPlayerEnterFirstTime(game, player) {
    game.lockInterface(3.0);
    game.runExternalScriptWait("triggers/theGateSoldier", "startConvo", 1.0, player);
}

function startConvo(game, player) {
    game.scrollToCreature("theGateSoldier");
    
    game.sleep(2000);
    
    game.currentArea().getEntityWithID("theGateSoldier").startConversation(player);
}