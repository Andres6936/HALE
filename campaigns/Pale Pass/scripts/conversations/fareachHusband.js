
function startConversation(game, parent, target, conversation) {
    if (game.get("missingHusbandRewardRecieved") != null) {
        conversation.addText("Thank you so much for finding me!");
        conversation.addResponse("You are welcome.  Farewell.", "onExit");
        
    } else if (game.get("missingHusbandQuestCompleted") != null) {
        conversation.addText("Please talk to my wife about your reward.");
        conversation.addResponse("Goodbye.", "onExit");
    } else {
        conversation.addText("Are they gone?");
        conversation.addResponse("Yes, I've killed the spiders.", "convo2");
    }
}

function convo2(game, parent, target, conversation) {
    conversation.addText("Oh thank you!  I've been holed up in here for days!");
    conversation.addText("I was out hunting, when one of the beasts attacked me!  I narrowly escaped with my life!");
    
    conversation.addResponse("The way is clear now.  You can head back to town.", "completeQuest");
}

function completeQuest(game, parent, talker, conversation) {
    game.runExternalScript("quests/missingHusband", "foundHusband");
    game.put("missingHusbandQuestCompleted", true);
    
    game.currentArea().removeEntity(parent);
    parent.setLocationInCurrentArea(11, 9);
    game.getArea("Fareach").getEntities().addEntity(parent);
    
    conversation.exit();
}

function onExit(game, parent, talker, conversation) {
    conversation.exit();
}
