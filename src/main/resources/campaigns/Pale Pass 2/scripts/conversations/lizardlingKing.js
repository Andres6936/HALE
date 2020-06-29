
function startConversation(game, parent, target, conversation) {
    if (game.get("kingSerpentRewardGiven")) {
        conversation.addText("Hello again, surfacer.");
        
        conversation.addResponse("Farewell.", "onExit");
        conversation.addResponse("Die, foul creature! <span style=\"font-family: red\">Fight</span>", "fight");
    } else if (game.get("kingSerpentKilled") != null) {
        
        conversation.addText("You have returned.  My scouts tell me that many serpents have been run off and the king one is dead.");
        
        conversation.addResponse("And my reward?", "askReward");
        
    } else if (game.get("lizardlingQuestStarted") == null) {
        conversation.addText("Surfacer, wait!");
    
        conversation.addResponse("<span style=\"font-family: red\">Continue</span>", "convo02");
    } else {
        conversation.addText("Clear the monsters in the lake island and you will have your reward.");
        
        conversation.addResponse("Farewell.", "onExit");
        conversation.addResponse("Die, foul creature! <span style=\"font-family: red\">Fight</span>", "fight");
    }
}

function convo02(game, parent, target, conversation) {
    conversation.addText("Why have you come to my domain?");

    conversation.addResponse("I am seeking a piece of a key to the gate to the surface.", "convo03");
    conversation.addResponse("Die, foul creature! <span style=\"font-family: red\">Fight</span>", "fight");
}

function convo03(game, parent, target, conversation) {
    conversation.addText("You wish to take this key from me?  How dare you come here and demand gifts of me!");
    
    conversation.addResponse("<span style=\"font-family: red\">Speech</span> Perhaps we can make a deal.  What do you want?", "convo04a");
    conversation.addResponse("Give me the key and I will spare your life.", "convo04b");
    conversation.addResponse("I will take it from your cold, dead hands! <span style=\"font-family: red\">Fight</span>", "fight");
}

function convo04b(game, parent, target, conversation) {
    conversation.addText("The audacity!  You will die today, fool!");
    
    conversation.addResponse("<span style=\"font-family: red\">Fight</span>", "fight");
}

function convo04a(game, parent, target, conversation) {
    var ranks = game.campaign().getBestPartySkillRanks("Speech");
    
    if (ranks > 30) {
        game.addPartyXP(16 * game.ruleset().getValue("EncounterXPFactor"));
    
        conversation.addText("Perhaps.  The key is located in the vault of our ancestors on an island in the center of this lake.");
        
        conversation.addText("The island has been overrun by creatures from the deep.  If you kill the largest creature which is resting outside the vault, the key is yours.  As an added incentive, I also have some treasure for you if you do this.");
        
        conversation.addText("Do we have a deal?");
        
        conversation.addResponse("Yes.  I will deal with your monster problem and obtain the key.", "convo05");
        conversation.addResponse("I think I'd rather just kill you.  <span style=\"font-family: red\">Fight</span>", "fight");
    } else {
        conversation.addText("There will be no deal.  You will die now!");
        
        conversation.addResponse("<span style=\"font-family: red\">Fight</span>", "fight");
    }
}

function convo05(game, parent, target, conversation) {
    conversation.addText("Very well.  My people within the lake will no longer attack you.  I cannot guarantee your safety further abroad, though.");
    
    game.runExternalScript("quests/theLizardlingKey", "talkKing");
    game.put("lizardlingQuestStarted", true);
    
    conversation.addResponse("I will return when I have cleared the island.", "onExit");
}

function fight(game, parent, target, conversation) {
    game.setFactionRelationship("Player", "Lizardlings", "Hostile");
    game.clearRevealedAreas();
    conversation.exit();
}

function onExit(game, parent, talker, conversation) {
    conversation.exit();
    game.clearRevealedAreas();
}

function askReward(game, parent, target, conversation) {
    conversation.addText("As promised.  I give you a powerful ring I'm sure you'll find useful.");
    
	game.addPartyXP(10 * game.ruleset().getValue("EncounterXPFactor"));
    target.inventory.getUnequippedItems().add("ringGreaterProtection");
    
    game.addMessage(target.getName() + " was given a Ring of Greater Protection.");
    
    game.put("kingSerpentRewardGiven", true);
    game.runExternalScript("quests/theLizardlingKey", "questComplete");
    
    conversation.addResponse("Thank you.  Farewell.", "onExit");
}
