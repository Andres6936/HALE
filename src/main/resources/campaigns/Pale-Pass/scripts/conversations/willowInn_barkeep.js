function startConversation(game, parent, target, conversation) {
    if (game.get("sleptWillowInn") == null) {
        conversation.addText("Welcome to the Willow Inn.  On your way to Fareach, eh?  What can I get you?");
        conversation.addResponse("<span style=\"font-family: red\">Barter</span>", "barter");
        conversation.addResponse("We'd like a room for the night.", "askRoom");
        conversation.addResponse("Nothing now.  Farewell.", "onExit");
    } else {
        conversation.addText("Hello again.  Can I get you something?");
        
        conversation.addResponse("<span style=\"font-family: red\">Barter</span>", "barter");
        conversation.addResponse("Nothing now.  Farewell.", "onExit");
    }
}

function askRoom(game, parent, talker, conversation) {
    conversation.addText("I have a room available for 5 silver for the night.  Let me know when you are ready to retire.");
    
    if (game.getPartyCurrency().getValue() >= 50) {
        conversation.addResponse("<span style=\"font-family: red\">Pay 5 SP and sleep</span>", "sleep");
    }
    conversation.addResponse("I'll talk with some of the other guests first and ask you again later.", "onExit");
}

function sleep(game, parent, talker, conversation) {
    conversation.exit();
    
    var currency = game.getPartyCurrency();
    
    if (currency.getValue() >= 50) {
        currency.addSP(-5);
        
        game.date().incrementHours(8);
        
        game.runExternalScript("quests/aStrangeDream", "startQuest");
        game.runExternalScript("quests/joiningTheArmy", "startQuest");
    }
}

function barter(game, parent, talker, conversation) {
    conversation.exit();
    
    game.showMerchant("willowInn_barkeep");
}

function onExit(game, parent, talker, conversation) {
    conversation.exit()
}
