function onAreaLoadFirstTime(game, area) {
    game.lockInterface(3.0);
    game.runExternalScriptWait("triggers/onOrcCavesEnter", "startConvo", 1.0);
}

function startConvo(game) {
    game.scrollToCreature("orc_guardOrcCaves");
    
    game.sleep(2000);
    
    game.currentArea().getEntityWithID("orc_guardOrcCaves").startConversation(game.getParty().get(0));
    
    game.runExternalScript("quests/theStolenGoods", "enterCave");
}