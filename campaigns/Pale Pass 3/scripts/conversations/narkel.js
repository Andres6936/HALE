function startConversation(game, parent, target, conversation) {
    if (parent.get("alreadyTalked") != null) {
        conversation.addText("I have helped you all I can.  Go now, mortal.");
        
        conversation.addResponse("Farewell.", "onExit");
    } else {
    
        conversation.addText("Why have you come, mortal?  Surely you have realized that the living are not welcome here.");
    
        conversation.addResponse("You are the spirit of the warrior Narkel?", "convo01a");
        conversation.addResponse("We seek information about the Master.", "convo02");
    }
}

function convo01a(game, parent, target, conversation) {
    conversation.addText("That I am.  So tell me, why have you sought me out?");
    
    conversation.addResponse("We seek information about the Master.", "convo02");
}

function convo02(game, parent, target, conversation) {
    conversation.addText("The Master?  You of course mean the old enemy.  So he has risen again?");
    
    conversation.addResponse("Yes, and his armies are on the move.", "convo03");
}

function convo03(game, parent, target, conversation) {
    conversation.addText("I feared this day would come.  The trap that ensnared him once grows thin.");
    
    conversation.addResponse("<span style=\"font-family: red;\">Continue</span>", "convo04");
}

function convo04(game, parent, target, conversation) {
    conversation.addText("I can sense his presence in you, chosen blood.  The master seeks to destroy you, so that his soul can be completely free of its prison.");
    
    conversation.addResponse("So how do we defeat him?", "convo05");
}

function convo05(game, parent, target, conversation) {
    conversation.addText("The Master is a powerful demon, not of this world.  He cannot be truly defeated by any means mortals possess.");
    
    conversation.addResponse("<span style=\"font-family: red;\">Continue</span>", "convo06");
}

function convo06(game, parent, target, conversation) {
    conversation.addText("Ages ago, I led an army that fought with the old enemy.  We could not stop him, but we were able to delay him.");
    
    conversation.addResponse("<span style=\"font-family: red;\">Continue</span>", "convo07");
}

function convo07(game, parent, target, conversation) {
    conversation.addText("The most powerful mages of my time cast a spell imprisoning the pieces of his soul in them and their descendants.  Through some twist of fate, it seems that their lines now grow thin.");
    
    conversation.addResponse("<span style=\"font-family: red;\">Continue</span>", "convo08");
}

function convo08(game, parent, target, conversation) {
    conversation.addText("But magic has since faded from the world.  No one alive possesses the power to bind him again.");
    
    conversation.addResponse("Then how can we stop him?", "convo09");
}

function convo09(game, parent, target, conversation) {
    conversation.addText("There is one possibility.  The Master is a powerful demon.  At some point in the distant past, he must have been bound to this plane from his native one.");
    
    conversation.addResponse("<span style=\"font-family: red;\">Continue</span>", "convo10");
}

function convo10(game, parent, target, conversation) {
    conversation.addText("The spell to bind such a creature would require a powerful focus, probably an exceptionally large and pure crystal.  If the focus were found and destroyed, the spell would end and the Master would be returned to his home plane.");
    
    conversation.addResponse("So where is this focus?", "convo11");
}

function convo11(game, parent, target, conversation) {
    conversation.addText("Therein is the problem.  My people searched using our most powerful magic, but we could find no trace of the focus.");
    
    conversation.addText("My only guess is that the Master will probably keep it close to him, so that he can guard it.");
    
    conversation.addResponse("That is not much to go on.", "convo12");
    conversation.addResponse("Thank you for your help, spirit.", "convo12");
}

function convo12(game, parent, target, conversation) {
    conversation.addText("Sadly, that is all the help I can provide.  Go now, so that I can return to the slumber of death.");
    
    conversation.addResponse("Farewell, Narkel.", "onExit");
    
    parent.put("alreadyTalked", true);
    game.put("narkelQuestComplete", true);
    game.runExternalScript("quests/theMaster", "narkel");
}

function onExit(game, parent, talker, conversation) {
    conversation.exit();
}
