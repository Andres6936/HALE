function onAreaLoadFirstTime(game, area) {
	var tutQuest = game.getQuestEntry("Tutorial");
	if (tutQuest.hasSubEntry("Welcome")) return;

    var popup = game.createHTMLPopup("tutorial/tutorial_01.html");
    popup.setSize(400, 250);
    popup.addCallback("triggers/onCampaignLoad", "tutorial01Closed");
    popup.show();

    
    var subEntry = tutQuest.createSubEntry("Welcome");
    subEntry.setShowTitle(false);
    subEntry.addExternalText("tutorial/tutorial_01.html");
	
	for (var i = 0; i < game.getParty().size(); i++) {
		var partyMember = game.getParty().get(i);
        
		partyMember.abilities.fillEmptySlots();
	}
}

function tutorial01Closed(game) {
	var tutQuest = game.getQuestEntry("Tutorial");
	// only show log notifications for the first entry above
	tutQuest.setShowLogNotifications(false);
	if (tutQuest.hasSubEntry("Basic Interaction")) return;

    var popup = game.createHTMLPopup("tutorial/tutorial_02.html");
    popup.setSize(400, 250);
    popup.addCallback("triggers/onCampaignLoad", "tutorial02Closed");
    popup.show();

    
    tutQuest.setCurrentSubEntriesCompleted();
    var subEntry = tutQuest.createSubEntry("Basic Interaction");
    subEntry.setShowTitle(false);
    subEntry.addExternalText("tutorial/tutorial_02.html");
}

function tutorial02Closed(game) {
	var tutQuest = game.getQuestEntry("Tutorial");
	if (tutQuest.hasSubEntry("Opening a chest")) return;

    var popup = game.createHTMLPopup("tutorial/tutorial_03.html");
    popup.setSize(400, 250);
    popup.show();

    tutQuest.setCurrentSubEntriesCompleted();
    var subEntry = tutQuest.createSubEntry("Opening a chest");
    subEntry.setShowTitle(false);
    subEntry.addExternalText("tutorial/tutorial_03.html");
}