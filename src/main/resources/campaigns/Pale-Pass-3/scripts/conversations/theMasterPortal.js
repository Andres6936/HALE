function startConversation(game, parent, target, conversation) {
	conversation.exit();
	
	game.runExternalScript("ai/theMaster", "startCutscene");
}