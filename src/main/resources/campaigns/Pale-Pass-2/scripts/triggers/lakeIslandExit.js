
function onPlayerEnter(game, player, trigger) {
    if (game.get("kingSerpentKilled") != null) return;


    var creature = game.currentArea().getEntityWithID("kingSerpent");
    
    if (creature == null) {
        game.put("kingSerpentKilled", true);
    }
}
