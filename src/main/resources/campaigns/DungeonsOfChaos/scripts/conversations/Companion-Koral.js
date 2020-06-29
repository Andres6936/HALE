function startConversation(game, parent, talker, conv) {
	if (parent.get("inParty") == true) {
	    conv.addTextWithFont("Before you stands Koral, your companion.", "medium-italic");
	    conv.addTextWithFont("What do you need?", "medium");
		
		conv.addResponse("I think we should go our separate ways.", "askLeave");
	} else {
	    if (parent.get("met") == true) {
		    conv.addTextWithFont("Before you stands Koral.", "medium-italic");
	        conv.addTextWithFont("What do you need?", "medium");
		} else {
		    conv.addTextWithFont("Before you stands a tall, sturdy woman.  She has blue skin with a sheen almost like a fish's scales, and long green hair.", "medium-italic");
            conv.addTextWithFont("She looks you over carefully before speaking.", "medium-italic");
	        conv.addTextWithFont("I am Koral.  Can I do something for you, stranger?.", "medium");
		
		    parent.put("met", true);
		}
		
	    conv.addResponse("Would you be interested in joining me in exploring the dungeon?", "askJoin");
	}
	
    conv.addResponse("Nothing now, thanks.", "onExit");
}

function askLeave(game, parent, talker, conv) {
    game.removeCompanion(parent);
	parent.put("inParty", false);
	
	conv.addTextWithFont("Very well.  I will be at the Hardy Hearth if you have need of me.", "medium");
	
	conv.addResponse("Farewell.", "resetLocAndExit");
}

function askJoin(game, parent, talker, conv) {
    game.addCompanion(parent);
	parent.put("inParty", true);

    conv.addTextWithFont("It is better to go together than alone.  I will join you.", "medium");
	
	conv.addResponse("Let's get going, then.", "onExit");
}

function resetLocAndExit(game, parent, talker, conv) {
    parent.setLocation(game.campaign().getArea("HardyHearth"), 16, 12);
    conv.exit();
}

function onExit(game, parent, talker, conv) {
    conv.exit();
}

