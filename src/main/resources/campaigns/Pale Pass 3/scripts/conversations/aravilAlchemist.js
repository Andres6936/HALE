function startConversation(game, parent, target, conversation) {
    conversation.addText("Welcome!  Have you need of my alchemy reagents?");
    
    conversation.addResponse("<span style=\"font-family: red\">Barter</span>", "trade");
    
    conversation.addResponse("Farewell", "onExit");
}

function trade(game, parent, target, conversation) {
    game.showMerchant("aravilAlchemist");
    conversation.exit();
}


function onExit(game, parent, talker, conversation) {
    conversation.exit();
}
