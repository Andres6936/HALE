
function startConversation(game, parent, target, conversation) {
    conversation.addText("What?  Who are you?  You not allowed here!");
    conversation.addResponse("Die, foul Orc! <span style=\"font-family: red\">Fight</span>", "onExit");
    conversation.addResponse("I am a messenger for your Chief.  Let me pass.", "askMessage");
    conversation.addResponse("I'm sorry, I was just leaving.", "askLeave");
}

function askMessage(game, parent, talker, conversation) {
    conversation.addText("We not take messages!");
    
    conversation.addResponse("But...", "askLeave");
}

function askLeave(game, parent, talker, conversation) {
    conversation.addText("No.  You not leave here alive!");
    
    conversation.addResponse("<span style=\"font-family: red\">Fight</span>", "onExit");
}

function onExit(game, parent, talker, conversation) {
    conversation.exit();
    parent.getEncounter().setFaction("Hostile");
}
