function startQuest(game) {
    if (game.hasQuestEntry("Dwarven Slavers")) return;
    
    var quest = game.getQuestEntry("Dwarven Slavers")
    var entry = quest.createSubEntry("Find the Slaver Camp")
    
    entry.addText("The smith in the goblin village told you the story of how his sons have apparently been taken by slavers.");
    entry.addText("He believes that they will be found at the north end of the Mushroom Forest, in a slaver camp located there.");
}

function enterCamp(game) {
	if (!game.hasQuestEntry("Dwarven Slavers")) return;
	
	var quest = game.getQuestEntry("Dwarven Slavers");
	
	if (quest.hasSubEntry("Free the Slaves")) return;
	
	var entry = quest.createSubEntry("Free the Slaves");
	
	entry.addText("You have entered the Slaver camp.  You need to find where they keep the slaves and free them.");
}

function freeSlaves(game) {
	game.put("slaverQuestFinished", true);

	if (!game.hasQuestEntry("Dwarven Slavers")) return;
	
	var quest = game.getQuestEntry("Dwarven Slavers");
	
	if (quest.hasSubEntry("Return to the Smith")) return;
	
	var entry = quest.createSubEntry("Return to the Smith");
	
	entry.addText("You have freed the Goblin slaves from the Dwarven camp.  Return to the smith in the goblin city and tell him the good news.");
}

function endQuest(game) {
    var quest = game.getQuestEntry("Dwarven Slavers");
    
	if (quest.hasSubEntry("Quest Complete")) return;
	
    var entry = quest.createSubEntry("Quest Complete");
    
    entry.addText("You found and freed the Goblin Smith's sons.");
    
    quest.setCompleted();
}