
function onAreaLoadFirstTime(game, area) {
    // allow traveling to the willow inn and fareach via the world map
    game.activateTransition("WillowInnRoadToWorldMap");
    
    game.revealWorldMapLocation("Fareach");
    game.revealWorldMapLocation("The Willow Inn");
    
    var popup = game.createHTMLPopup("popups/fareachEnter.html");
    popup.setSize(300, 200);
    popup.show();
}

function onAreaLoad(game, area) {
    if (game.get("talkedToFareachSoldier") != null && game.get("removedFareachSoldier") == null) {
        var area = game.getArea("Fareach Garrison");
        
        area.removeEntity(area.getEntityWithID("fareach_soldier"));
        
        game.put("removedFareachSoldier", true);
    }
}