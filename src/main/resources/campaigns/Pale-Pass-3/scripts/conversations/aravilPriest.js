function startConversation(game, parent, target, conversation) {
    conversation.addText("Do you require any services?");
    
    conversation.addResponse("<span style=\"font-family: red\">Barter</span>", "trade");
    
    if (game.getPartyCurrency().getValue() >= 500) {
        for (var i = 0; i < game.getParty().size(); i++) {
            if (game.getParty().get(i).isDead()) {
                conversation.addResponse("<span style=\"font-family: red\">Raise " + game.getParty().get(i).getName() + "</span>", "raisePartyMember", game.getParty().get(i));
            }
        }
    }
    
	if (game.getParty().hasItem("wraithEssence", 1)) {
		conversation.addResponse("I have returned from the abandoned house with a piece of the spirit's essence.", "jobComplete");
	} else if (parent.get("questStarted") == null) {
		conversation.addResponse("Actually, I was wondering if there were any jobs you needed doing?", "askJob");
	}
	
    conversation.addResponse("No.  Farewell", "onExit");
}

function jobComplete(game, parent, target, conversation) {
	game.addPartyXP(20 * game.ruleset().getValue("EncounterXPFactor"));
    game.runExternalScript("quests/hauntedHouse", "questComplete");
    game.getParty().removeItem("wraithEssence");
	
	conversation.addText("That is fantastic!  I will begin casting the enchantments to purify the spirit immediately!");
	
	conversation.addResponse("<span style=\"font-family: red\">Speech</span> Don't you think we deserve a reward for our efforts?", "askReward");
	conversation.addResponse("Great.  Farewell.", "onExit");
}

function askReward(game, parent, target, conversation) {
	var check = game.campaign().getBestPartySkillRanks("Speech");
    
	var amount = 5 + parseInt(check / 10);
	
	game.getPartyCurrency().addGP(amount);
	game.addMessage("The party has gained " + amount + " gold pieces.");
	
	conversation.addText("Very well.  Take this gold as a token of thanks.");
	
	conversation.addResponse("Farewell.", "onExit");
}

function askJob(game, parent, target, conversation) {
	conversation.addText("Well there is one job that may suit someone such as yourself.");
	
	conversation.addText("At the southeast end of this district, there is a house that has long stood abandoned.");
	
	conversation.addText("It is said that at one time, a serial murderer of men lived in the house.");
	
	conversation.addResponse("<span style=\"font-family: red\">Continue</span>", "askJob2");
}

function askJob2(game, parent, target, conversation) {
	conversation.addText("Lately, I have sensed great disturbances around the house, and all fear to enter, afraid of what lies within.");
	
	conversation.addResponse("<span style=\"font-family: red\">Continue</span>", "askJob3");
}

function askJob3(game, parent, target, conversation) {
	conversation.addText("I fear the murderer remains as a spirit, and that he has likely called other evil spirits to him.");
	
	conversation.addText("If you can enter the house, defeat the spirit, and retrieve a piece of his essence, I believe I will be able to put him to rest and lift the curse of the house.");
	
	game.runExternalScript("quests/hauntedHouse", "startQuest");
	parent.put("questStarted", true);
	game.activateTransition("aravilCommonsToAbandonedHouse");
	
	conversation.addResponse("We will consider it.  Farewell.", "onExit");
	conversation.addResponse("We will return when it is done.", "onExit");
}

function raisePartyMember(game, parent, talker, conversation, partyMember) {
    if (game.getPartyCurrency().getValue() >= 500) {
        game.getPartyCurrency().addGP(-5);
        
        partyMember.raiseFromDead();
        game.addMessage("link", partyMember.getName() + " has been raised back to life.");
    }
    
    conversation.exit();
}

function trade(game, parent, target, conversation) {
    game.showMerchant("aravilPriest");
    conversation.exit();
}


function onExit(game, parent, talker, conversation) {
    conversation.exit();
}
