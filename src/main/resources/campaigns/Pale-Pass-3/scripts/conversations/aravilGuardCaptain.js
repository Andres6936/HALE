function startConversation(game, parent, target, conversation) {
	if (parent.get("questComplete") != null) {
		conversation.addText("Hello again, friend.");
		
		conversation.addResponse("Farewell.", "onExit");
	} else if (game.getParty().hasItem("werewolf_foot", 1)) {
		conversation.addText("Solved that problem in Greenrange Forest yet?");
		
		conversation.addResponse("Yes.  <span style=\"font-family: red\">Explain the presence of werewolves and show him the werewolf foot.</span>", "jobComplete01");
	} else if (parent.get("questStarted") != null) {
		conversation.addText("Solved that problem in Greenrange Forest yet?");
		
		conversation.addResponse("Still working on it.  Farewell.", "onExit");
	} else {
		conversation.addText("Well met.  What can I do for you, citizen?");
	
		conversation.addResponse("I was wondering if you knew of any jobs that needed doing.", "askJob");
		conversation.addResponse("Nothing today, thanks.", "onExit");
	}
}

function jobComplete01(game, parent, target, conversation) {
	parent.put("questComplete", true);
	game.addPartyXP(25 * game.ruleset().getValue("EncounterXPFactor"));
    game.runExternalScript("quests/fullMoon", "questComplete");
    game.getParty().removeItem("werewolf_foot");
	
	conversation.addText("Werewolves in the Greenrange Forest!  And you managed to kill them all?  Well done!");
	
	conversation.addResponse("<span style=\"font-family: red\">Speech</span> We deserve payment for our work.", "askReward");
	conversation.addResponse("Thank you.  Farewell.", "onExit");
}

function askReward(game, parent, target, conversation) {
	var check = game.campaign().getBestPartySkillRanks("Speech");
    
	var amount = 8 + parseInt(check / 10);
	
	game.getPartyCurrency().addGP(amount);
	game.addMessage("The party has gained " + amount + " gold pieces.");
	
	conversation.addText("I can't deny that.  Here, take this gold.");
	
	conversation.addResponse("Farewell.", "onExit");
}

function askJob(game, parent, target, conversation) {
	conversation.addText("Well, we are stretched pretty thin by the Master, but we are handling it.");
	
	conversation.addText("There is one thing, though.  I've been getting reports from some farmers about disappearing animals, strange howls at night, and the like.  One farmhouse in particular seems to be getting the worst of it.");
	
	conversation.addResponse("<span style=\"font-family: red\">Continue</span>", "askJob02");
}

function askJob02(game, parent, target, conversation) {
	conversation.addText("Normally, we would handle it, but we are a bit busy at the moment.");
	
	conversation.addText("Head out to the Greenrange Forest, and figure out what is going on.  There is a farmhouse in the north end of the woods you should check out first.");
	
	game.activateTransition("greenrangeForestToFarmhouse");
	game.revealWorldMapLocation("Greenrange Forest");
	game.runExternalScript("quests/fullMoon", "startQuest");
	parent.put("questStarted", true);
	
	conversation.addResponse("We will look into it.  Farewell.", "onExit");
}


function onExit(game, parent, talker, conversation) {
    conversation.exit()
}
