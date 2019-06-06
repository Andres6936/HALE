function startQuest(game) {
    if (game.hasQuestEntry("The Mushroom Men")) return;
    
    var quest = game.getQuestEntry("The Mushroom Men")
    var entry = quest.createSubEntry("Collect 10 mushroom meat")
    entry.addText("In the north end of the goblin city, a goblin cook approached you with an offer:  bring him ten mushroom meat and he will give you 'big treasure'.");
    entry.addText("The mushroom meat can be found on the mushroom men, just north of the goblin city.");
}

function endQuest(game) {
    var quest = game.getQuestEntry("The Mushroom Men");
    
    var entry = quest.createSubEntry("Quest complete");
    
    entry.addText("You brought the 10 pieces of mushroom meat to the goblin cook and recieved your reward.");
    
    quest.setCompleted();
}