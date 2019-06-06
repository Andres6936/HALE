function startConversation(game, parent, target, conversation) {
    conversation.addText("Intruders, here?  Kill them all!");
    conversation.addResponse("<span style=\"font-family: red\">Fight</span>", "onExit");
}


function onExit(game, parent, talker, conversation) {
    parent.getEncounter().setFaction("Hostile");
    conversation.exit();
    game.clearRevealedAreas();
}
