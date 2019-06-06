function startConversation(game, parent, target, conversation) {
    if (parent.get("secretFound") == null) {
    
        conversation.addString("<div style=\"font-family: blue; margin-top: 1em\">");
        conversation.addString("There is something odd about this section of wall.");
        conversation.addString("</div>");
        
        conversation.addResponse("<span style=\"font-family: red\">Search</span> Examine the wall carefully", "examine");
    
    } else {
        conversation.addString("<div style=\"font-family: blue; margin-top: 1em\">");
        conversation.addString("There is nothing here.");
        conversation.addString("</div>");
    }

    conversation.addResponse("<span style=\"font-family: red\">Leave</span>", "onExit");
}

function examine(game, parent, talker, conversation) {
    var check = game.campaign().getBestPartySkillModifier("Search");
    
    if (check > 45) {
        parent.put("secretFound", true);
        
        game.activateTransition("ancientTombToSecretTomb");
        
        conversation.addString("<div style=\"font-family: blue; margin-top: 1em\">");
        conversation.addString("You discover a hidden passageway!");
        conversation.addString("</div>");
    } else {
        conversation.addString("<div style=\"font-family: blue; margin-top: 1em\">");
        conversation.addString("A careful search reveals nothing out of the ordinary.");
        conversation.addString("</div>");
    }
    
    conversation.addResponse("<span style=\"font-family: red\">Leave</span>", "onExit");
}

function onExit(game, parent, talker, conversation) {
    conversation.exit();
}