function startConversation(game, parent, target, conversation) {
    conversation.addText("You come here, slaughter my people, why?");
    
    conversation.addResponse("Die, foul Orc! <span style=\"font-family: red\">Fight</span>", "onExit");
    conversation.addResponse("I hunt down your evil kind for the good of all.", "orcFightResponse");
    
    if (game.hasQuestEntry("The Stolen Goods"))
        conversation.addResponse("You stole something from a merchant.  I am returning it.", "orcStealResponse");
    
    conversation.addResponse("I don't know, really.  I was just passing through.", "orcAnnoyedResponse");
}

function orcFightResponse(game, parent, talker, conversation) {
    conversation.addText("Then you die now!");
    
    conversation.addResponse("<span style=\"font-family: red\">Fight</span>", "onExit");
}

function orcStealResponse(game, parent, talker, conversation) {
    conversation.addText("The stupid merchant!  You are fools to be his errand boy.  Die now!");
    
    conversation.addResponse("<span style=\"font-family: red\">Fight</span>", "onExit");
}

function orcAnnoyedResponse(game, parent, talker, conversation) {
    conversation.addText("You kill my people on a whim!  Die fool!");
    
    conversation.addResponse("<span style=\"font-family: red\">Fight</span>", "onExit");
}

function onExit(game, parent, talker, conversation) {
    parent.getEncounter().setFaction("Hostile");
    conversation.exit();
}
