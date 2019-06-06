function startConversation(game, parent, target, conversation) {
    conversation.addText("I'm the finest smith in Aravil.");
    
    conversation.addResponse("<span style=\"font-family: red\">Barter</span>", "trade");
    
    conversation.addResponse("Farewell", "onExit");
}

function trade(game, parent, target, conversation) {
    game.showMerchant("aravilSmith");
    conversation.exit();
}


function onExit(game, parent, talker, conversation) {
    conversation.exit();
}
