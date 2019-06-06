function cutsceneFinished(game) {
    var popup = game.showCampaignConclusionPopup();
    popup.addText("<div style=\"font-family: medium-white;\">");
    popup.addText("Congratulations on completing the Pale Pass campaign.");
    popup.addText("</div>");
    
    popup.addText("<div style=\"font-family: medium; margin-top: 1em;\">");
    popup.addText("Thanks for playing!");
    popup.addText("</div>");
    
    popup.setTextAreaHeight(120);
    
    popup.show();
}

function startCutscene(game) {
    game.showCutscene("theMaster");
}

function onCreatureDeath(game, parent) {
    game.lockInterface(3.0);
	
	game.runExternalScriptWait("ai/theMaster", "spawnPortal", 1.0);
    
    game.revealArea(37, 7, 2, 0);
}

function spawnPortal(game) {
    var portalCreature = game.getNPC("theMasterPortal");
	portalCreature.setLocationInCurrentArea(37, 7);
	portalCreature.setFaction("Neutral");
	
	game.currentArea().getEntities().addEntity(portalCreature);
	
	var effect = portalCreature.createEffect();
	effect.setHasDescription(false);
	
	var g1 = game.getBaseParticleGenerator("sparkle");
	g1.setDurationInfinite();
	g1.setRotationSpeedDistribution(game.getUniformDistribution(100.0, 200.0));
	g1.setPosition(portalCreature.getLocation());
	effect.addAnimation(g1);
	
	portalCreature.applyEffect(effect);
}

function runTurn(game, parent) {
    game.runExternalScript("ai/aiStandard", "runTurn", parent);
}
