
function onAreaLoadFirstTime(game, area, transition) {
    game.runExternalScript("quests/theSmuggler", "questComplete");
	
	game.addPartyXP(20 * game.ruleset().getValue("EncounterXPFactor"));
}
