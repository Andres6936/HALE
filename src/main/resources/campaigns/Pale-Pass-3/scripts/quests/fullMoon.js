function startQuest(game) {
    if (game.hasQuestEntry("Full Moon")) return;
    
    var quest = game.getQuestEntry("Full Moon");
    
    var entry = quest.createSubEntry("Investigate the strange reports");
    
    entry.addText("The Guard Captain of Aravil asked you to head to Greenrange Forest and investigate some strange reports of animal disappearances.  He suggested you start by going to a farmhouse in the north end of the forest.");
}

function readJournal(game) {
	var quest = game.getQuestEntry("Full Moon");
	
	if (quest.hasSubEntry("Find the werewolves")) return;
    
    quest.setCurrentSubEntriesCompleted();
    
    var entry = quest.createSubEntry("Find the werewolves");
    
    entry.addText("In the farmhouse, you discovered that the farmers had been killed by a werewolf.  The farmer's journal indicated that the wolves were coming from the canyons, on the south end of the Greenrange Forest.  Find the werewolve's den and put a stop to the attacks.");
}

function werewolvesDefeated(game) {
	var quest = game.getQuestEntry("Full Moon");
	
	if (quest.hasSubEntry("Return to the Guard Captain")) return;
	
	quest.setCurrentSubEntriesCompleted();
	
	var entry = quest.createSubEntry("Return to the Guard Captain");
	
	entry.addText("You have defeated the werewolves and have proof in the form of the foot of the most powerful werewolf.  Return to the Captain of the Guard in the Aravil Commons.");
}

function questComplete(game) {
	var quest = game.getQuestEntry("Full Moon");
	
	if (quest.isCompleted()) return;
	
	var entry = quest.createSubEntry("Quest Complete");
    entry.addText("You returned to the Guard Captain with evidence of your deeds and recieved your reward.");
	
	quest.setCompleted();
}

