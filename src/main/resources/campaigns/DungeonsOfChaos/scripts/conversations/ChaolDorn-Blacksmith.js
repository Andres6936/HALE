function startConversation(game, parent, target, conversation) {
    conversation.addText("You'll need sturdy armor and a good weapon if you are thinking about heading below.");
    conversation.addResponse("<span style=\"font-family: red\">Barter</span>", "showMerchant");
    conversation.addResponse("Nothing now, thanks.", "onExit");
}

function showMerchant(game, parent, talker, conversation) {
    game.showMerchant("ChaolDorn-Blacksmith");
    conversation.exit();
}

function onExit(game, parent, talker, conversation) {
    conversation.exit();
}
