
function onPlayerEnter(game, player, trigger) {
    // only spring the trap once
    if (game.get("goblinCityPitTrapSprung") != null) return;
    
    for (var i = 0; i < game.getParty().size(); i++) {
        var partyMember = game.getParty().get(i);
        
        // if not all party members are in the trap area, do nothing
        if ( !trigger.containsPoint(partyMember.getLocation()) )
            return;
    }
    
    game.put("goblinCityPitTrapSprung", true);
    
    // lock the entrance door
    var entranceDoor = game.currentArea().getEntityWithID("goblinCityPitEntrance");
    var exitDoor = game.currentArea().getEntityWithID("goblinCityPitExit");
    
    entranceDoor.close(player);
    entranceDoor.setLocked(true);
    
    // show the surrounding area
    game.revealArea(27, 11, 5, 0);
    
    // create chieftan and start convo
    var chieftan = game.getNPC("goblin_chieftan_entrance");
	chieftan.setLocation(game.currentArea(), 23, 14);
    //chieftan.setLocationInCurrentArea(23, 14);
    chieftan.setFaction("Neutral");
    game.currentArea().getEntities().addEntity(chieftan);
    
    game.lockInterface(3.0);
    game.runExternalScriptWait("triggers/goblinCityPit", "startConvo", 1.0, player);
}

function startConvo(game, player) {
    game.scrollToCreature("goblin_chieftan_entrance");
    
    game.sleep(2000);
    
    game.currentArea().getEntityWithID("goblin_chieftan_entrance").startConversation(player);
}