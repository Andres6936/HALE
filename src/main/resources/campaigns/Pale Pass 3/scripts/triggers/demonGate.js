function onAreaLoadFirstTime(game, area, transition) {
    game.lockInterface(3.0);
    game.runExternalScriptWait("triggers/demonGate", "startConvo", 1.0, game.getParty().get(0));
    
    game.revealArea(9, 10, 5, 0);
}

function startConvo(game, player) {
    game.scrollToPosition(9, 10);
    
    game.sleep(2000);
    
    game.currentArea().getEntityWithID("demonGateMage").startConversation(player);
}
