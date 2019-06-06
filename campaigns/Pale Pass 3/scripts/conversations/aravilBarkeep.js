function startConversation(game, parent, target, conversation) {
    conversation.addText("Hello, friend.  What do you need?");
    
    if (game.get("theSmugglerQuestStarted") == null) {
        conversation.addResponse("Heard any good rumors lately?", "convo02");
    }
    
    
    conversation.addResponse("Nothing today, thanks.", "onExit");
}

function convo02(game, parent, target, conversation) {
    conversation.addText("I have been hearing some stories about some buried treasure lately.  Care to buy a drink and hear about it?");
    
    conversation.addResponse("<span style=\"font-family: red\">Pay 2 CP</span> Sure, lets hear it.", "convo03");
    conversation.addResponse("Another time perhaps.  Farewell.", "onExit");
}

function convo03(game, parent, target, conversation) {
    game.getPartyCurrency().addCP(-2);
    
    conversation.addText("Well, as the story goes, this all takes place out in Greenrange Forest.");
    
    conversation.addResponse("Where is that located?", "convo04");
    conversation.addResponse("<span style=\"font-family: red\">Continue Listening.</span>", "convo04");
}

function convo04(game, parent, target, conversation) {
    game.revealWorldMapLocation("Greenrange Forest");
    
    conversation.addText("The forest is a ways East of here.  The story, anyway, is that this old weapons smuggler had got his hands on some real rare quality stuff.");
    
    conversation.addText("He had set up a camp somewhere out in those woods, and was selling his weapons out of a cave.");
    
    conversation.addResponse("<span style=\"font-family: red\">Continue Listening.</span>", "convo05");
}

function convo05(game, parent, target, conversation) {
    conversation.addText("Of course, the city guard found out and came a'knockin.");
    
    conversation.addText("The old smuggler was too quick for 'em though, and he escaped.  Headed way up north, they say.");
    
    conversation.addResponse("<span style=\"font-family: red\">Continue Listening.</span>", "convo06");
}

function convo06(game, parent, target, conversation) {
    conversation.addText("Now the guard searched the forest high and low for the smuggler's stash.  They found some weapons, but not nearly enough to account for all the business he had been doing.");
    
    conversation.addText("So the story goes, his big stash is still somewhere out there in them woods.  Probably close to his old cave hideout, but no one knows where.");
    
	game.activateTransition("greenrangeForestToCave");
    game.put("theSmugglerQuestStarted", true);
    game.runExternalScript("quests/theSmuggler", "startQuest");
    
    conversation.addResponse("Thanks for the tale, bartender.  Farewell.", "onExit");
}


function onExit(game, parent, talker, conversation) {
    conversation.exit()
}
