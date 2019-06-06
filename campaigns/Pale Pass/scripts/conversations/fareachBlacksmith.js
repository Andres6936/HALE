function startConversation(game, parent, target, conversation) {
    conversation.addText("I'm the best smith in town.  Also the only one.  What can I get you?");
    conversation.addResponse("<span style=\"font-family: red\">Barter</span>", "showMerchant");
    conversation.addResponse("Nothing now, thanks.", "onExit");
}

function showMerchant(game, parent, talker, conversation) {
    game.showMerchant("fareach_Blacksmith");
    conversation.exit();
}

function onExit(game, parent, talker, conversation) {
    conversation.exit()
}
