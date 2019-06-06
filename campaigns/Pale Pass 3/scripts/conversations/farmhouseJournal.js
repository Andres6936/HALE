function startConversation(game, parent, target, conversation) {
    conversation.addString("<div style=\"font-family: blue\">");
    conversation.addString("This is the journal of a farmer living in Greenrange Forest.");
    conversation.addString("</div>");
	
    conversation.addResponse("<span style=\"font-family: red\">Open the Journal</span>", "convoMain");
}

function convoMain(game, parent, target, conversation) {
	conversation.addString("<div style=\"font-family: blue\">");
    conversation.addString("The journal consists of many lengthy entries on crops, trading of seeds and fertilizers and the weather.");
    conversation.addString("</div>");
	
	conversation.addResponse("<span style=\"font-family: red\">Skip to the final entry</span>", "convo02");
}

function convo02(game, parent, target, conversation) {
	conversation.addString("<div style=\"font-family: blue\">");
    conversation.addString("The wolves came for my animals again last night.  I am going to have to make a stand, or I'll be ruined.");
    conversation.addString("</div>");
	
	conversation.addString("<div style=\"font-family: blue; margin-top: 1em;\">");
    conversation.addString("I think they are coming from down south, somewhere in the canyon.  Tonight, I will make my stand.");
    conversation.addString("</div>");

	conversation.addResponse("<span style=\"font-family: red\">Close the Journal</span>", "onExit");
	
	game.runExternalScript("quests/fullMoon", "readJournal");
	game.activateTransition("greenrangeCanyonToWerewolfDen");
}

function onExit(game, parent, talker, conversation) {
    conversation.exit();
}
