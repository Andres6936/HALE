function onAreaLoadFirstTime(game, area, transition) {
    game.lockInterface(3.0);
    game.runExternalScriptWait("triggers/dwarvenCityOnLoad", "startConvo", 1.0, game.getParty().get(0));
}

function startConvo(game, player) {
    game.scrollToCreature("deepDwarfCityEntranceGuard");
    
    game.sleep(2000);
    
    game.currentArea().getEntityWithID("deepDwarfCityEntranceGuard").startConversation(player);
}