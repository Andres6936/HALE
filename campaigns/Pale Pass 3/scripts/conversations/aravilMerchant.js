function startConversation(game, parent, target, conversation) {
    conversation.addText("Welcome!  I sell a variety of goods at the lowest prices around!");
    
    conversation.addResponse("<span style=\"font-family: red\">Barter</span>", "trade");
    
    conversation.addResponse("Farewell", "onExit");
}

function trade(game, parent, target, conversation) {
    game.showMerchant("aravilMerchant");
    conversation.exit();
}


function onExit(game, parent, talker, conversation) {
    conversation.exit();
}
