function startQuest(game) {
    if (game.hasQuestEntry("Smuggler's Stash")) return;
    
    var quest = game.getQuestEntry("Smuggler's Stash");
    
    var entry = quest.createSubEntry("Find the Smuggler's Stash");
    
    entry.addText("A barkeep in Aravil told you an interesting story about a smuggler's stash located in or near a cave somewhere in Greenrange Forest.  Supposedly, the smuggler had a stash of very high quality weaponry.");
}

function questComplete(game) {
	var quest = game.getQuestEntry("Smuggler's Stash");
	
	if (quest.isCompleted()) return;
	
	quest.setCurrentSubEntriesCompleted();
	
	var entry = quest.createSubEntry("Quest Complete");
    entry.addText("You discovered a secret level in a cave in Greenrange Forest containing the remains of the smuggler's stash.");
    
    quest.setCompleted();
}
