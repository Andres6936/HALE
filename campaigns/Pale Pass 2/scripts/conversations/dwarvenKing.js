function startConversation(game, parent, target, conversation) {
    if (parent.get("questComplete") != null) {
		conversation.addText("Best of luck on your travels, surfacer.");
		
		conversation.addResponse("Farewell.", "onExit");
	} else if (game.get("dwarvenAxeRetrieved") != null) {
        conversation.addText("Hello surfacer.  Do you have the axe?");
        
        conversation.addResponse("Yes, here it is.", "giveAxe");
        conversation.addResponse("Not yet.  Farewell.", "onExit");
    } else if (parent.get("axeQuestStarted") != null) {
        conversation.addText("Hello surfacer.  Do you have the axe?");
        
        conversation.addResponse("Not yet.  Farewell.", "onExit");
    } else {
        conversation.addText("Greetings surfacer.  Why have you come to my city?");
    
        conversation.addResponse("We seek to travel through the gate to the surface.  We heard you have a key fragment.", "convo02");
    }
}

function convo02(game, parent, target, conversation) {
    conversation.addText("Interesting, so you have been stranded down here with the rest of us?");
    
    conversation.addResponse("Yes, we were caught in a cave collapse.", "convo03");
    conversation.addResponse("You are stranded here too?", "convo02a");
}

function convo02a(game, parent, target, conversation) {
    conversation.addText("Only in the sense that our ancestors were trapped down here thousands of years ago.");
    
    conversation.addText("A few generations ago my people attempted to invade the surface, but we failed.  It was folly then, and would remain so now.  We no longer have a need or desire to return.");
    
    conversation.addResponse("We were caught in a cave collapse.", "convo03");
}

function convo03(game, parent, target, conversation) {
    conversation.addText("That is interesting.  We don't see many surfacers this far from the gate.");
    
    conversation.addText("But tell me, why should we help you?");
    
    conversation.addResponse("Because you like us?", "convo04a");
    conversation.addResponse("What do you want in return?", "convo04b");
}

function convo04a(game, parent, target, conversation) {
    conversation.addText("Ha ha!  I think I do.  Unfortunately for you, when you are king such things cannot be allowed to affect your judgement.");
    
    conversation.addResponse("<span style=\"font-family: red\">Continue</span>", "convo04b");
}

function convo04b(game, parent, target, conversation) {
    conversation.addText("Yes, you must help us first.  Then, you may have the key.");
    
    conversation.addResponse("<span style=\"font-family: red\">Continue</span>", "convo05");
}

function convo05(game, parent, target, conversation) {
    conversation.addText("Some years ago, during my father's reign, a group of our warriors splintered off from the rest of us and took to slaving.  They currently make camp not far from here.");
    
    conversation.addText("When they left, they took an heirloom of my family, a unique axe.");
    
    conversation.addResponse("<span style=\"font-family: red\">Continue</span>", "convo06");
}

function convo06(game, parent, target, conversation) {
    conversation.addText("My scouts have recently learned that the slavers in turn lost the axe, deep in some tunnels to the south.");
    
    conversation.addText("It is dangerous for my men to pass through the territory of the slavers and the goblins, and I do not wish to start a war by moving in force.");
    
    conversation.addResponse("<span style=\"font-family: red\">Continue</span>", "convo07");
}

function convo07(game, parent, target, conversation) {
    conversation.addText("However, you should be able to move without attracting too much attention.");
    
    conversation.addText("I will mark the location of the axe on your map.  Return with it, and you shall have your key fragment.");
    
    conversation.addResponse("I will do so.  Farewell.", "onExitAxeQuest");
}

function onExitAxeQuest(game, parent, talker, conversation) {
    parent.put("axeQuestStarted", true);
	
    game.runExternalScript("quests/theDwarvenKey", "axeQuest");
    game.revealWorldMapLocation("Deep Tunnels");
    
    conversation.exit();
}

function giveAxe(game, parent, target, conversation) {
	game.addMessage("You gave away the Axe of Gan Tok.");
    game.getParty().removeItem("axe_dwarven");
	game.addPartyXP(10 * game.ruleset().getValue("EncounterXPFactor"));
	
	parent.put("questComplete", true);
	
	game.runExternalScript("quests/theDwarvenKey", "questComplete");
	
	conversation.addText("I knew there was something special about you!");
	conversation.addText("You have done us a great honor, surfacer.  As promised you may have the key fragment.");
	
	conversation.addResponse("Thank you.", "giveAxe2");
}

function giveAxe2(game, parent, target, conversation) {
	target.inventory.getUnequippedItems().add("dwarvenKeyFragment");
    game.addMessage(target.getName() + " was given the Key Fragment.");

	conversation.addText("Go now, with our blessing.");
	
	conversation.addResponse("Farewell.", "onExit");
}

function onExit(game, parent, talker, conversation) {
    conversation.exit()
}
