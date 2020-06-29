
function startConversation(game, parent, target, conversation) {
    conversation.addText("So, here you are.  Know that you will never reach the Pale Pass alive.");
    conversation.addResponse("And who are you?", "askWho");
    conversation.addResponse("The Pale Pass?  What is that?", "askPass");
    conversation.addResponse("I think you have me confused with someone else.", "askConfused");
}

function askWho(game, parent, target, conversation) {
    conversation.addText("Who I am is not important.  Know only that my employer has paid me well to end your journey here.");
    
    conversation.addResponse("Your employer?  Who is that?", "askEmploy");
    conversation.addResponse("Perhaps instead of fighting we can reach some sort of agreement?", "askDiplo");
    conversation.addResponse("So be it.  Prepare to die!", "onExit");
}

function askPass(game, parent, target, conversation) {
    conversation.addText("Don't act stupid.  My employer is paying well to make sure you never reach your target.");
    
    conversation.addResponse("Your employer?  Who is that?", "askEmploy");
    conversation.addResponse("Perhaps instead of fighting we can reach some sort of agreement?", "askDiplo");
    conversation.addResponse("So be it.  Prepare to die!", "onExit");
}

function askConfused(game, parent, target, conversation) {
    conversation.addText("No, the description was quite specific.  And here you are, just where employer said you would be.");
    
    conversation.addResponse("Your employer?  Who is that?", "askEmploy");
    conversation.addResponse("Perhaps instead of fighting we can reach some sort of agreement?", "askDiplo");
    conversation.addResponse("So be it.  Prepare to die!", "onExit");
}

function askEmploy(game, parent, target, conversation) {
    conversation.addText("It matters not to you.  You'll be in the ground soon anyway.");
    
    conversation.addResponse("Perhaps instead of fighting we can reach some sort of agreement?", "askDiplo");
    conversation.addResponse("So be it.  Prepare to die!", "onExit");
}

function askDiplo(game, parent, target, conversation) {
    conversation.addText("I'm afraid not.  My reputation is on the line, you see.  No, I'm afraid you must die.");
    
    conversation.addResponse("So be it.  Prepare to die!", "onExit");
}

function onExit(game, parent, target, conversation) {
    parent.getEncounter().setFaction("Hostile");
    
    conversation.exit();
}