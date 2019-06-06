function startConversation(game, parent, target, conversation) {
    if (parent.get("secretFound") != null) {
        conversation.addText("There is nothing else of interest here.");
        conversation.addResponse("Leave.", "onExit");
        
    } else {
    
        conversation.addText("There is something off about these barrels.");
        conversation.addResponse("Investigate more closely.  <span style=\"font-family: red\">Search</span>", "investigate");
        conversation.addResponse("Leave.", "onExit");
    }
}

function investigate(game, parent, talker, conversation) {
    var check = game.campaign().getBestPartySkillModifier("Search");
    
    if (check > 30) {
        parent.put("secretFound", true);
        conversation.addText("After a thorough search, you discover a hidden coin purse under the barrels.");
        conversation.addText("In the purse are coins worth 5 GP.");
        
        game.getPartyCurrency().addGP(5)
        
        conversation.addResponse("Leave.", "onExit");
        
    } else {
        conversation.addText("You find nothing of interest.");
        conversation.addResponse("Leave.", "onExit");
    }
}

function onExit(game, parent, talker, conversation) {
    conversation.exit()
}
