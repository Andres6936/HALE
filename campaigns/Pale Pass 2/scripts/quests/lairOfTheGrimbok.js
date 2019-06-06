function startQuest(game) {
    if (game.hasQuestEntry("Lair of the Grimbok")) return;
    
    var quest = game.getQuestEntry("Lair of the Grimbok")
    var entry = quest.createSubEntry("Find the Lair")
    entry.addText("Reaching the entrance of a goblin city, you were confronted by the goblin's leader.");
    entry.addText("He offered to help you return to the surface if you find and kill a beast known as a Grimbok.");
    entry.addText("First, you must travel east to the beast's lair.");
}

function killedGrimbok(game) {
    var quest = game.getQuestEntry("Lair of the Grimbok");
    
    quest.setCurrentSubEntriesCompleted();
    
	if (quest.hasSubEntry("Return with the head")) return;
	
    var entry = quest.createSubEntry("Return with the head");
    
    entry.addText("You have defeated the Grimbok and collected the beast's head.");
    entry.addText("  Now, you should return to the Goblin Chieftan.");
    
    game.put("killedGrimbok", true);
}

function endQuest(game) {
    var quest = game.getQuestEntry("Lair of the Grimbok");
    
    var entry = quest.createSubEntry("Quest complete");
    
    entry.addText("You defeated the Grimbok and returned with the head to the Goblin Chieftan.");
    
    quest.setCompleted();
}