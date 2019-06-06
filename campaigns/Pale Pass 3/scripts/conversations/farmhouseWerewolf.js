function startConversation(game, parent, target, conversation) {
    conversation.addString("<div style=\"font-family: blue\">");
    conversation.addString("As you are round the corner into the storeroom, you see a hugely muscular wolf like creature.");
    conversation.addString("</div>");
	
	conversation.addString("<div style=\"font-family: blue; margin-top: 1em;\">");
    conversation.addString("Its fangs glisten with blood.  Obviously, the occupants of this farmhouse have already been made its victims.");
    conversation.addString("</div>");
	
    conversation.addResponse("<span style=\"font-family: red\">Continue</span>", "convo02");
}

function convo02(game, parent, target, conversation) {
	conversation.addString("<div style=\"font-family: blue\">");
    conversation.addString("The creature must sense your presence, for it quickly turns towards you and leaps to attack!");
    conversation.addString("</div>");
	
	conversation.addResponse("<span style=\"font-family: red\">Fight</span>", "onExit");
}

function onExit(game, parent, talker, conversation) {
    conversation.exit();
	parent.getEncounter().setFaction("Hostile");
}
