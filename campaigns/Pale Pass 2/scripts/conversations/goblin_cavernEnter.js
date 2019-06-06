
function startConversation(game, parent, target, conversation) {
    var alreadyTalked = game.get("alreadyTalkedGoblinEnter");
    
    if (alreadyTalked == null) {
        conversation.addString("<div style=\"font-family: blue\">");
        conversation.addString("Before you stands a short, stout, and horribly ugly creature.");
        conversation.addString("</div>");
        
        conversation.addString("<div style=\"font-family: blue; margin-top: 1em\">");
        conversation.addString("It looks to be some kind of Orc, but of a variety you have never seen nor heard of.");
        conversation.addString("</div>");
    
        conversation.addResponse("<span style=\"font-family: red\">Continue</span>", "convo1");
        
        game.put("alreadyTalkedGoblinEnter", true);
    } else {
        conversation.addString("<div style=\"font-family: blue\">");
        conversation.addText("You see the same creature you saw before.");
        conversation.addString("</div>");
        
        conversation.addResponse("<span style=\"font-family: red\">Continue</span>", "convo2");
    }
}

// functions from first conversation

function convo1(game, parent, target, conversation) {
    conversation.addString("<div style=\"font-family: blue\">");
    conversation.addText("The creature looks at you with a start, and then turns and flees into the blackness.");
    conversation.addString("</div>");
    
    conversation.addResponse("<span style=\"font-family: red\">Continue</span>", "onExit");
}

function onExit(game, parent, talker, conversation) {
    conversation.exit();
    
    game.lockInterface(5);
    
    game.runExternalScriptWait("conversations/goblin_cavernEnter", "moveAndRemove", 1.0, parent);
}

function moveAndRemove(game, parent) {
    parent.timer.reset();
    
    game.ai.moveTowards(parent, 14, 10, 0);
    game.sleep(1000);
    
    game.unlockInterface();
    game.currentArea().removeEntity(parent);
}

// functions from second conversation

function convo2(game, parent, target, conversation) {
    conversation.addText("Brog lak to do naka!");
    
    conversation.addResponse("I'm sorry, I can't understand you.", "convo3");
    conversation.addResponse("Die, foul creature!", "convo3");
}

function convo3(game, parent, target, conversation) {
    conversation.addString("<div style=\"font-family: blue\">");
    conversation.addText("The creature again turns and flees deeper into the cave.");
    conversation.addText("</div>");
    
    conversation.addResponse("<span style=\"font-family: red\">Continue</span>", "onExit2");
}

function onExit2(game, parent, talker, conversation) {
    conversation.exit();
    
    game.lockInterface(5);
    
    game.runExternalScriptWait("conversations/goblin_cavernEnter", "moveAndRemove2", 1.0, parent);
}

function moveAndRemove2(game, parent) {
    parent.timer.reset();
    
    game.ai.moveTowards(parent, 33, 6, 0);
    game.sleep(1000);
    
    game.unlockInterface();
    game.currentArea().removeEntity(parent);
}
