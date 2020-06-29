function startConvo(game, player) {
    game.scrollToCreature("scoutCommander");
    
    game.sleep(2000);
    
    game.currentArea().getEntityWithID("scoutCommander").startConversation(player);
}

function onPlayerEnterFirstTime(game, player, trigger) {
    game.lockInterface(3.0);
    game.runExternalScriptWait("triggers/scoutCommander", "startConvo", 1.0, player);
    
    game.revealArea(23, 22, 4, 0);
}
