function startConversation(game, parent, target, conversation) {
	conversation.addString("<div style=\"font-family: blue\">");
    conversation.addString("You rush into the summoning room to find four mages surrounding a burning pit.");
    conversation.addString("</div>");
	
	conversation.addString("<div style=\"font-family: blue; margin-top: 1em;\">");
    conversation.addString("Chanting a strange and horrible rite, you can see them start the process of summoning a demon before your very eyes.");
    conversation.addString("</div>");
	
    conversation.addResponse("<span style=\"font-family: red\">Continue</span>", "convo02");
}

function convo02(game, parent, target, conversation) {
	conversation.addString("<div style=\"font-family: blue\">");
    conversation.addString("The mages must be stopped in order to close the conduit.");
    conversation.addString("</div>");
	
	conversation.addResponse("<span style=\"font-family: red\">Fight</span>", "onExit");
}

function onExit(game, parent, talker, conversation) {
    conversation.exit();
	parent.getEncounter().setFaction("Hostile");
	game.clearRevealedAreas();
	game.scrollToCreature(talker);
}
