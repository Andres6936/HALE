function startConversation(game, parent, target, conversation) {
    if (game.get("joiningTheArmyComplete") == null) {
        conversation.addText("If you are trying to cross, you are out of luck.  The bridge was destroyed in the battle.");
        conversation.addResponse("What happened?", "ask1");
    } else {
        conversation.addText("Hello, again.");
        conversation.addResponse("Farewell.", "onExit");
    }
    
    
}

function ask1(game, parent, talker, conversation) {
    conversation.addText("We met the Valrins in battle just on the other side of the river.");
    conversation.addText("After our army crossed, they sabotaged the bridge, trying to trap us.");
    
    conversation.addResponse("<span style=\"font-family: red\">Continue</span>", "ask2");
}

function ask2(game, parent, talker, conversation) {
    conversation.addText("The plan backfired however, and our boys fought twice as hard, scattering the Valerin army.");
    conversation.addText("The king's army is even now pursuing the remnants deep into Valer.");
    
    conversation.addResponse("Is there any other way to cross and follow the army?", "ask3");
}

function ask3(game, parent, talker, conversation) {
    conversation.addText("The nearest crossing is about a week's journey to the South.  You could try using that crossing and then cutting back up to pursue the army, but my guess is that it'll all be over by the time you reach them.");
    
    conversation.addResponse("I guess my journey ends here then.", "ask4");
}

function ask4(game, parent, talker, conversation) {
    conversation.addText("Indeed.  I'd recommend you and your companions turn around and head home.");
    
    game.runExternalScript("quests/joiningTheArmy", "questComplete");
    game.put("joiningTheArmyComplete", true);
    
    conversation.addResponse("Thanks for your time.  Farewell.", "onExit");
}


function onExit(game, parent, talker, conversation) {
    conversation.exit()
}
