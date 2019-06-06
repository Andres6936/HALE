function startConversation(game, parent, target, conversation) {
	if (parent.get("finalQuestRecieved") != null) {
		conversation.addText("You must travel to the Pale Pass and defeat the Master.  Good luck.");
		
		conversation.addResponse("Farewell.", "onExit");
	} else if (game.get("focusCrystalLost") != null) {
		conversation.addText("You are back, but the Master's armies are still here.  What happened?");
		
		conversation.addResponse("The crystal was already gone by the time we got there.", "crystal02");
		
	} else if (parent.get("attackQuestGiven") != null) {
		conversation.addText("Good luck, friends.");
		
		conversation.addResponse("Farewell.", "onExit");
	} else {
		conversation.addText("Hail friends!  You must be the adventurers I was told about.");
	
		conversation.addResponse("<span style=\"font-family: red\">Continue</span>", "convo02");
	}
}

function crystal02(game, parent, target, conversation) {
	conversation.addText("Blast!  We were so close to having it!  Do you have any clue where the crystal is now?");
	
	conversation.addResponse("Apparently the Master has it now.", "crystal03");
}

function crystal03(game, parent, target, conversation) {
	conversation.addText("So it has come to this, then.");
	
	conversation.addResponse("<span style=\"font-family: red\">Continue</span>", "crystal04");
}

function crystal04(game, parent, target, conversation) {
	conversation.addText("Observing the enemy troop movements, we believe we have pinned down the location of their main base.  It is sure to be where the Master and the crystal are located.");
	
	conversation.addResponse("<span style=\"font-family: red\">Continue</span>", "crystal05");
}

function crystal05(game, parent, target, conversation) {
	conversation.addText("It is north of here, in the frozen Pale Pass.");
	
	game.revealWorldMapLocation("Pale Pass");
	
	conversation.addText("The Master is sure to be guarding the crystal, but our only hope seems to be in destroying it.");
	
	conversation.addResponse("<span style=\"font-family: red\">Continue</span>", "crystal06");
}

function crystal06(game, parent, target, conversation) {
	conversation.addText("The Master's armies grow stronger by the day.  We will continue the fight here, but I don't know how much longer we can hold them.");
	
	conversation.addText("You must head to the Pale Pass, and then find a way to defeat the Master.");
	
	conversation.addResponse("<span style=\"font-family: red\">Continue</span>", "crystal07");
}

function crystal07(game, parent, target, conversation) {
	conversation.addText("It is likely that his base will be guarded by his most powerful demons, so make sure you are ready.");
	
	conversation.addText("Good luck.");
	
	parent.put("finalQuestRecieved", true);
	game.runExternalScript("quests/theMaster", "palePass");
	
	conversation.addResponse("Good luck to you, general.  Farewell.", "onExit");
}

function convo02(game, parent, target, conversation) {
	conversation.addText("It is good you are here, we need all the help we can get.");
	
	conversation.addResponse("What is the situation like here?", "convo03");
}

function convo03(game, parent, target, conversation) {
	conversation.addText("Well, the Master has brought in a large army of demons from somewhere up north.");
	
	conversation.addText("As of yet, there hasn't been much fighting.  But we'll be ready for the bastards.");
	
	conversation.addResponse("<span style=\"font-family: red\">Continue</span>", "convo04");
}

function convo04(game, parent, target, conversation) {
	conversation.addText("I had recieved word from one of my scouts that he had found something matching the description of your focus crystal.");
	
	conversation.addText("Unfortunately, his group was ambushed and we believe the crystal now lies somewhere in the enemy camp, probably in the command tent.");
	
	conversation.addResponse("So how do we get it back?", "convo05");
}

function convo05(game, parent, target, conversation) {
	conversation.addText("We don't have much chance of breaking through the enemy lines at the moment.  However, we should be able to keep the bulk of the Master's forces occupied on the front lines.");
	
	conversation.addResponse("<span style=\"font-family: red\">Continue</span>", "convo06");
}

function convo06(game, parent, target, conversation) {
	conversation.addText("That should give you a chance to break into the enemy camp and destroy the crystal.  It will be hard fighting, but you seem up to the challenge.");
	conversation.addText("Good luck.");
	
	conversation.addResponse("Very well.  Good luck, general.", "onExit");
	
	parent.put("attackQuestGiven", true);
	game.runExternalScript("quests/theMaster", "theFocusCrystal");
	game.revealWorldMapLocation("Master's Army");
}

function onExit(game, parent, talker, conversation) {
    conversation.exit();
}
