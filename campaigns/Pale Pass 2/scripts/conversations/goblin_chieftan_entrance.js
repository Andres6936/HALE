
function startConversation(game, parent, target, conversation) {
    var alreadyTalked = parent.get("alreadyTalked");
    
    if (game.get("killedGrimbok") != null) {
        conversation.addText("You have returned.  Do you have the head of the beast?");
        
        conversation.addResponse("Yes, here it is.", "convoB201");
        
    } else if (alreadyTalked != null) {
        conversation.addText("Find the Grimbok to the East.  Return to me with the beast's head.  Then we will talk.");
        conversation.addResponse("<span style=\"font-family: red\">Leave</span>", "onExit");
    } else {
        parent.put("alreadyTalked", true);
    
        conversation.addText("Grob tal na la, ofrin je yabluk!");
        conversation.addResponse("We cannot understand you.", "convo01");
    }
}

function convo01(game, parent, target, conversation) {
    conversation.addText("Grob tal...surfacer?");
    
    conversation.addResponse("<span style=\"font-family: red\">Continue</span>", "convo02");
}

function convo02(game, parent, target, conversation) {
    conversation.addText("You are...surfacers?  It is true then, the collapse.");
    conversation.addText("Why have you been killing my people?");
    
    conversation.addResponse("We were defending ourselves.  They attacked first.", "convo03a");
    conversation.addResponse("What do you mean, we haven't killed anyone?", "convo03b");
    conversation.addResponse("Because you are just so damn ugly.", "convo03c");
}

function convo03a(game, parent, target, conversation) {
    conversation.addText("I see.  For that, I am sorry.  But what would you do, to foreigners in your lands?");
    
    conversation.addResponse("So what happens now?", "convo04");
}

function convo03b(game, parent, target, conversation) {
    conversation.addText("My people may seem fools to you, but I am not witless.  You would do well to remember that.");
    
    conversation.addResponse("So what happens now?", "convo04");
}

function convo03c(game, parent, target, conversation) {
    conversation.addText("You have fallen into my trap, are totally at my mercy, and still you wish to fight?  Brave.  Brave and foolish.");
    
    conversation.addResponse("So what happens now?", "convo04");
}

function convo04(game, parent, target, conversation) {
    conversation.addText("I have a proposal for you.  You wish to return to your surface lands, yes?  I can help you.");
    
    conversation.addResponse("Yes.  Please help us.", "convo05a");
    conversation.addResponse("And what do you want in return?", "convo05b");
}

function convo05a(game, parent, target, conversation) {
    conversation.addText("I will help you.  But first, I require a service.");
    
    conversation.addText("There is a terrible beast living in the caves to the east of here.  It has menaced my people for years, and grows ever more bold in its attacks.");
    conversation.addText("You are obviously capable Warriors.  Dispatch this creature, and you will have gained my trust and help.");
    
    conversation.addResponse("Very well, we accept.", "convo07");
    conversation.addResponse("And what if we refuse?", "convo06");
}

function convo05b(game, parent, target, conversation) {
    conversation.addText("There is always that, of course.");
    
    conversation.addText("There is a terrible beast living in the caves to the east of here.  It has menaced my people for years, and grows ever more bold in its attacks.");
    conversation.addText("You are obviously capable Warriors.  Dispatch this creature, and you will have gained my trust and help.");
    
    conversation.addResponse("Very well, we accept.", "convo07");
    conversation.addResponse("And what if we refuse?", "convo06");
}

function convo06(game, parent, target, conversation) {
    conversation.addText("You truly have no choice.  In order to return to the surface, you must pass through my domain.");
    
    conversation.addResponse("Very well, we accept.", "convo07");
}

function convo07(game, parent, target, conversation) {
    conversation.addText("Excellent.  Follow the path to the East, and you will find the lair of the Grimbok.  Return to me with the creature's head, and I will honor our agreement and help you return to the surface.");
    
    conversation.addResponse("Goodbye for now, then.", "onExitFirstMeeting");
}

function onExitFirstMeeting(game, parent, talker, conversation) {
    conversation.exit();
    
    game.clearRevealedAreas();
    
    game.runExternalScript("quests/lairOfTheGrimbok", "startQuest");
    
    var door = game.currentArea().getEntityWithID("goblinCityPitGrimbakExit");
    door.unlock(parent);
}

function convoB201(game, parent, target, conversation) {
    conversation.addText("I am impressed.  You have done a valuable service to our clan.");
    
    conversation.addText("In return, we will aid you as best we can.  Our city is now open to you.  Find me in my chambers at the south end of the city when you wish to discuss this further.");
    
	game.addPartyXP(10 * game.ruleset().getValue("EncounterXPFactor"));
	
    conversation.addResponse("Very well.", "onExitGrimbok");
}

function onExit(game, parent, talker, conversation) {
    conversation.exit();
}

function onExitGrimbok(game, parent, target, conversation) {
    game.runExternalScript("quests/lairOfTheGrimbok", "endQuest");
    
    game.runExternalScript("quests/theMaster", "goblinTrust");
    
    game.currentArea().removeEntity(parent);
    
    game.getParty().removeItem("grimbok_head");
    
    var door = game.currentArea().getEntityWithID("goblinCityPitExit");
    door.unlock(parent);
    
    conversation.exit();
}
