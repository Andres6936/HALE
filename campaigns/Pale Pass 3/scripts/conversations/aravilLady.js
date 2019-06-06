function startConversation(game, parent, target, conversation) {
    if (parent.get("armyComplete") != null) {
        conversation.addText("Good luck, my friend.");
        
        conversation.addResponse("Farewell.", "onExit");
    } else if (game.get("demonGateComplete") != null) {
        conversation.addText("You have returned!  I trust you succeeded in shutting down the gate.");
        
        conversation.addResponse("<span style=\"font-family: red;\">Continue</span>", "army01");
    } else if (parent.get("renarelQuestRecieved") != null) {
        conversation.addText("Travel to Renarel Lake and shutdown the infernal gate there.");
        
        conversation.addResponse("Farewell.", "onExit");
    } else if (parent.get("narkelCompleteDate") != null) {
        var completeDate = parent.get("narkelCompleteDate");
        
        var curDate = game.date().getTotalRoundsElapsed();
        
        if (curDate - completeDate > game.date().ROUNDS_PER_DAY * 2) {
            // we have waited long enough
            conversation.addText("Hello again.  Are you ready for another mission?");
            
            conversation.addResponse("Yes, what is it?", "renarel01");
            conversation.addResponse("Not yet.  Farewell.", "onExit");
            
        } else {
            // we haven't waited long enough
            conversation.addText("Come back and talk to me in a day or two, and perhaps I'll have something more to discuss.");
            
            conversation.addResponse("Farewell.", "onExit");
        }
    } else if (game.get("narkelQuestComplete") != null) {
        conversation.addText("You have returned.  What did you discover?");
        
        conversation.addResponse("<span style=\"font-family: red;\">Explain what Narkel told you about the focus.</span>", "narkel01");
    } else if (parent.get("tombQuestRecieved") != null) {
        conversation.addText("Travel to the Tomb of Narkel, and report back with what you find.  Good luck.");
        
        conversation.addResponse("Farewell.", "onExit");
    } else {
        conversation.addText("Welcome to our city, stranger.  Can I help you in some way?");
        conversation.addResponse("We wish to help you with the fight against the Master.", "convo02");
        conversation.addResponse("Not at the moment.  Farewell.", "onExit");
    }
}

function army01(game, parent, target, conversation) {
    conversation.addText("Your timing is excellent.  I have just recieved word that our scouts have made a major breakthrough in determining the location of the Master's hidden base, as well as the focus crystal.");
    
    conversation.addText("Even as we speak, our army should be setting up a forward camp.");
    
    conversation.addResponse("<span style=\"font-family: red;\">Continue</span>", "army02");
}

function army02(game, parent, target, conversation) {
    conversation.addText("You should head there immediately.  Find and speak with my general there.");
    
    conversation.addText("With your skill and knowledge of the Master's forces, you may be our best hope of defeating him.");
    
    conversation.addResponse("<span style=\"font-family: red;\">Continue</span>", "army03");
}

function army03(game, parent, target, conversation) {
    conversation.addText("Good luck, my friend.");
    
    conversation.addResponse("Farewell.", "onExit");
    
    game.revealWorldMapLocation("Army Camp");
    game.runExternalScript("quests/theMaster", "learnOfArmyLocation");
    parent.put("armyComplete", true);
}

function renarel01(game, parent, target, conversation) {
    conversation.addText("My agents have been in the field looking for any clues about the Master's plans or this mysterious focus you learned of from Narkel.");
    
    conversation.addResponse("<span style=\"font-family: red;\">Continue</span>", "renarel02");
}

function renarel02(game, parent, target, conversation) {
    conversation.addText("Unfortunately not much has turned up on the focus so far.  However, I have discovered something that is frightening indeed.");
    
    conversation.addResponse("<span style=\"font-family: red;\">Continue</span>", "renarel03");
}

function renarel03(game, parent, target, conversation) {
    conversation.addText("The Master is apparently in the process of opening up gates to the infernal planes, in order to summon more of his demonic kind here.");
    
    conversation.addText("We have reports that one such gate is already in operation, located on a small island in the center of Renarel Lake.");
    
    conversation.addResponse("<span style=\"font-family: red;\">Continue</span>", "renarel04");
}

