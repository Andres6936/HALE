function startConversation(game, parent, target, conversation) {
    if (game.get("missingHusbandRewardRecieved") != null) {
        conversation.addText("Thank you so much for finding my husband!");
        conversation.addResponse("You are welcome.  Farewell.", "onExit");
        
    } else if (game.get("missingHusbandQuestCompleted") != null) {
        var paymentAmount = parent.get("paymentAmount");
        // by default give 1 GP if the player didn't ask for anything
        if (paymentAmount == null) paymentAmount = 1;
        paymentAmount = parseInt(paymentAmount);
        
        conversation.addString("<p>");
        conversation.addString("You've found him!  Thanks so much for your help!  Here is ");
        conversation.addString("<span style=\"font-family: red\">");
        conversation.addString(paymentAmount);
        conversation.addString("</span> Gold for your help!</p>");
        
        game.getPartyCurrency().addGP(paymentAmount);
        game.put("missingHusbandRewardRecieved", true);
        game.runExternalScript("quests/missingHusband", "questComplete");
        
        conversation.addResponse("Thank you.  Farewell.", "onExit");
        
    } else if (game.get("missingHusbandQuestAccepted") != null) {
        conversation.addText("Please find my husband!  He was heading to the forest south of here.");
        conversation.addResponse("I am still looking.  Farewell.", "onExit");
        
    } else {
        conversation.addText("Hello.  I'm sorry for the intrusion, but I've no where else to turn!");
        conversation.addResponse("What seems to be the trouble?", "askTrouble");
        conversation.addResponse("Not my problem.  Goodbye.", "onExit");
    }
}

function askTrouble(game, parent, talker, conversation) {
    conversation.addText("Well, its my husband.  He went into the woods south of here to hunt for game two days ago, and hasn't returned.");
    conversation.addText("I've tried asking for help, but no one can be bothered to look for him!");
    conversation.addText("Will you help me find him?");
    
    conversation.addResponse("Of course!  Fear not, I will find your husband!", "acceptQuest");
    conversation.addResponse("I could look, but I will require something in return. <span style=\"font-family: red\">Speech</span>", "askForMoney");
    conversation.addResponse("Sorry, not interested.  Goodbye.", "onExit");
}

function askForMoney(game, parent, talker, conversation) {
    // only allow the player one chance to perform the conversation check and get a good amount
    if (parent.get("paymentAmount") == null) {
        var check = game.campaign().getBestPartySkillCheck("Speech");
        
        if (check > 70) {
            parent.put("paymentAmount", 3);
        } else if (check > 50) {
            parent.put("paymentAmount", 2);
        } else {
            parent.put("paymentAmount", 1);
        }
    }
    conversation.addString("<p>");
    conversation.addString("I don't have much, but I can give you <span style=\"font-family: red\">");
    conversation.addString( parseInt(parent.get("paymentAmount")) );
    conversation.addString("</span> gold pieces if you find him!");
    conversation.addString("</p>");
    
    conversation.addResponse("I accept.", "acceptQuest");
    conversation.addResponse("I will think on it.  Bye.", "onExit");
}

function acceptQuest(game, parent, talker, conversation) {
    game.runExternalScript("quests/missingHusband", "startQuest");
    game.put("missingHusbandQuestAccepted", true);
    
    conversation.exit();
}

function onExit(game, parent, talker, conversation) {
    conversation.exit()
}
