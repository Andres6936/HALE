function startConversation(game, parent, target, conversation) {
    conversation.addText("I have a modest selection of potions and alchemy reagents if you are interested.");
    conversation.addResponse("<span style=\"font-family: red\">Barter</span>", "showMerchant");
    conversation.addResponse("No, thanks.", "onExit");
}

function onExit(game, parent, talker, conversation) {
    conversation.exit()
}

function showMerchant(game, parent, talker, conversation) {
    game.showMerchant("fareach_Alchemist");
    conversation.exit();
}