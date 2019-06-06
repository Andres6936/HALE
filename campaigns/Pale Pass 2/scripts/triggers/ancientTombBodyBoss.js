
function onPlayerEnterFirstTime(game, player, trigger) {
    // show the surrounding area
    game.revealArea(5, 7, 3, 0);
    
    game.lockInterface(3.0);
    game.runExternalScriptWait("triggers/ancientTombBodyBoss", "startConvo", 1.0, player);
}

function startConvo(game, player) {
    game.scrollToCreature("ghoulGreater");
    
    game.sleep(2000);
    
    game.currentArea().getEntityWithID("ghoulGreater").startConversation(player);
}
