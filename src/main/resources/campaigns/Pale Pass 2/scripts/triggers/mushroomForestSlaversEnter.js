
function onPlayerEnterFirstTime(game, player, trigger) {
    var popup = game.createHTMLPopup("popups/dwarvenSlaversMainEntrance.html");
    popup.setSize(300, 250);
    popup.show();
    
    game.put("dwarvenSlaversTryEntrance", true);
}
