function onAreaLoadFirstTime(game, area) {
	var tutQuest = game.getQuestEntry("Tutorial");
	if (tutQuest.hasSubEntry("Exploration")) return;

    var popup = game.createHTMLPopup("tutorial/tutorial_12.html");
    popup.setSize(400, 250);
    popup.show();

    tutQuest.setCurrentSubEntriesCompleted();
    var subEntry = tutQuest.createSubEntry("Exploration");
    subEntry.setShowTitle(false);
    subEntry.addExternalText("tutorial/tutorial_12.html");
}