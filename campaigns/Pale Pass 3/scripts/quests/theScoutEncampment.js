function startQuest(game) {
    if (game.hasQuestEntry("The Scout Encampment")) return;
    
    var quest = game.getQuestEntry("The Scout Encampment");
    
    var entry = quest.createSubEntry("Travel to the Encampment");
    
    entry.addText("In order to enter the city of Aravil, the guard commander wants you to prove your loyalty.  She has offered to let you into the city if you travel to a nearby encampment, kill the Master's scout commander there, and return with his insignia.");
}

function insigniaObtained(game) {
    var quest = game.getQuestEntry("The Scout Encampment");
    
    if (quest.hasSubEntry("Return with the Insignia")) return;
    
    quest.setCurrentSubEntriesCompleted();
    
    var entry = quest.createSubEntry("Return with the Insignia");
    
    entry.addText("You have defeated the Master's scouts and their commander.  Return with the Insignia to the Gate Commander at Aravil.");
}

function endQuest(game) {
    var quest = game.getQuestEntry("The Scout Encampment");
    
    if (quest.isCompleted()) return;
    
    var entry = quest.createSubEntry("Quest Complete");
    entry.addText("You retrieved the Insignia and presented it to the Guard Commander.  In return, she allowed you to enter the city of Aravil.");
    
    quest.setCompleted();
}