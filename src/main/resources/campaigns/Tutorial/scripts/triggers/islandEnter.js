function onPlayerEnter(game, player) {
	var tutQuest = game.getQuestEntry("Tutorial");
	
	if (!tutQuest.hasSubEntry("Scrolling the view")) return;
	if (tutQuest.hasSubEntry("Basic Combat")) return;

    var popup = game.createHTMLPopup("tutorial/tutorial_08.html");
    popup.setSize(400, 250);
    popup.addCallback("triggers/islandEnter", "startCombat");
    popup.show();

    var subEntry = tutQuest.createSubEntry("Basic Combat");
    subEntry.setShowTitle(false);
    subEntry.addExternalText("tutorial/tutorial_08.html");
}

function startCombat(game) {
	var tutQuest = game.getQuestEntry("Tutorial");
	if (tutQuest.hasSubEntry("Attacking")) return;

	var orc = game.currentArea().getEntityWithID("tutorialOrc");
    game.moveCreature(orc, 21, 4);

    game.sleep(1000);
	
	var popup = game.createHTMLPopup("tutorial/tutorial_09.html");
    popup.setSize(400, 250);
    popup.show();

    var subEntry = tutQuest.createSubEntry("Attacking");
    subEntry.setShowTitle(false);
    subEntry.addExternalText("tutorial/tutorial_09.html");
}