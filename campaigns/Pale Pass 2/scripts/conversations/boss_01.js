function startConversation(game, parent, target, conversation) {
    conversation.addText("Trying to return to the surface, eh?  Know that you will never see the sun again!");
    conversation.addResponse("Who are you?", "convo01a");
	conversation.addResponse("Another servant of the master, I presume?", "convo01b");
}

function convo01a(game, parent, target, conversation) {
	conversation.addText("My name is unimportant.  Surely you know that I serve the Master.  In his name I shall strike you down!");
	
	conversation.addResponse("I will stop you, and then I will stop the Master.", "convo02");
}

function convo01b(game, parent, target, conversation) {
	conversation.addText("Very perceptive, but you will need more than wits to save you!");
	
	conversation.addResponse("I will stop you, and then I will stop the Master.", "convo02");
}

function convo02(game, parent, target, conversation) {
	conversation.addText("Thats unlikely, I'm afraid.  Already, he has raised an army that has the nations of the surface trembling in fear.");
	
	conversation.addText("Soon, you and the last few remaining descendants will be dead, and his power will be complete.");
	
	conversation.addResponse("Stand aside, and we will let you live.", "convo02b");
	conversation.addResponse("Not if I have a say in it! <span style=\"font-family: red\">Fight</span>", "onExit");
}

function convo02b(game, parent, target, conversation) {
	conversation.addText("You are in no position to bargain, fool.");
	
	conversation.addText("I will not leave you alive, only to let another steal the glory of your death.");
	
	conversation.addResponse("Then prepare to die! <span style=\"font-family: red\">Fight</span>", "onExit");
}

function onExit(game, parent, talker, conversation) {
    parent.getEncounter().setFaction("Hostile");
    conversation.exit();
    game.clearRevealedAreas();
}
