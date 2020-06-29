function startQuest(game) {
    if (game.hasQuestEntry("The Master")) return;
    
    var quest = game.getQuestEntry("The Master")
    var entry = quest.createSubEntry("The Cave Collapse")
    entry.addText("You need to find the master of the mercenaries who tried to kill you, and how it fits in with the strange visions you have had.");
    entry.addText("First, though, you need to find some way of returning to the surface.");
    
}

function goblinTrust(game) {
    var quest = game.getQuestEntry("The Master");
    
    quest.setCurrentSubEntriesCompleted();
    
    var entry = quest.createSubEntry("The Goblin City");
    entry.addText("You found some unlikely allies in a clan of goblins living underground.  When you are ready, you should speak to the goblin's leader in the south end of the city about returning to the surface.");
}

function startGate(game) {
    var quest = game.getQuestEntry("The Master");
    
    if (quest.hasSubEntry("The Gate to the Surface")) return;
    
    quest.setCurrentSubEntriesCompleted();
    
    var entry = quest.createSubEntry("The Gate to the Surface");
    
    entry.addText("The goblin chieftan has told you of a gate to the surface to the south of the goblin city.  However, in order to use the gate you will need to locate three pieces of a key.  The pieces may be found by journeying beyond the mushroom forest to the north of the goblin city.");
}

function learnOfMaster(game) {
    var quest = game.getQuestEntry("The Master");
    
    if (quest.hasSubEntry("The Nature of the Master")) return;
    
    quest.setCurrentSubEntriesCompleted();
    
    var entry = quest.createSubEntry("The Nature of the Master");
    
    entry.addText("In an Ancient Tomb, you discovered the reason why the Master seeks to kill you.  You are a descendant of one of an ancient order of mages, and hold a piece of the Master's power inside of you.  You must either find a way to defeat or destroy him, or be killed yourself.");
}

function fragmentsObtained(game) {
    var quest = game.getQuestEntry("The Master");
    
    if (quest.hasSubEntry("Fragments Obtained")) return;
    
    quest.setCurrentSubEntriesCompleted();
    
    var entry = quest.createSubEntry("Fragments Obtained");
    
    entry.addText("You have finally collected all three key fragments.  Now, you must return to the goblin chieftan, who possesses the fourth and final fragment.");
}

function keyComplete(game) {
    var quest = game.getQuestEntry("The Master");
    
    if (quest.hasSubEntry("Travel to the Gate")) return;
    
    quest.setCurrentSubEntriesCompleted();
    
    var entry = quest.createSubEntry("Travel to the Gate");
    
    entry.addText("You have the completed key in your possession.  You must travel to the Gate and return to the surface.  In order to reach the gate, you will need to pass through the lair of the fire drakes.");
}