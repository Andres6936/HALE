function startConversation(game, parent, target, conversation) {
    conversation.addText("Braving the dungeons?  I can sell you potions and ingredients to keep you strong and healthy.");
    conversation.addResponse("<span style=\"font-family: red\">Barter</span>", "showMerchant");
    conversation.addResponse("I'm not interested now.", "onExit");
}

function onExit(game, parent, talker, conversation) {
    conversation.exit();
}

function showMerchant(game, parent, talker, conversation) {
    game.showMerchant("ChaolDorn-Alchemist");
    conversation.exit();
}
