function onPlayerEnterFirstTime(game, player) {
    game.lockInterface(3.0);
    game.runExternalScriptWait("triggers/largeCavernEnter", "startConvo", 1.0, player);
}

function startConvo(game, player) {
    game.scrollToCreature("goblin_cavernEnter");
    
    game.sleep(2000);
    
    game.currentArea().getEntityWithID("goblin_cavernEnter").startConversation(player);
}