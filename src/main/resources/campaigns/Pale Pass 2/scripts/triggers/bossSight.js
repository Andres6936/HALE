function startConvo(game, player) {
    game.scrollToCreature("boss_01");
    
    game.sleep(2000);
    
    game.currentArea().getEntityWithID("boss_01").startConversation(player);
}

function onPlayerEnterFirstTime(game, player, trigger) {
    game.lockInterface(3.0);
    game.runExternalScriptWait("triggers/bossSight", "startConvo", 1.0, game.getParty().get(0));
    
    game.revealArea(33, 26, 4, 0);
}
