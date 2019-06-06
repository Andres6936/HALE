function startConversation(game, parent, target, conversation) {
    conversation.addText("Would you like to see my wares, surfacer?");
    
    conversation.addResponse("<span style=\"font-family: red\">Barter</span>", "trade");
    
    conversation.addResponse("No.  Farewell", "onExit");
}

function trade(game, parent, target, conversation) {
    game.showMerchant("dwarf_alchemist");
    conversation.exit();
}


function onExit(game, parent, talker, conversation) {
    conversation.exit();
}
