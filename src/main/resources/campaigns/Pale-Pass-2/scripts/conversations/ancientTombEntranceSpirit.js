function startConversation(game, parent, target, conversation) {
    if (parent.get("convoComplete")) {
        conversation.addString("<div style=\"font-family: blue\">");
        conversation.addString("The skeletal guardian looks at you but does not speak.");
        conversation.addString("</div>");
        
        conversation.addResponse("<span style=\"font-family: red\">Leave</span>", "onExit");
        
    } else if (game.get("trialOfWillComplete") != null && game.get("trialOfBodyComplete") != null) {
        conversation.addText("You have completed the trials, mortal.  The door behind me leads to your prize.");
        
        conversation.addResponse("Farewell.", "onExitTrialsComplete");
    } else if (parent.get("alreadyTalked") != null) {
        conversation.addText("Hello again mortal.");
        
        conversation.addResponse("Tell me more about the trials in this tomb.", "trials01");
        conversation.addResponse("Farewell.", "onExit");
    } else {
        conversation.addText("What brings you into my tomb, mortal?");
        
        conversation.addResponse("We seek the key to surface.", "convo02");
        conversation.addResponse("I have no interest in talking with the dead.  Farewell.", "onExit");
    }
}

function convo02(game, parent, target, conversation) {
    conversation.addText("The key was broken and stolen from this place many years ago.  Only one piece remains.");
    
    conversation.addResponse("I know.  I need that piece.", "convo03");
    conversation.addResponse("I see.  Goodbye, then.", "onExit");
}

function convo03(game, parent, target, conversation) {
    conversation.addText("Only one of the chosen blood may enter.");
    
    conversation.addResponse("Chosen blood?", "convo04");
}

function convo04(game, parent, target, conversation) {
    conversation.addText("Long ago there was a great war between the surfacers and the dwarves.  In the end, the surfacers won.");
    conversation.addText("This tomb was built in the memory of those who fell in that war.");
    conversation.addText("The chosen blood is possessed only by the descendants of those who built the tomb.");
    
    conversation.addResponse("How do I know if I am one of the chosen blood?", "convo05");
    conversation.addResponse("A fascinating story.  Farewell.", "onExit");
}

function convo05(game, parent, target, conversation) {
    conversation.addText("There are two trials in this tomb built to test you: The Trial of Mind, and the Trial of Body.  Only one of the chosen blood may complete the trials and unlock the door behind me.");
    
    parent.put("alreadyTalked", true);
    
    game.runExternalScript("quests/theTombKey", "trialsStarted");
    
    conversation.addResponse("Tell me more about the trials.", "trials01");
    conversation.addResponse("I will complete the trials and return.  Farewell.", "onExit");
    conversation.addResponse("I am not sure about any of this, but it seems to be the only way forward.  Farewell.", "onExit");
}

function trials01(game, parent, target, conversation) {
    conversation.addText("There are two trials:  the Trial of the Mind, and the Trial of the Body.");
    
    conversation.addResponse("Tell me about the trial of the mind.", "trialMind");
    conversation.addResponse("Tell me about the trial of the body.", "trialBody");
    conversation.addResponse("That is all I need to know.  Farewell.", "onExit");
}

function trialMind(game, parent, target, conversation) {
    conversation.addText("The Trial of Mind is reached through the doorway to the North.  Your mind will be tested, to see that you have the intelligence and will of the chosen blood.");
    
    conversation.addResponse("Tell me about the trial of the body.", "trialBody");
    conversation.addResponse("That is all I need to know.  Farewell.", "onExit");
}

function trialBody(game, parent, target, conversation) {
    conversation.addText("The Trial of Body is reach through the doorway to the East.  Your fighting abilities to be tested, to see that you have the power and courage of the chosen blood.");
    
    conversation.addResponse("Tell me about the trial of the mind.", "trialMind");
    conversation.addResponse("That is all I need to know.  Farewell.", "onExit");
}

function onExit(game, parent, talker, conversation) {
    conversation.exit();
}

function onExitTrialsComplete(game, parent, talker, conversation) {
    conversation.exit();
    game.currentArea().getEntityWithID("ruraldoor_SE_unlockable").unlock();
    
    game.runExternalScript("quests/theTombKey", "trialsComplete");
    
    parent.put("convoComplete", true);
}
