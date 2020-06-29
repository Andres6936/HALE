function startConvo(game, player) {
    game.scrollToCreature("drakeKing");
    
    game.sleep(2000);
    
    game.currentArea().getEntityWithID("drakeKing").startConversation(player);
}

function onPlayerEnterFirstTime(game, player, trigger) {
    game.lockInterface(3.0);
    game.runExternalScriptWait("triggers/drakeKingSight", "startConvo", 1.0, game.getParty().get(0));
    
    game.revealArea(34, 9, 4, 0);
}
