function startConversation(game, parent, target, conversation) {
    if (parent.get("secretFound") != null) {
        conversation.addText("There is nothing else of interest here.");
        
        conversation.addResponse("Leave.", "onExit");
        
    } else {
    
        conversation.addText("There is an unusual looking boulder here.");

        conversation.addResponse("Investigate more closely.  <span style=\"font-family: red\">Search</span>", "investigate");
        conversation.addResponse("Leave.", "onExit");
    }
}

function investigate(game, parent, talker, conversation) {
    var check = game.campaign().getBestPartySkillModifier("Search");
    
    if (check > 50) {
        parent.put("secretFound", true);
        conversation.addText("After a careful search, you discover a hidden stash of gold coins worth 8 GP!");
        
        conversation.addResponse("Leave.", "onExit");
        
        game.getPartyCurrency().addGP(8);
        
    } else {
        conversation.addText("You find nothing of interest.");
        conversation.addResponse("Leave.", "onExit");
    }
}

function onExit(game, parent, talker, conversation) {
    conversation.exit()
}
