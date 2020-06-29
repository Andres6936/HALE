
function startConversation(game, parent, target, conversation) {
    conversation.addText("Are you OK?");
    conversation.addResponse("Yes, I'm fine.  What happened?", "re1");
}

function re1(game, parent, target, conversation) {
    conversation.addText("You seemed to be in another place for a while.");
    
    conversation.addResponse("I saw another vision.", "re2");
}

function re2(game, parent, target, conversation) {
    conversation.addText("Again?  What should we do?");
    
    conversation.addResponse("The vision is drawing me, to a place South of here.  I must go.", "re3");
}

function re3(game, parent, target, conversation) {
    if (game.getParty().size() > 2) {
        conversation.addText("Then we are coming with you!");
    } else {
        conversation.addText("Then I am coming with you!");
    }
    
    conversation.addResponse("Lets go.", "onExit");
}

function onExit(game, parent, talker, conversation) {
    conversation.exit()
}
