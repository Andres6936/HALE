function startQuest(game) {
    if (game.hasQuestEntry("The Tomb Key")) return;
    
    var quest = game.getQuestEntry("The Tomb Key");
    
    var entry = quest.createSubEntry("Find the Key");
    
    entry.addText("One key fragment rests in an ancient tomb to the NorthWest of the Mushroom Forest.  Enter the tomb and retrieve the key fragment.");
}

function trialsStarted(game) {
    var quest = game.getQuestEntry("The Tomb Key");
    
    if (quest.hasSubEntry("Complete the Trials")) return;
    
    quest.setCurrentSubEntriesCompleted();
    
    var entry = quest.createSubEntry("Complete the Trials");
    
    entry.addText("You have reached the Ancient Tomb, but in order to retrieve the key fragment, you will need to complete two trials: The Trial of Mind, and the Trial of Body.");
}

function trialsComplete(game) {
    var quest = game.getQuestEntry("The Tomb Key");
    
    if (quest.hasSubEntry("Trials Complete")) return;
    
    quest.setCurrentSubEntriesCompleted();
    
    var entry = quest.createSubEntry("Trials Complete");
    
    entry.addText("You have completed the trials.  Now, you need to claim the key fragment.");
	
	game.addPartyXP(10 * game.ruleset().getValue("EncounterXPFactor"));
}

function keyObtained(game) {
    var quest = game.getQuestEntry("The Tomb Key");
    
    if (quest.hasSubEntry("The Key Fragment")) return;

    var entry = quest.createSubEntry("The Key Fragment");
    
    entry.addText("You obtained the Ancient Tomb key fragment, but were ambushed in a trap set by the Master.");
    
    game.showCutscene("ancientTomb");
    
    quest.setCompleted();
    
    var fragmentsObtained = game.get("fragmentsObtained");
    if (fragmentsObtained == null) fragmentsObtained = 1;
    else fragmentsObtained++;
    
    game.put("fragmentsObtained", fragmentsObtained);
    
    if (fragmentsObtained == 3) game.runExternalScript("quests/theMaster", "fragmentsObtained");
}

function spawnTrap(game) {
    game.addEncounterToArea("ancientTombTrap_lvl10", 22, 26);
}