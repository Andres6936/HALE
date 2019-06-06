
function startConversation(game, parent, target, conversation) {
    conversation.addString("<div style=\"font-family: blue\">");
    conversation.addText("From the stance of those around it, this creature appears to be the leader of the drakes.");
    conversation.addString("</div>");
    
    conversation.addResponse("<span style=\"font-family: red\">Continue</span>", "convo02");
}

function convo02(game, parent, target, conversation) {
    conversation.addString("<div style=\"font-family: blue\">");
    conversation.addText("It lets out a roar and begins a charge.  Clearly, there will be no parley.");
    conversation.addString("</div>");
    
    conversation.addResponse("<span style=\"font-family: red\">Fight</span>", "onExit");
}


function onExit(game, parent, talker, conversation) {
    parent.getEncounter().setFaction("Hostile");
    conversation.exit();
    game.clearRevealedAreas();
}
