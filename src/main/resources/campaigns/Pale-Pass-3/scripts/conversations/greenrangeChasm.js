function startConversation(game, parent, target, conversation) {
    if (parent.get("secretFound") == null) {
    
        conversation.addString("<div style=\"font-family: blue; margin-top: 1em\">");
        conversation.addString("This part of the cave is on the edge of a deep chasm.  You wonder how deep it goes.");
        conversation.addString("</div>");
        
        conversation.addResponse("<span style=\"font-family: red\">Search</span> Examine the cave carefully", "examine");
    
    } else {
        conversation.addString("<div style=\"font-family: blue; margin-top: 1em\">");
        conversation.addString("There is nothing else here.");
        conversation.addString("</div>");
    }

    conversation.addResponse("<span style=\"font-family: red\">Leave</span>", "onExit");
}

function examine(game, parent, talker, conversation) {
    var check = game.campaign().getBestPartySkillModifier("Search");
    
    if (check > 75) {
        parent.put("secretFound", true);
        
        game.activateTransition("greenrangeCaveToLevel2");
        
        conversation.addString("<div style=\"font-family: blue; margin-top: 1em\">");
        conversation.addString("Well concealed under the lip of the cave floor, you discover a rope has been secured to the rocks.  It must lead to a secret room below.");
        conversation.addString("</div>");
    } else {
        conversation.addString("<div style=\"font-family: blue; margin-top: 1em\">");
        conversation.addString("A careful search of the area reveals nothing out of the ordinary.");
        conversation.addString("</div>");
    }
    
    conversation.addResponse("<span style=\"font-family: red\">Leave</span>", "onExit");
}

function onExit(game, parent, talker, conversation) {
    conversation.exit();
}