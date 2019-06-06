function onPlayerEnterFirstTime(game, player) {
	var tutQuest = game.getQuestEntry("Tutorial");
	if (tutQuest.hasSubEntry("Saving the Game")) return;

    var popup = game.createHTMLPopup("tutorial/tutorial_13.html");
    popup.setSize(400, 250);
    popup.show();

    var subEntry = tutQuest.createSubEntry("Saving the Game");
    subEntry.setShowTitle(false);
    subEntry.addExternalText("tutorial/tutorial_13.html");
}
