
function startConversation(game, parent, target, conversation) {
    conversation.addText("Welcome to our town, traveler.  I can offer a few services, if you need them.");
    conversation.addText("I can also raise the dead for 5 GP.");
    conversation.addResponse("<span style=\"font-family: red\">Barter</span>", "showMerchant");
    
    if (game.getPartyCurrency().getValue() >= 500) {
        for (var i = 0; i < game.getParty().size(); i++) {
            if (game.getParty().get(i).isDead()) {
                conversation.addResponse("<span style=\"font-family: red\">Raise " + game.getParty().get(i).getName() + "</span>", "raisePartyMember", game.getParty().get(i));
            }
        }
    }
    
    conversation.addResponse("No thanks, farewell.", "onExit");
}

function raisePartyMember(game, parent, talker, conversation, partyMember) {
    if (game.getPartyCurrency().getValue() >= 500) {
        game.getPartyCurrency().addGP(-5);
        
        partyMember.raiseFromDead();
        game.addMessage("link", partyMember.getName() + " has been raised back to life.");
    }
    
    conversation.exit();
}

function showMerchant(game, parent, talker, conversation) {
    game.showMerchant("fareach_Merchant");
    conversation.exit();
}

function onExit(game, parent, talker, conversation) {
    conversation.exit()
}
