function onAreaLoadFirstTime(game, area) {
    var popup = game.createHTMLPopup("popups/southernRoadEnter.html");
    popup.setSize(300, 200);
    popup.show();
}

function onAreaExit(game, area, transition) {
    if (transition.getID().equals("SouthernRoadToWorldMap2")) {
        game.revealWorldMapLocation("Sonorin Desert");
    }
}
