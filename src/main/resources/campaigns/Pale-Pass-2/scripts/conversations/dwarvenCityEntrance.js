
function startConversation(game, parent, target, conversation) {
    conversation.addText("Hail, surfacer!");
    conversation.addText("You are free to move about our city of Gan Tok.  Don't cause any trouble, or you'll be answering to me.");
    
    conversation.addResponse("I won't cause any trouble.  Farewell.", "onExit");
}


function onExit(game, parent, talker, conversation) {
    conversation.exit();
}
