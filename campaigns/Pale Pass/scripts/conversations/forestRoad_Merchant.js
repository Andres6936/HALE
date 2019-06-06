function startConversation(game, parent, target, conversation) {
    if (game.get("stolenGoodsComplete") != null) {
        conversation.addText("Hello again, friend.");
        conversation.addResponse("<span style=\"font-family: red\">Barter</span>", "showMerchant");
        conversation.addResponse("Farewell.", "onExit");
        
    } else if (game.get("stolenGoodsObtained") == null) {
        conversation.addText("Hello traveler.  I've found myself in a bit of trouble here, but I can still offer some wares if you are interested.");
        conversation.addResponse("What sort of trouble are you in?", "askTrouble");
        conversation.addResponse("<span style=\"font-family: red\">Barter</span>", "showMerchant");
        conversation.addResponse("Farewell.", "onExit");
        
    } else {
        conversation.addText("You've returned.  Did you find my goods?");
        
        conversation.addResponse("Yes.  Here they are.", "offerGoods");
        conversation.addResponse("Yes.  Don't you think I deserve something for my hard work? <span style=\"font-family: red\">Speech</span>", "offerGoodsForPayment");
        conversation.addResponse("Not yet.  Goodbye.", "onExit");
    }
}

function offerGoods(game, parent, talker, conversation) {
    game.getParty().removeItem("merchantGoods");
    
    game.runExternalScript("quests/theStolenGoods", "completeQuest");
    
    conversation.addText("Many thanks, friend.  May good fortune always smile upon you.");
    
    conversation.addResponse("Farewell.", "onExit");
}

function offerGoodsForPayment(game, parent, talker, conversation) {
    // only allow the player one chance to perform the conversation check and get a good amount
    if (parent.get("paymentAmount") == null) {
        var check = game.campaign().getBestPartySkillCheck("Speech");
        
        if (check > 90) {
            parent.put("paymentAmount", 5);
        } else if (check > 70) {
            parent.put("paymentAmount", 3);
        } else if (check > 50) {
            parent.put("paymentAmount", 2);
        } else {
            parent.put("paymentAmount", 1);
        }
    }
    conversation.addString("<p>");
    conversation.addString("Fair enough.  I can offer you <span style=\"font-family: red\">");
    conversation.addString( parseInt(parent.get("paymentAmount")) );
    conversation.addString("</span> gold pieces for it.");
    conversation.addString("</p>");
    
    conversation.addResponse("I accept.", "acceptPayment");
    conversation.addResponse("I will think on it.  Bye.", "onExit");
}

function acceptPayment(game, parent, talker, conversation) {
    game.getPartyCurrency().addGP(parent.get("paymentAmount"));
    
    game.getParty().removeItem("merchantGoods");
    
    game.runExternalScript("quests/theStolenGoods", "completeQuest");
    
    conversation.addText("Many thanks, friend.  May good fortune always smile upon you.");
    
    conversation.addResponse("Farewell.", "onExit");
}

function askTrouble(game, parent, talker, conversation) {
    conversation.addText("Well, my caravan was attacked by orcs yesterday.  They killed one of my guards and ran off with some of my wares.");
    conversation.addText("Now, I am stuck waiting here for my runner to return with more men from Fareach so I can get my goods back from the orcs.");
    
    conversation.addResponse("I can help you recover those stolen goods.", "offerHelp");
    conversation.addResponse("<span style=\"font-family: red\">Barter</span>", "showMerchant");
    conversation.addResponse("I'm sorry for your troubles.  Farewell.", "onExit");
}

function offerHelp(game, parent, talker, conversation) {
    game.runExternalScript("quests/theStolenGoods", "startQuest");
    
    conversation.addText("I'm not sure I recommend it, but you are welcome to try.  The orcs ran off towards a cave just to the north of here.");
    conversation.addText("In any event, you should take a look at what I have before attempting it.");
    
    conversation.addResponse("<span style=\"font-family: red\">Barter</span>", "showMerchant");
    conversation.addResponse("I will return with your stolen goods.  Goodbye.", "onExit");
}

function showMerchant(game, parent, talker, conversation) {
    game.showMerchant("forestRoad_Merchant");
    conversation.exit();
}

function onExit(game, parent, talker, conversation) {
    conversation.exit()
}
