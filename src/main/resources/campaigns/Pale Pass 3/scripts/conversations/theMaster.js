function startConversation(game, parent, target, conversation) {
	if (parent.get("introDone") != null) {
		conversation.addString("<div style=\"font-family: blue\">");
		conversation.addString("The Master is visibly weakened by the destruction of the crystal, but he does not seem to have been unsummoned as you had hoped.");
		conversation.addString("</div>");
		
		conversation.addResponse("<span style=\"font-family: red\">Continue</span>", "convo02");
	} else {
		conversation.addString("<div style=\"font-family: blue\">");
		conversation.addString("You have seen the figure now before you many times in your dreams.  The Master.");
		conversation.addString("</div>");
	
		conversation.addString("<div style=\"font-family: blue; margin-top: 1em;\">");
		conversation.addString("The ancient demon does not appear to be surprised to see you.");
		conversation.addString("</div>");
	
		conversation.addResponse("<span style=\"font-family: red\">Continue</span>", "intro02");
	}
}

function convo02(game, parent, target, conversation) {
	conversation.addString("<div style=\"font-family: blue\">");
	conversation.addString("Your best hope is to destroy his avatar on this plane.  Perhaps that will banish him permanently.");
	conversation.addString("</div>");
	
	conversation.addResponse("<span style=\"font-family: red\">Continue the Fight</span>", "onExit");
}

function onExit(game, parent, target, conversation) {
	conversation.exit();
}

function intro02(game, parent, target, conversation) {
	conversation.addString("<div style=\"font-family: blue\">");
    conversation.addString("He does not speak, but instead motions to his minions, and lets out an otherworldly roar.");
    conversation.addString("</div>");
	
	conversation.addString("<div style=\"font-family: blue; margin-top: 1em;\">");
    conversation.addString("Your only hope of stopping him is to destroy the focus crystal behind him.");
    conversation.addString("</div>");
	
	conversation.addResponse("<span style=\"font-family: red\">Fight</span>", "onExitIntro");
}

function onExitIntro(game, parent, talker, conversation) {
	parent.put("introDone", true);
    parent.getEncounter().setFaction("Hostile");
    conversation.exit();
    game.clearRevealedAreas();
}
