function startConversation(game, parent, target, conversation) {
    if (parent.get("secretFound") == null) {
    
        conversation.addString("<div style=\"font-family: blue; margin-top: 1em\">");
        conversation.addString("This boulder seems very out of place in the surrounding area.");
        conversation.addString("</div>");
        
        conversation.addResponse("<span style=\"font-family: red\">Search</span> Examine the boulder carefully", "examine");
    
    } else {
        conversation.addString("<div style=\"font-family: blue; margin-top: 1em\">");
        conversation.addString("There is nothing here.");
        conversation.addString("</div>");
    }

    conversation.addResponse("<span style=\"font-family: red\">Leave</span>", "onExit");
}

function examine(game, parent, talker, conversation) {
    var check = game.campaign().getBestPartySkillModifier("Search");
    
    if (check > 50) {
        parent.put("secretFound", true);
        
        talker.inventory.getUnequippedItems().add("deepTunnelsKey");
        
        conversation.addString("<div style=\"font-family: blue; margin-top: 1em\">");
        conversation.addString("You discover a key hidden under the rock.  It likely opens a door somewhere nearby.");
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