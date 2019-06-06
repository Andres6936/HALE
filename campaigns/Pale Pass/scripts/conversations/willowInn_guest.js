
function startConversation(game, parent, target, conversation) {
    if (game.get("willowInnGuest_talked") == null) {
        game.put("willowInnGuest_talked", true);
    
        conversation.addText("Well met friend.  We are traveling West to Finrel.  Where are you headed?");
        conversation.addResponse("East to Fareach.", "answerLocation");
        conversation.addResponse("None of your business.  Farewell.", "onExit");
    } else {
        conversation.addText("Hello again.");
        conversation.addResponse("Know anything about the road to Fareach?", "answerLocation");
        conversation.addResponse("Goodbye.", "onExit");
    }
}

function answerLocation(game, parent, talker, conversation) {
    conversation.addText("Fareach?  The road there is dangerous now with bandits.  You'd best be careful.");
    
    conversation.addResponse("You've been there before?  What is the city like?", "askCity");
    conversation.addResponse("Thanks for the advice.  Farewell.", "onExit");
}

function askCity(game, parent, talker, conversation) {
    conversation.addText("It is not much more than the garrison and a few supporting shops.  Going to enlist are you?");
    
    conversation.addResponse("Yes.  How did you know?", "askEnlist");
    conversation.addResponse("None of your business.  Farewell.", "onExit");
}

function askEnlist(game, parent, talker, conversation) {
    conversation.addText("Well, there really isn't any other reason to travel there these days.  It is mostly a ghost town.");
    conversation.addText("If you do want to enlist, I'd hurry.  I've heard that the army will be moving out soon.");
    
    conversation.addResponse("Thanks for the advice.  Goodbye.", "onExit");
}

function onExit(game, parent, talker, conversation) {
    conversation.exit()
}
