function startConvo(game, player) {
    game.scrollToCreature("theMaster");
    
    game.sleep(2000);
    
    game.currentArea().getEntityWithID("theMaster").startConversation(player);
}

function onPlayerEnterFirstTime(game, player, trigger) {
    game.lockInterface(3.0);
    game.runExternalScriptWait("triggers/theMaster", "startConvo", 1.0, player);
    
    game.revealArea(37, 7, 6, 0);
}