function renarel04(game, parent, target, conversation) {
    conversation.addText("I need you to travel to the gate, and destroy the mages that are powering it.  Return to me when you have done this task.");
    
    parent.put("renarelQuestRecieved", true);
    game.revealWorldMapLocation("Lake Renarel");
    game.runExternalScript("quests/theMaster", "learnOfGate");
    
    conversation.addResponse("We will destroy this evil.  Farewell.", "onExit");
}

function narkel01(game, parent, target, conversation) {
    parent.put("narkelCompleteDate", game.date().getTotalRoundsElapsed());
    game.runExternalScript("quests/theMaster", "narkelComplete");
    
    conversation.addText("So it seems we must find and destroy this focus if we are to have any hope against the Master.");
    
    conversation.addText("We must make learning about it our top priority.");
    
    conversation.addResponse("<span style=\"font-family: red;\">Continue</span>", "narkel02");
}

function narkel02(game, parent, target, conversation) {
    conversation.addText("You have done amazingly well.  I am sure that you have an important part to play in the battle ahead.");
    
    conversation.addResponse("<span style=\"font-family: red;\">Continue</span>", "narkel03");
}

function narkel03(game, parent, target, conversation) {
    conversation.addText("For now though, we must bide our time while my agents try to find us some leads.");
    
    conversation.addText("I suggest you come speak with me again in a few days.  Perhaps then I will have more for you.");
    
    conversation.addResponse("Very well, I will speak to you soon.", "onExit");
}

function convo02(game, parent, target, conversation) {
    conversation.addText("Excellent, we need all the soldiers we can get.  Please talk to the captain of the guard, in the Commons.");
    
    conversation.addResponse("We have unique information on the nature of the Master.", "convo03");
    conversation.addResponse("We will, thank you.", "onExit");
}

function convo03(game, parent, target, conversation) {
    conversation.addText("Oh really?  What information is that?");
    
    conversation.addResponse("<span style=\"font-family: red;\">Explain your adventures and the Chosen Blood.</span>", "convo04");
}

function convo04(game, parent, target, conversation) {
    conversation.addText("<span style=\"font-family: blue;\">The Lady listens intently to your story, without interruption.</span>");
    
    conversation.addResponse("<span style=\"font-family: red;\">Continue</span>", "convo05");
}

function convo05(game, parent, target, conversation) {
    conversation.addText("What an amazing tale!  It is a bit much to take in all at once.  However, it does fit with what my scholars have managed to research so far.");
    
    conversation.addResponse("<span style=\"font-family: red;\">Continue</span>", "convo06");
}

function convo06(game, parent, target, conversation) {
    conversation.addText("In fact, things are starting to make more sense.  We have very few records of the time, but it seems the Master was fought and defeated by ancient mages hundreds of years ago.");
    
    conversation.addResponse("<span style=\"font-family: red;\">Continue</span>", "convo07");
}

function convo07(game, parent, target, conversation) {
    conversation.addText("I have something to ask of you, then.  We need to know more about how the Master was defeated last time, and there is one who may be able to answer.");
    
    conversation.addResponse("<span style=\"font-family: red;\">Continue</span>", "convo08");
}

function convo08(game, parent, target, conversation) {
    conversation.addText("To the west of here, carved into the mountainside, is the tomb of an ancient warrior, Narkel.");
    conversation.addText("From our records, it seems he was the leader of a great army that fought the Master.");
    
    conversation.addResponse("<span style=\"font-family: red;\">Continue</span>", "convo09");
    
    game.revealWorldMapLocation("Tomb of Narkel");
}

function convo09(game, parent, target, conversation) {
    conversation.addText("It is likely that his spirit still resides in the tomb, and you are a perfect choice to travel there, and see what you can uncover.");
    
    conversation.addText("We will continue our research here as best we can.");
    
    parent.put("tombQuestRecieved", true);
    game.runExternalScript("quests/theMaster", "learnOfTomb");
    
    conversation.addResponse("I will travel to the tomb, then.  Farewell", "onExit");
}


function onExit(game, parent, talker, conversation) {
    conversation.exit()
}
