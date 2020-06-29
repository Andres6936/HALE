
function startConversation(game, parent, target, conversation) {
    conversation.addText("So, here you are.");
    conversation.addResponse("Who are you?", "askWho");
    conversation.addResponse("You knew I was coming?", "askComing");
}

function askWho(game, parent, target, conversation) {
    conversation.addText("I'm the one who is going to end your life.  The master has chosen me specifically.  It is a great honor.");
    
    conversation.addResponse("Who is this master you speak of?", "askMaster");
}

function askComing(game, parent, target, conversation) {
    conversation.addText("Yes, the master predicted that you might make it this far.  He gave me the honor of killing you.");
    
    conversation.addResponse("Who is this master you speak of?", "askMaster");
}

function askMaster(game, parent, target, conversation) {
    conversation.addText("You do not even know who you fight?  Know only this then, that his name is Elrinar, and he is the one who has destroyed you.");
    
    conversation.addResponse("What if I turn around, and forget this ever happened?", "askForget");
    conversation.addResponse("Then prepare to die! <span style=\"font-family: red\">Fight</span>", "onExit");
}

function askForget(game, parent, target, conversation) {
    conversation.addText("I'm afraid it is much to late for that.  No, we will fight here and now.");
    
    conversation.addResponse("Then prepare to die! <span style=\"font-family: red\">Fight</span>", "onExit");
}


function onExit(game, parent, talker, conversation) {
    conversation.exit();
    parent.getEncounter().setFaction("Hostile");
	game.clearRevealedAreas();
}
