function onPlayerEnter(game, player) {
    if (game.get("joiningTheArmyComplete") != null && game.get("joiningTheArmyWalkAway") == null) {
        game.put("joiningTheArmyWalkAway", true);
        
        game.showCutscene("blackRiverCrossing");
        game.runExternalScript("quests/aStrangeDream", "seeVision");
    }
}

function startConvo(game) {
    game.lockInterface(3.0);
    game.runExternalScriptWait("triggers/blackRiverCrossingWalkAway", "startConvoAsync", 1.0);
}

function startConvoAsync(game) {
    game.scrollToCreature(game.getParty().get(1));
    
    game.sleep(2000);
    game.startConversation(game.getParty().get(1), game.getParty().get(0), "conversations/blackRiverCrossingWalkAway");
}
