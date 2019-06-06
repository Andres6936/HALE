function onPlayerEnterFirstTime(game, player) {
    game.lockInterface(3.0);
    game.runExternalScriptWait("triggers/sonorinCanyonBoss", "startConvo", 1.0, player);
}

function startConvo(game, player) {
    game.scrollToCreature("boss_lvl07");
    game.revealArea(5, 3, 3, 0);
    game.sleep(2000);
    game.currentArea().getEntityWithID("boss_lvl07").startConversation(player);
}

function fightConcluded(game) {
    var popup = game.showCampaignConclusionPopup();
    popup.addText("<div style=\"font-family: medium-white;\">");
	popup.addText("Congratulations, you have completed Chapter 1 of the Pale Pass campaign.");
	popup.addText("  You may export your characters now if you wish and continue on with the next chapter.");
	popup.addText("</div>");
	
	popup.setNextCampaign("Pale Pass 2", "Continue to Chapter 2");
	popup.setTextAreaHeight(120);
	
	popup.show();
}
