
function startQuest(game) {
    var quest = game.getQuestEntry("The Missing Husband")
    var entry = quest.createSubEntry("Find the husband of the woman in Fareach")
    entry.addText("You spoke to a woman in Fareach whose husband has been missing for days, and agreed to look for him.");
    entry.addText("You should look in the forest to the South of Fareach, where he was last headed.");
}

function foundHusband(game) {
    var quest = game.getQuestEntry("The Missing Husband");
    
    quest.setCurrentSubEntriesCompleted();
    
    var subEntry = quest.createSubEntry("Return to Fareach");
    
    subEntry.addText("Return to the town of Fareach and tell the woman there that you have found her husband.");
}

function questComplete(game) {
    var quest = game.getQuestEntry("The Missing Husband");
    
    var subQuest = quest.createSubEntry("Completed");
    subQuest.addText("I've reunited the wife with her missing husband.");
    
    game.addPartyXP(5 * game.ruleset().getValue("EncounterXPFactor"));
    
    quest.setCompleted();
}