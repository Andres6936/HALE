function startConversation(game, parent, target, conversation) {
    if (parent.get("secretDoorFound") != null) {
        conversation.addText("There is a hidden stairway here.");
        
        conversation.addResponse("Use the stairs.", "goDownStairs");
        conversation.addResponse("Leave.", "onExit");
        
    } else {
    
        conversation.addText("There is something slightly strange about this bookcase.");
        conversation.addResponse("Investigate more closely.  <span style=\"font-family: red\">Search</span>", "investigate");
        conversation.addResponse("Leave.", "onExit");
    }
}

function investigate(game, parent, talker, conversation) {
    var check = game.campaign().getBestPartySkillModifier("Search");
    
    if (check > 5) {
        parent.put("secretDoorFound", true);
        conversation.addText("Behind the bookcase, you discover a descending stairway!");
        
        conversation.addResponse("Go down the staircase", "goDownStairs");
        conversation.addResponse("Leave.", "onExit");
        
    } else {
        conversation.addText("You find nothing of interest.");
        conversation.addResponse("Leave.", "onExit");
    }
}

function goDownStairs(game, parent, talker, conversation) {
    conversation.exit();
    
    game.campaign().transition("WillowInnToBasement");
}

function onExit(game, parent, talker, conversation) {
    conversation.exit()
}
