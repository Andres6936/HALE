
function startConversation(game, parent, target, conversation) {
    conversation.addText("Trade, surfacer?");
    
    conversation.addResponse("<span style=\"font-family: red\">Barter</span>", "trade");
    
    if (game.get("slaverQuestStarted") == null) {
        conversation.addResponse("Do you know of any work around here?", "askWork");
    }
    
    conversation.addResponse("No.  Farewell", "onExit");
}

function askWork(game, parent, target, conversation) {
    conversation.addText("Talk to blacksmith, I hear he needs some help.");
    
    conversation.addResponse("<span style=\"font-family: red\">Barter</span>", "trade");
    conversation.addResponse("Farewell", "onExit");
}

function trade(game, parent, target, conversation) {
    game.showMerchant("goblin_alchemist");
    conversation.exit();
}


function onExit(game, parent, talker, conversation) {
    conversation.exit();
}
