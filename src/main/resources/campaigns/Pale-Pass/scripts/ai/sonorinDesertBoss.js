/*
 * A customized aiStandard for the boss creatures to record their deaths
 */

function runTurn(game, parent) {
	game.runExternalScript("ai/aiStandard", "runTurn", parent);
}

function onCreatureDeath(game, parent) {
    var numDead = game.get("sonorinBossKilled");
    if (numDead == null) numDead = 0;
    
    numDead = parseInt(numDead);
    
    if (numDead < 4) {
        // still more hostiles to kill
        
        game.put("sonorinBossKilled", parseInt(numDead + 1));
    } else {
        // all enemies dead
        game.runExternalScript("items/sonorinExitDoor", "onBossFightFinished");
    }
}
