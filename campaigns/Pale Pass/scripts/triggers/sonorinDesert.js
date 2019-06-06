function onAreaLoadFirstTime(game, area) {
    var popup = game.createHTMLPopup("popups/sonorinDesertEnter.html");
    popup.setSize(300, 200);
    popup.show();
}


function onPlayerEnterFirstTime(game, player) {
    game.lockInterface(3.0);
    game.runExternalScriptWait("triggers/sonorinDesert", "startConvo", 1.0, player);
}

function startConvo(game, player) {
    game.scrollToCreature("merc_lvl06_leader");
    
    game.sleep(2000);

    game.currentArea().getEntityWithID("merc_lvl06_leader").startConversation(player);
}