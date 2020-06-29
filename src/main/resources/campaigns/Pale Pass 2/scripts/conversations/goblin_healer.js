
function startConversation(game, parent, target, conversation) {
    conversation.addText("Trade or heal, surfacer?");
    
    conversation.addResponse("<span style=\"font-family: red\">Barter</span>", "trade");
    
    if (game.get("slaverQuestStarted") == null) {
        conversation.addResponse("Do you know of any work around here?", "askWork");
    }
    
    if (game.getPartyCurrency().getValue() >= 500) {
        for (var i = 0; i < game.getParty().size(); i++) {
            if (game.getParty().get(i).isDead()) {
                conversation.addResponse("<span style=\"font-family: red\">Raise " + game.getParty().get(i).getName() + "</span>", "raisePartyMember", game.getParty().get(i));
            }
        }
    }
    
    conversation.addResponse("No.  Farewell", "onExit");
}

function askWork(game, parent, target, conversation) {
    conversation.addText("Talk to blacksmith, I hear he needs some help.");
    
    conversation.addResponse("<span style=\"font-family: red\">Barter</span>", "trade");
    conversation.addResponse("Farewell", "onExit");
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
    game.showMerchant("goblin_healer");
    conversation.exit();
}


function onExit(game, parent, talker, conversation) {
    conversation.exit();
}
