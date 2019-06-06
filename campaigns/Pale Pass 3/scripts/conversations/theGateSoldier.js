
function startConversation(game, parent, target, conversation) {
    conversation.addText("Hail, traveler!  How did you end up in these woods?");
    conversation.addResponse("Its a long story.", "convo02");
    conversation.addResponse("Well, first there was this cave collapse, and then...", "convo02");
}

function convo02(game, parent, target, conversation) {
    conversation.addText("It is no matter.");

    conversation.addText("You had best be careful where you step, though.  The Master's army controls this countryside.");
    
    conversation.addResponse("The Master's army?", "convo03");
}

function convo03(game, parent, target, conversation) {
    conversation.addText("Have you been living under a rock these past few months?  The Master's army has swept in from the North, laying waste to the villages in this region.");
    
    conversation.addText("The King's army is holding Aravil for now.");
    
    conversation.addResponse("I wish to help fight the Master.", "convo04a");
    conversation.addResponse("Which way is it to Aravil?", "convo04b");
}

function convo04a(game, parent, target, conversation) {
    conversation.addText("Then I suggest you meet up with the army currently in Aravil.  The gate commander will decide whether you are allowed into the city.");
    
    conversation.addResponse("<span style=\"font-family: red\">Continue</span>", "convo05");
}

function convo04b(game, parent, target, conversation) {
    conversation.addText("You are not from around here, are you?  You simply follow the road East; you cannot miss it.");
    
    conversation.addText("I must warn you though, these are perilous times, and you may not be allowed into the city.");
    
    conversation.addResponse("<span style=\"font-family: red\">Continue</span>", "convo05");
}

function convo05(game, parent, target, conversation) {
    conversation.addText("It is not safe for either of us here.  I must be off.  Farewell.");
    
    conversation.addResponse("Goodbye.", "onExit");
}

function moveAndRemove(game, parent) {
    parent.timer.reset();
    
    game.ai.moveTowards(parent, 37, 6, 0);
    game.sleep(1000);
    
    game.unlockInterface();
    game.currentArea().removeEntity(parent);
    game.scrollToCreature(game.getParty().get(0));
}

function onExit(game, parent, talker, conversation) {
    game.lockInterface(3.0);
    
    game.runExternalScriptWait("conversations/theGateSoldier", "moveAndRemove", 1.0, parent);
    game.runExternalScript("quests/theMaster", "learnOfArmy");
    game.revealWorldMapLocation("Aravil West Gate");
    
    conversation.exit();
}
