function startQuest(game) {
    if (game.hasQuestEntry("The Master")) return;
    
    var quest = game.getQuestEntry("The Master");
    
    var entry = quest.createSubEntry("Escape from the Underground");
    
    entry.addText("You have escaped from the subterranean land you were trapped in.  While exploring the cave system, you discovered that you are of a chosen blood, and that the Master will not stop until you are dead.");
    
    entry.addText("In order to stop the seemingly endless stream of assassins, you must find and defeat the Master.");
}

function learnOfArmy(game) {
    var quest = game.getQuestEntry("The Master");
    
    if (quest.hasSubEntry("The Master's Army")) return;
    
    quest.setCurrentSubEntriesCompleted();
    
    var entry = quest.createSubEntry("The Master's Army");
    
    entry.addText("You met a scout who told you that while you were trapped underground, the Master has assembled an army and is now openly opposing the King.");
    
    entry.addText("You should travel to Aravil to learn more and help with the fight against the Master.");
}

function learnOfTomb(game) {
    var quest = game.getQuestEntry("The Master");
    
    if (quest.hasSubEntry("The Tomb of Narkel")) return;
    
    quest.setCurrentSubEntriesCompleted();
    
    var entry = quest.createSubEntry("The Tomb of Narkel");
    
    entry.addText("You spoke with the leader of Aravil about your adventures against the Master.  She asked you to travel to the Tomb of an ancient warrior, Narkel, and see if you can uncover anything about how to stop the Master.");
}

function narkel(game) {
    var quest = game.getQuestEntry("The Master");
    
    if (quest.hasSubEntry("The Binding Focus")) return;
    
    quest.setCurrentSubEntriesCompleted();
    
    var entry = quest.createSubEntry("The Binding Focus");
    
    entry.addText("Deep in his ancient tomb, you found and spoke with the spirit of the warrior, Narkel.");
    
    entry.addText("He informed you that the Master is an ancient demon, bound to this world through a powerful spell requiring some manner of focus.");
    
    entry.addText("If the focus is destroyed, the Master should be removed from this plane.  Unfortunately, Narkel did not know where the focus would be.");
    
    entry.addText("His only clues were that the focus must be a large and pure crystal, and that the Master would most likely keep it close to him.");
    
    entry.addText("You should speak to the Lady of Aravil about what you have discovered.");
}

function narkelComplete(game) {
    var quest = game.getQuestEntry("The Master");
    
    if (quest.hasSubEntry("Biding your Time")) return;
    
    quest.setCurrentSubEntriesCompleted();
    
    var entry = quest.createSubEntry("Biding your Time");
    
    entry.addText("You spoke with the Lady of Aravil about your discoveries in the Tomb of Narkel.  She asked you to come speak with her again in a few days, after her agents have had a chance to look for leads regarding the focus crystal.");
}

function learnOfGate(game) {
    var quest = game.getQuestEntry("The Master");
    
    if (quest.hasSubEntry("Renarel Lake")) return;
    
    quest.setCurrentSubEntriesCompleted();
    
    var entry = quest.createSubEntry("Renarel Lake");
    
    entry.addText("You have learned of an infernal gate that the Master is using to summon an army of demons.");
    
    entry.addText("Travel to Renarel Lake and kill the mages that are powering the gate.");
}

function gateComplete(game) {
    var quest = game.getQuestEntry("The Master");
    
    if (quest.hasSubEntry("The Demon Gate")) return;
    
    quest.setCurrentSubEntriesCompleted();
    
    var entry = quest.createSubEntry("The Demon Gate");
    
    entry.addText("You defeated the mages responsible for summoning the demons, and the gate's conduit has been severed.");
    
    entry.addText("You should return to the Lady of Aravil to plan your next move.");
}

function learnOfArmyLocation(game) {
    var quest = game.getQuestEntry("The Master");
    
    if (quest.hasSubEntry("Army Camp")) return;
    
    quest.setCurrentSubEntriesCompleted();
    
    var entry = quest.createSubEntry("Army Camp");
    
    entry.addText("You learned from the Lady of Aravil that her scouts have located the Master's base and hopefully the focus crystal as well.  Head north to meet up with the army, and speak to the General in charge there.");
}

function theFocusCrystal(game) {
    var quest = game.getQuestEntry("The Master");
    
    if (quest.hasSubEntry("The Focus Crystal")) return;
    
    quest.setCurrentSubEntriesCompleted();
    
    var entry = quest.createSubEntry("The Focus Crystal");
    
    entry.addText("Apparently, the focus crystal currently lies in the enemy army's camp.  The Aravil army will continue to fight the army on the front lines, while you attack from the rear.  You must find and destroy the focus crystal.");
}

function crystalLost(game) {
    var quest = game.getQuestEntry("The Master");
    
    if (quest.hasSubEntry("Crystal Lost")) return;
    
    quest.setCurrentSubEntriesCompleted();
    
    var entry = quest.createSubEntry("Crystal Lost");
    
    entry.addText("Unfortunately, you arrived at the enemy camp too late to claim the crystal.  You should return to the Aravil Army Camp to figure out your next move.");
}

function palePass(game) {
    var quest = game.getQuestEntry("The Master");
    
    if (quest.hasSubEntry("Pale Pass")) return;
    
    quest.setCurrentSubEntriesCompleted();
    
    var entry = quest.createSubEntry("Pale Pass");
    
    entry.addText("You have learned that the Master's base of operations is located to the north, in the Pale Pass.  You must travel there, destroy the focus crystal, and defeat the evil Master once and for all.");
}