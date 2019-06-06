function startConversation(game, parent, target, conversation) {
    if (parent.get("secretDoorFound") != null) {
        conversation.addText("There is a hidden door to a cave here.");
        
        conversation.addResponse("Enter the cave", "enterCave");
        conversation.addResponse("Leave.", "onExit");
        
    } else {
    
        conversation.addText("The way the nearby objects are arranged about this section of wall is strange.");
        conversation.addResponse("Investigate more closely.  <span style=\"font-family: red\">Search</span>", "investigate");
        conversation.addResponse("Leave.", "onExit");
    }
}

function investigate(game, parent, talker, conversation) {
    var check = game.campaign().getBestPartySkillModifier("Search");
    
    if (check > 30) {
        parent.put("secretDoorFound", true);
        conversation.addText("After careful search, you discover a pressure plate, revealing a secret cave!");
        
        conversation.addResponse("Go into the cave.", "enterCave");
        conversation.addResponse("Leave.", "onExit");
        
    } else {
        conversation.addText("You find nothing of interest.");
        conversation.addResponse("Leave.", "onExit");
    }
}

function enterCave(game, parent, talker, conversation) {
    conversation.exit();
    
    game.campaign().transition("SouthernRoadToBanditStash");
}

function onExit(game, parent, talker, conversation) {
    conversation.exit()
}
