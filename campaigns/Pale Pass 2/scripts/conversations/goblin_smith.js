
function startConversation(game, parent, target, conversation) {

    if (game.get("slaverQuestStarted") != null && game.get("slaverQuestFinished") != null) {
        conversation.addText("Thank you surfacer, for finding my sons!");
        
		if (parent.get("rewardGiven") == null) {
			if (parent.get("agreedToPay") != null) {
				conversation.addText("I don't have much, but I give you what I can, 10 gold, as promised.");
				
				game.getPartyCurrency().addPP(1);
				
				game.addMessage("blue", "The party has gained 10 GP.");
				
				game.addPartyXP(10 * game.ruleset().getValue("EncounterXPFactor"));
			} else {
				game.addPartyXP(8 * game.ruleset().getValue("EncounterXPFactor"));
			}
		
			parent.put("rewardGiven", true);
		}
        
        game.runExternalScript("quests/dwarvenSlavers", "endQuest");
        
    } else if (game.get("slaverQuestStarted") != null) {
        conversation.addText("Please find my sons.");
    } else {
        conversation.addText("Trade, surfacer?");
    }

    conversation.addResponse("<span style=\"font-family: red\">Barter</span>", "trade");
    
    if (game.get("slaverQuestStarted") == null && game.get("slaverQuestFinished") == null) {
        conversation.addResponse("Do you know of any work around here?", "askWork");
    }
    
    conversation.addResponse("Farewell", "onExit");
}

function askWork(game, parent, target, conversation) {
    conversation.addText("You big strong warrior, right?  Maybe you help me?");
    
    conversation.addResponse("I don't think so.  Goodbye.", "onExit");
    conversation.addResponse("Maybe.  What do you want?", "askWork2");
    conversation.addResponse("Of course I'll help!  What do you need?", "askWork2");
}

function askWork2(game, parent, target, conversation) {
    conversation.addText("Well, its my two sons...");
    
    conversation.addResponse("What's wrong with your sons?", "askWork3");
}

function askWork3(game, parent, target, conversation) {
    conversation.addText("I don't know where they are.");
    
    conversation.addText("Bad dwarves came in the darkness short time ago.  I think they took them to slaver camp.");
    
    conversation.addResponse("That's horrible!  Where is this slaver camp?", "askWork4a");
    conversation.addResponse("<span style=\"font-family: red\">Speech</span> I might be persuaded to find them, for the right price.", "askWork4b");
    conversation.addResponse("Not my problem.  Goodbye.", "onExit");
}

function askWork4a(game, parent, target, conversation) {
    conversation.addText("It north, at the other end of the mushroom forest.  I mark your map.");
    
    game.put("slaverQuestStarted", true);
    game.revealWorldMapLocation("Mushroom Forest");
    
    game.runExternalScript("quests/dwarvenSlavers", "startQuest");
    
    conversation.addResponse("I swear I will find your sons!  I will return soon.", "onExit");
    conversation.addResponse("I will do my best to find them.  Farewell.", "onExit");
    conversation.addResponse("Maybe I'll look into it if I have time.  Farewell.", "onExit");
}

function askWork4b(game, parent, target, conversation) {
    var check = game.campaign().getBestPartySkillRanks("Speech");
    
    if (check > 30) {
        parent.put("agreedToPay", true);
        
        conversation.addText("I give you everything I have if you find them!");
        
        conversation.addResponse("Then I will find them for you.  Where is this camp?", "askWork4a");
        conversation.addResponse("On second thought, I have better things to do.  Goodbye.", "onExit");
        
    } else {
        conversation.addText("But I have nothing to give you!");
        
        conversation.addResponse("I will find them regardless.  Where is this camp?", "askWork4a");
        conversation.addResponse("Then I am not interested. Farewell.", "onExit");
    }
}

function trade(game, parent, target, conversation) {
	var merchant = game.campaign().getMerchant("goblin_smith");

	if (parent.get("rewardGiven") == null) {
		merchant.setBuyValuePercentage(50);
		merchant.setSellValuePercentage(150);
	} else {
		merchant.setBuyValuePercentage(70);
		merchant.setSellValuePercentage(130);
	}

    game.showMerchant("goblin_smith");
    conversation.exit();
}


function onExit(game, parent, talker, conversation) {
    conversation.exit();
}
