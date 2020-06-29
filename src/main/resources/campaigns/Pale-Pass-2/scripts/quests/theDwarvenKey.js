function startQuest(game) {
    if (game.hasQuestEntry("The Dwarven Key")) return;
    
    var quest = game.getQuestEntry("The Dwarven Key");
    
    var entry = quest.createSubEntry("Find the Key");
    
    entry.addText("The Dwarves have a key fragment located in their city to the north.  Travel to the city and find the key fragment.");
}

function axeQuest(game) {
    var quest = game.getQuestEntry("The Dwarven Key");
    
    if (quest.hasSubEntry("Find the Axe")) return;
    
    var entry = quest.createSubEntry("Find the Axe");
    
    entry.addText("You have spoken to the Dwarven King, and he has agreed to give you his key fragment.  First, however, you must retrieve an axe of his royal family from the Goblin territory, in the Deep Tunnels area.");
}

function axeRetrieved(game) {
    var quest = game.getQuestEntry("The Dwarven Key");
    
    if (quest.hasSubEntry("Return the Axe")) return;
    
    var entry = quest.createSubEntry("Return the Axe");
    
    entry.addText("You have retrieved the axe from the Deep Tunnels.  Return it to the Dwarven King and he has agreed to give you the key fragment.");
}

function questComplete(game) {
    var quest = game.getQuestEntry("The Dwarven Key");
    
    if (quest.isCompleted()) return;
    
    quest.setCurrentSubEntriesCompleted();
    
    var entry = quest.createSubEntry("Quest Complete");
    
    entry.addText("You retrieved the axe and in return recieved the key fragment from the dwarven king.");
    
    quest.setCompleted();
    
    var fragmentsObtained = game.get("fragmentsObtained");
    if (fragmentsObtained == null) fragmentsObtained = 1;
    else fragmentsObtained++;
    
    game.put("fragmentsObtained", fragmentsObtained);
    
    if (fragmentsObtained == 3) game.runExternalScript("quests/theMaster", "fragmentsObtained");
}