function startConversation(game, parent, target, conversation) {
    conversation.addText("Arel,");
    conversation.addText("The target will be heading to the Sonorin Desert soon.  You should be able to locate them by the stone circle we discussed.");
    conversation.addText("I'm paying you in advance for this job so I want it done right.");
    conversation.addString("<div style=\"margin-top: 1em\">Bring me their heads when you are done.  The key will give you access to the canyon.</div>");
    conversation.addText("E.");
    conversation.addResponse("Done.", "onExit");
}

function onExit(game, parent, talker, conversation) {
    game.runExternalScript("quests/aStrangeDream", "mercNote");
    
    conversation.exit();
}
