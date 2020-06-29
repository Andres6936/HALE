
function onPlayerEnterFirstTime(game, player, trigger) {
    // show the lizardling king
    game.revealArea(32, 33, 5, 0);
    
    game.lockInterface(3.0);
    game.runExternalScriptWait("triggers/lizardlingKingEnter", "startConvo", 1.0, player);
}

function startConvo(game, player) {
    game.scrollToCreature("lizardlingKing");
    
    game.sleep(2000);
    
    var target = game.currentArea().getEntityWithID("lizardlingKing");
    
    game.setFactionRelationship("Player", "Lizardlings", "Friendly");
    target.getEncounter().setFaction("Lizardlings");
    
    target.startConversation(player);
}