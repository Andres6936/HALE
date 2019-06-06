
function startConversation(game, parent, target, conversation) {
    if (parent.get("alreadyTalked") != null) {
        conversation.addText("Thank you for your help.  We will leave for our city soon.");
        
        conversation.addResponse("Farewell.", "onExit");
    } else {
        parent.put("alreadyTalked", true);
        
        conversation.addText("Have you come to free us, surfacer?");
        
        conversation.addResponse("You are free to go.", "convo02");
    }
}

function convo02(game, parent, target, conversation) {
    conversation.addText("Thank you.  We will gather what we can and then return to the our city.");
    
    if (game.hasQuestEntry("dwarvenSlavers")) {
        conversation.addResponse("Are you the sons of the smith?", "convo03");
    }
    
    conversation.addResponse("Farewell.", "onExit");
}

function convo03(game, parent, target, conversation) {
    conversation.addText("Yes.  I'm sure our father will be grateful.");
    
    conversation.addResponse("Farewell.", "onExit");
}

function onExit(game, parent, talker, conversation) {
    conversation.exit();
    
    game.put("removeGoblinSlaves", true);
    
    game.runExternalScript("quests/dwarvenSlavers", "freeSlaves");
}
