
function startConversation(game, parent, target, conversation) {
    conversation.addString("<div style=\"font-family: blue\">");
    conversation.addString("Before you is likely the most hideous creature you have ever seen.");
    conversation.addString("</div>");
    
    conversation.addString("<div style=\"font-family: blue\">");
    conversation.addString("It has three disfigured heads, and a gigantic mouth filled with teeth where its stomach should be.");
    conversation.addString("</div>");
    
    conversation.addResponse("<span style=\"font-family: red\">Continue</span>", "convo01");
}

function convo01(game, parent, target, conversation) {
    conversation.addText("Test your strength, mortal!  Defeat me to complete the Trial of Body!");
    
    conversation.addResponse("<span style=\"font-family: red\">Fight</span>", "onExit");
}

function onExit(game, parent, talker, conversation) {
	parent.getEncounter().setFaction("Hostile");
    conversation.exit();
    game.clearRevealedAreas();
    
    game.put("trialOfBodyComplete", true);
}
