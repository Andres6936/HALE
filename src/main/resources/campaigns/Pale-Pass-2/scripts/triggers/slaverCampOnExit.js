function onAreaExit(game, area, transition) {
    if (game.get("removeGoblinSlaves") == true) {
        game.put("removeGoblinSlaves", false);
        
        var e1 = game.currentArea().getEntityWithID("goblin_peasant01");
        var e2 = game.currentArea().getEntityWithID("goblin_peasant02");
        var e3 = game.currentArea().getEntityWithID("goblin_slave_leader");
        
        game.currentArea().removeEntity(e1);
        game.currentArea().removeEntity(e2);
        game.currentArea().removeEntity(e3);
    }
}
