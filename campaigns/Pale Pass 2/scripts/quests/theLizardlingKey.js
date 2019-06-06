function startQuest(game) {
    if (game.hasQuestEntry("The Lizardling Key")) return;

    var quest = game.getQuestEntry("The Lizardling Key");
    
    var entry = quest.createSubEntry("Find the Key");
    
    entry.addText("The Lizardling Domain contains one key fragment.  Find the Lizardling King, and obtain the fragment from him.  The Lizardling Domain is located to the Northeast of the Mushroom Forest.");
}

function talkKing(game) {
    var quest = game.getQuestEntry("The Lizardling Key");
    
    if (quest.hasSubEntry("Clear the Island")) return;
    
    quest.setCurrentSubEntriesCompleted();
    
    var entry = quest.createSubEntry("Clear the Island");
    entry.addText("The Lizardling King has offered you a reward for kill the giant serpent on the island in the center of his lake.  In addition, you will be able to obtain the lizardling key fragment from the vault in the island's center.");
    
}

function keyObtained(game) {
    var quest = game.getQuestEntry("The Lizardling Key");
    
    if (quest.hasSubEntry("The Key Fragment")) return;
    
    quest.setCurrentSubEntriesCompleted();
    
    var entry = quest.createSubEntry("The Key Fragment");
    
    entry.addText("You have obtained the key fragment from the Lizardling domain.  You may wish to return to the Lizardling King and claim your reward for clearing the island.");
	
	var fragmentsObtained = game.get("fragmentsObtained");
    if (fragmentsObtained == null) fragmentsObtained = 1;
    else fragmentsObtained++;
    
    game.put("fragmentsObtained", fragmentsObtained);
    
    if (fragmentsObtained == 3) game.runExternalScript("quests/theMaster", "fragmentsObtained");
}

function questComplete(game) {
    var quest = game.getQuestEntry("The Lizardling Key");
    
    if (quest.isCompleted()) return;
    
    quest.setCurrentSubEntriesCompleted();
    
    var entry = quest.createSubEntry("Quest Complete");
    
    entry.addText("You killed the sea serpents and claimed your prize, a powerful ring.");
    
    quest.setCompleted();
}