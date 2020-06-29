
function startQuest(game) {
    var quest = game.getQuestEntry("Joining the Army")
    var entry = quest.createSubEntry("Travel to Fareach")
    entry.addText("Travel to Fareach and inquire with the garrison about joining the war effort.")
}

function followArmy(game) {
    var quest = game.getQuestEntry("Joining the Army");
    
    quest.setCurrentSubEntriesCompleted();
    
    var subQuest = quest.createSubEntry("Follow the Army");
    
    subQuest.addText("The army has already left Fareach, but you are not giving up hope yet.  You must follow, onward East to the Black River Crossing.");
    
}

function questComplete(game) {
    var quest = game.getQuestEntry("Joining the Army");
    
    var subQuest = quest.createSubEntry("Completed");
    subQuest.addText("Reaching the Black River Crossing, I discovered the bridge destroyed.");
    subQuest.addText("With the war soon ending and no hope of reaching the army for at least a couple weeks, your prospects of joining the army have disappeared.");
    
    quest.setCompleted();
}