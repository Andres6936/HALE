function startQuest(game) {
    if (game.hasQuestEntry("Haunted House")) return;
    
    var quest = game.getQuestEntry("Haunted House");
    
    var entry = quest.createSubEntry("Defeat the Wraith");
    
    entry.addText("The Priest at the temple in Aravil asked you to enter a house that has been haunted by a powerful spirit.  You must destroy the spirit and bring its essence back to the Priest to be cleansed.");
}

function questComplete(game) {
	var quest = game.getQuestEntry("Haunted House");
	
	if (quest.isCompleted()) return;
	
	var entry = quest.createSubEntry("Quest Complete");
    entry.addText("You returned the piece of the spirit's essence to the priest, and the house is now at piece.");
    
	
	quest.setCompleted();
}