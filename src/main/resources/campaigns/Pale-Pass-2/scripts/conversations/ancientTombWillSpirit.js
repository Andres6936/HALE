
function startConversation(game, parent, target, conversation) {
    if (parent.get("alreadyTalked") != null) {
        conversation.addText("Go now, chosen blood.  Find the key and save yourself.");
    
        conversation.addResponse("Farewell.", "onExit");
    } else {
        conversation.addText("You have come, chosen blood.");
    
        conversation.addResponse("Who are you?", "convo01a");
        conversation.addResponse("I have chosen blood?", "convo01b");
        conversation.addResponse("You were expecting me?", "convo01c");
    }
}

function convo01a(game, parent, target, conversation) {
    conversation.addText("I am a servant of the master, whom you already know.");
    conversation.addText("Fear not, as I mean you no harm.");
    
    if (parent.get("learnConvo01a") != null && parent.get("learnConvo01b") != null) {
        conversation.addResponse("Tell me more about the master.  Who is he?", "convo02");
    }
    
    conversation.addResponse("I have chosen blood?", "convo01b");
    conversation.addResponse("You were expecting me?", "convo01c");
    
    parent.put("learnConvo01a", true);
}

function convo01b(game, parent, target, conversation) {
    conversation.addText("Yes, I can sense the stands of fate around you.");
    conversation.addText("You may be the only one left, the last descendant of an ancient order.");
    
    if (parent.get("learnConvo01a") != null && parent.get("learnConvo01b") != null) {
        conversation.addResponse("Tell me more about the master.  Who is he?", "convo02");
    }
    
    conversation.addResponse("Who are you?", "convo01a");
    conversation.addResponse("You were expecting me?", "convo01c");
    
    parent.put("learnConvo01b", true);
}

function convo01c(game, parent, target, conversation) {
    conversation.addText("Not expecting exactly.  Only I knew that one day, you must come.  And here you are.");
    
    if (parent.get("learnConvo01a") != null && parent.get("learnConvo01b") != null) {
        conversation.addResponse("Tell me more about the master.  Who is he?", "convo02");
    }
    
    conversation.addResponse("Who are you?", "convo01a");
    conversation.addResponse("I have chosen blood?", "convo01b");
}

function convo02(game, parent, target, conversation) {
    conversation.addText("I do not know his true nature.  He was imprisoned here for hundreds of years, until he broke free.");
    
    conversation.addText("He has made it his mission to hunt and destroy down you and your kind, chosen blood.");
    
    conversation.addResponse("Why does he want me dead?", "convo03");
}

function convo03(game, parent, target, conversation) {
    conversation.addText("Ages ago, the master led a mighty army of the dwarves in these caves into a terrible war.");
    
    conversation.addText("He wished to bring his army to the surface, and conquer that world as well.");
    
    conversation.addResponse("<span style=\"font-family: red\">Continue</span>", "convo04");
}

function convo04(game, parent, target, conversation) {
    conversation.addText("There was a great battle, and all seemed lost for the surfacers.");
    
    conversation.addText("Their last hope was an order of nine powerful mages.");
    
    conversation.addResponse("<span style=\"font-family: red\">Continue</span>", "convo05");
}

function convo05(game, parent, target, conversation) {
    conversation.addText("The mages were some of the most powerful this world has ever known.  They discovered the secret to a spell strong enough to stop the master.");
    
    conversation.addResponse("<span style=\"font-family: red\">Continue</span>", "convo06");
}

function convo06(game, parent, target, conversation) {
    conversation.addText("They cast the spell, imprisoning the Master's soul inside of themselves.");
    conversation.addText("The Master's body, they imprisoned in this tomb.");
    
    conversation.addResponse("<span style=\"font-family: red\">Continue</span>", "convo07");
}

function convo07(game, parent, target, conversation) {
    conversation.addText("The pieces of his soul are still splintered, existing in the descendants of those ancient mages.");
    conversation.addText("You are one such descendant, and one of the last.");
    
    conversation.addResponse("<span style=\"font-family: red\">Continue</span>", "convo08");
}

function convo08(game, parent, target, conversation) {
    conversation.addText("The master seeks to end the line of the nine mages, and reunite his soul in its true power.");
    
    conversation.addText("If he succeeds, the world will once again tremble at his might.");
    
    conversation.addResponse("Why have you told me all this?  I thought you were his servant?", "convo09a");
    conversation.addResponse("So how do we stop him?", "convo10");
}

function convo09a(game, parent, target, conversation) {
    conversation.addText("Sitting in this stone form for a thousand years can dull one's sense of loyalty.");
    
    conversation.addResponse("Is there any way I can help you?", "convo09b");
    conversation.addResponse("So how do we stop the Master?", "convo10");
}

function convo09b(game, parent, target, conversation) {
    conversation.addText("I am bound by the Master's power.  Only his destruction can free me.");
    
    conversation.addResponse("So how do we stop the Master?", "convo10");
}

function convo10(game, parent, target, conversation) {
    conversation.addText("That, I do not know.  I believe however, that you may be the only one left who can stop him.")
    
    conversation.addResponse("<span style=\"font-family: red\">Continue</span>", "convo11");
}

function convo11(game, parent, target, conversation) {
    conversation.addText("You must stop him to save yourself.  Go now.  You can tell the guardian that you have completed the Trial of Will.");
    
    conversation.addResponse("Thank you.  Farewell.", "onExit");
}


function onExit(game, parent, talker, conversation) {
    parent.put("alreadyTalked", true);
    
    game.put("trialOfWillComplete", true);
    
    game.runExternalScript("quests/theMaster", "learnOfMaster");
    
    conversation.exit();
}
