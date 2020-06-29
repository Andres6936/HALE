function onPlayerEnterFirstTime(game, player, trigger) {
    game.lockInterface(3.0);
    game.runExternalScriptWait("triggers/greenrangeFarmhouse", "startConvo", 1.0, player);
    
    game.revealArea(33, 26, 4, 0);
}

function startConvo(game, player) {
    game.scrollToCreature("werewolf_farmhouse");
    
    game.sleep(2000);
    
    game.currentArea().getEntityWithID("werewolf_farmhouse").startConversation(player);
}
