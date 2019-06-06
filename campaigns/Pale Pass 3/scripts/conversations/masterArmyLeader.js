function startConversation(game, parent, target, conversation) {
    conversation.addText("So you have come.  Know that you are too late; the crystal is already gone.  The Master himself now has it.");
	
    conversation.addResponse("<span style=\"font-family: red\">Continue</span>", "convo02");
}

function convo02(game, parent, target, conversation) {
	conversation.addText("And now you must die.  Attack!");
	
	conversation.addResponse("<span style=\"font-family: red\">Fight</span>", "onExit");
}

function onExit(game, parent, talker, conversation) {
    parent.getEncounter().setFaction("Hostile");
    conversation.exit();
    game.clearRevealedAreas();
	game.runExternalScript("quests/theMaster", "crystalLost");
	game.put("focusCrystalLost", true);
}
