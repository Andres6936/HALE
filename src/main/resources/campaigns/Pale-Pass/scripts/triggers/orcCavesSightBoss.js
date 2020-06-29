function onPlayerEnterFirstTime(game, player) {
    game.lockInterface(3.0);
    game.runExternalScriptWait("triggers/orcCavesSightBoss", "startConvo", 1.0, player);
}

function startConvo(game, player) {
    game.scrollToCreature("orcChieftan_lvl03");
    
    game.sleep(2000);

    game.currentArea().getEntityWithID("orcChieftan_lvl03").startConversation(player);
}