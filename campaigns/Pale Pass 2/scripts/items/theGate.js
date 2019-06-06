function onOpen(game, item, opener) {
    game.runExternalScriptWait("items/theGate", "startCutscene", 1.0);
    game.lockInterface(1.0);
}

function startCutscene(game) {
    game.showCutscene("theGate");
}

function campaignConclusion(game) {
	var popup = game.showCampaignConclusionPopup();
	popup.addText("<div style=\"font-family: medium-white;\">");
	popup.addText("Congratulations, you have completed Chapter 2 of the Pale Pass campaign.");
	popup.addText("  You may export your characters now if you wish and continue on with the next chapter.");
	popup.addText("</div>");
	
	popup.setNextCampaign("Pale Pass 3", "Continue to Chapter 3");
	popup.setTextAreaHeight(120);
	
	popup.show();
}