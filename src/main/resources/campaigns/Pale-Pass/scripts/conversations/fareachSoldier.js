function startConversation(game, parent, target, conversation) {
    conversation.addText("What are you doing in here?");
    conversation.addResponse("We are looking to join the army.", "askJoin");
    conversation.addResponse("Just looking around.  Farewell", "onExit");
}

function askJoin(game, parent, talker, conversation) {
    conversation.addText("I'm afraid you are too late for that.");
    conversation.addText("The army has already set off; heading East towards the Black River Crossing.");
    
    conversation.addResponse("But we've come so far already!", "convo2");
}

function convo2(game, parent, talker, conversation) {
    conversation.addText("Well, you can head towards Black River Crossing and try to catch up, but you might just end up missing the action anyway.");
    conversation.addText("I'm actually headed west to Kings Rock myself.");
    
    game.runExternalScript("quests/joiningTheArmy", "followArmy");
    game.put("talkedToFareachSoldier", true);
    game.revealWorldMapLocation("Black River Crossing");
    
    conversation.addResponse("I guess I don't have much choice.  Farewell.", "onExit");
    conversation.addResponse("I will make haste, and hopefully catch a taste of battle!  Farewell.", "onExit");
}

function onExit(game, parent, talker, conversation) {
    conversation.exit()
}
