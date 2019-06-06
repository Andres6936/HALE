function startQuest(game) {
    if (game.hasQuestEntry("The Stolen Goods")) return;
    
    var quest = game.getQuestEntry("The Stolen Goods")
    var entry = quest.createSubEntry("Travel to the Orc Cave")
    entry.addText("Travel North from the Merchant on the Forest Road to reach the Orc Cave and find the stolen goods.")
}

function enterCave(game) {
    // don't let this be the first quest entry
    if (!game.hasQuestEntry("The Stolen Goods")) return;
    
    var quest = game.getQuestEntry("The Stolen Goods");
    
    quest.setCurrentSubEntriesCompleted();
    
    var subQuest = quest.createSubEntry("Find the Goods");
    subQuest.addText("Search the cave and locate the Merchant's Stolen Goods.");
}

function addItem(game) {
    var questAlreadyAccepted = false;
    if (game.hasQuestEntry("The Stolen Goods")) questAlreadyAccepted = true;
    
    game.put("stolenGoodsObtained", true);
    
    var quest = game.getQuestEntry("The Stolen Goods");
    
    quest.setCurrentSubEntriesCompleted();
    
    var subQuest = quest.createSubEntry("Return the Goods");
    
    if (questAlreadyAccepted)
        subQuest.addText("Now that you have found the stolen goods, you can return them to the Merchant.");
    else
        subQuest.addText("You found some merchant goods in an Orc Cave.  Perhaps you can find who they belong to.");
}

function completeQuest(game) {
    var quest = game.getQuestEntry("The Stolen Goods");
    
    var subQuest = quest.createSubEntry("Completed");
    subQuest.addText("The stolen goods were returned to the merchant.");
    
    quest.setCompleted();
    
    game.addPartyXP(5 * game.ruleset().getValue("EncounterXPFactor"));
    
    game.put("stolenGoodsComplete", true);
}
