
function startConversation(game, parent, target, conversation) {
    if (parent.get("questDone") != null) {
        conversation.addText("What you want, surfacer?");
        
        conversation.addResponse("Nothing.  Farewell.", "onExit");
        
    } else if (game.getParty().hasItem("mushroomMeat", 10)) {
        conversation.addText("You have mushroom meat!  Give to me, I give you big reward!");
        
        conversation.addResponse("OK.  Here you go.", "done01");
        conversation.addResponse("Hold on a minute.  What do I get in return?", "done02");
        
    } else if (parent.get("questAccepted") != null) {
        conversation.addText("Bring me 10 mushroom meat.  Get big reward!");
        
        conversation.addResponse("Farewell.", "onExit");
    } else {
        conversation.addText("Surfacer!  You like big treasure, yes?");
        conversation.addResponse("Um...What?", "convo01");
    }
}

function done01(game, parent, target, conversation) {
    conversation.addText("You do good, take this hat.  Strong magic!");
    
    conversation.addResponse("Thank you.  Farewell.", "onExitQuestDone");
}

function done02(game, parent, target, conversation) {
    conversation.addText("I give you magic hat.  Strong magic!");
    
    conversation.addResponse("Agreed.", "done03");
    conversation.addResponse("<span style=\"font-family: red;\">Speech</span> Why don't you throw in some gold as well?", "done04");
    conversation.addResponse("No deal.  Farewell.", "onExit");
}

function done03(game, parent, target, conversation) {
    conversation.addText("OK Good.  Take hat.  I take mushroom.");
    
    conversation.addResponse("Farewell.", "onExitQuestDone");
}

function done04(game, parent, target, conversation) {
    // only allow one chance to perform the conversation check
    if (parent.get("paymentAmount") == null) {
        var check = game.campaign().getBestPartySkillRanks("Speech");
        
        if (check > 60) {
            parent.put("paymentAmount", 5);
        } else if (check > 40) {
            parent.put("paymentAmount", 3);
        } else if (check > 20) {
            parent.put("paymentAmount", 2);
        } else {
            parent.put("paymentAmount", 1);
        }
    }
    
    conversation.addString("<div>OK.  You take hat and <span style=\"font-family: red\">");
    conversation.addString( parseInt(parent.get("paymentAmount")) );
    conversation.addString("</span> gold.</div>");
    
    conversation.addResponse("You have a deal.", "done05");
    conversation.addResponse("Not good enough.  Farewell.", "onExit");
}

function done05(game, parent, target, conversation) {
    game.getPartyCurrency().addGP(parent.get("paymentAmount"));
    game.addPartyXP(5 * game.ruleset().getValue("EncounterXPFactor"));
	
    conversation.addText("OK.  Take hat and gold.  I take mushroom.");
    
    conversation.addResponse("Farewell.", "onExitQuestDone");
}

function onExitQuestDone(game, parent, target, conversation) {
    parent.put("questDone", true);
    game.runExternalScript("quests/mushroomMen", "endQuest");
    
    game.getParty().removeItem("mushroomMeat", 10);
    
    target.inventory.getUnequippedItems().add("wizardHat02", "Exceptional");
    
    conversation.exit();
}

function convo01(game, parent, target, conversation) {
    conversation.addText("You like treasure!  Big sword, big axe!  Yes?");
    
    conversation.addResponse("Well yes, we can always use good equipment.", "convo02");
    conversation.addResponse("No.  Farewell.", "onExit");
}

function convo02(game, parent, target, conversation) {
    conversation.addText("Good!  We make deal!  Bring me mushroom meat.  I give you good treasure!");
    
    conversation.addResponse("OK.  Where do I find this mushroom meat?", "convo03");
    conversation.addResponse("Not interested.  Goodbye.", "onExit");
}

function convo03(game, parent, target, conversation) {
    conversation.addText("Head North, find and kill mushroom men.  Take meat.  Need at least 10.");
    
    parent.put("questAccepted", true);

    game.revealWorldMapLocation("Mushroom Forest");
    game.runExternalScript("quests/mushroomMen", "startQuest");
    
    conversation.addResponse("OK.  I will return with the meat from ten of these mushroom men.", "onExit");
}

function onExit(game, parent, talker, conversation) {
    conversation.exit()
}
