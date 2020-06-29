function onAreaLoadFirstTime(game, area, transition) {
    game.lockInterface(3.0);
    game.runExternalScriptWait("triggers/goblinCavesEnter", "startConvo", 1.0, game.getParty().get(0));
}

function startConvo(game, player) {
    game.scrollToCreature("goblin_cavernEnter");
    
    game.sleep(2000);
    
    game.currentArea().getEntityWithID("goblin_cavernEnter").startConversation(player);
}