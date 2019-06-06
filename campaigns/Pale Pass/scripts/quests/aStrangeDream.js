function startQuest(game) {
    if (game.hasQuestEntry("A Strange Dream")) return;
    
    game.showCutscene("willowInnSleep")
    
    game.put("sleptWillowInn", true);
    
    var quest = game.getQuestEntry("A Strange Dream")
    var entry = quest.createSubEntry("The dream")
    entry.addText("You had a strange dream while sleeping at the Willow Inn.");
    entry.addText("  You saw a man shrouded in black, and a strange glowing sword.");
    entry.addText("  You don't know what it all means.  In any event, you should continue on the road to Fareach.  ");
    
    game.activateTransition("WillowInnRoadToForestRoad");
    
    game.currentArea().removeEntity(game.currentArea().getEntityWithID("willowInn_guest01"));
    game.currentArea().removeEntity(game.currentArea().getEntityWithID("willowInn_guest02"));
    game.currentArea().removeEntity(game.currentArea().getEntityWithID("willowInn_guest03"));
    game.currentArea().removeEntity(game.currentArea().getEntityWithID("willowInn_guest04"));
    
    game.currentArea().getEntityWithID("willowInn_barmaid").setLocationInCurrentArea(13, 5);
}

function seeVision(game) {
    var quest = game.getQuestEntry("A Strange Dream");
    var entry = quest.createSubEntry("A waking vision");
    
    quest.setCurrentSubEntriesCompleted();
    
    entry.addText("While at Black River Crossing, you had a waking vision, again of the shadowy figure.  ");
    entry.addText("It beckoned you to go to a desert, the location of which you strangely now know.  ");
    entry.addText("Although you have no idea what to expect, the only way to get to the source of these visions is to follow the path laid out to you.");
    
    game.revealWorldMapLocation("Southern Road");
}

function mercNote(game) {
    var quest = game.getQuestEntry("A Strange Dream");
	
	if (quest.hasSubEntry("Unexpected Resistance")) return;
	
    var entry = quest.createSubEntry("Unexpected Resistance");
    
    quest.setCurrentSubEntriesCompleted();
    
    entry.addText("In the Sonorin Desert, you came across some mercenaries who attacked you.  ");
    entry.addText("You discovered a note from an unknown figure with the initial E who someone knew exactly where you'd be.  ");
    entry.addText("It is clear that this figure, whoever he or she is, does not want you to continue pursuing the source of your visions.  ");
    entry.addText("According to the note, there is a canyon nearby where the mercenary was supposed to meet this person.");
    
}