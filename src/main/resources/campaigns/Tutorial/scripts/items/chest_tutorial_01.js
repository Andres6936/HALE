function onOpen(game, container, opener) {
    var tutQuest = game.getQuestEntry("Tutorial");
	if (tutQuest.hasSubEntry("Picking up an item")) return;
	
	var popup = game.createHTMLPopup("tutorial/tutorial_04.html");
    popup.setSize(400, 250);
    popup.show();

    tutQuest.setCurrentSubEntriesCompleted();
    var subEntry = tutQuest.createSubEntry("Picking up an item");
    subEntry.setShowTitle(false);
    subEntry.addExternalText("tutorial/tutorial_04.html");
}