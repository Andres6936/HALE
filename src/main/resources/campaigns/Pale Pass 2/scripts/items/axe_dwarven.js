
function onAddItem(game, parent, item) {
    game.put("dwarvenAxeRetrieved", true);
    
    game.runExternalScript("quests/theDwarvenKey", "axeRetrieved");
}
