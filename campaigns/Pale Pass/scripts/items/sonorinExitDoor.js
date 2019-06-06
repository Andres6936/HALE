
function onOpen(game, item, opener) {
    game.runExternalScriptWait("items/sonorinExitDoor", "startCutscene", 1.0);
    game.lockInterface(1.0);
}

function onBossFightFinished(game) {
    game.currentArea().getEntityWithID("sonorinCanyonExitDoor").unlock();
}

function startCutscene(game) {
    game.showCutscene("sonorinCanyon");
}