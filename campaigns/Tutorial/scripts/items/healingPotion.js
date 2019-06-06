function onUse(game, item, target) {
	game.put("playerMustDrinkPotion", false);

    var quality = item.getQuality().getModifier() + 100;

    var hp = game.dice().d((10 * quality) / 100, 3);
    
    game.addMessage("blue", target.getName() + " drinks a potion and is healed of " + hp + " points of damage.");
    
    target.healDamage(hp);
    
    target.inventory.remove(item);
    
    game.ai.provokeAttacksOfOpportunity(target);

	var cb = item.getTemplate().getScript().createDelayedCallback("showTutorial");
	cb.setDelay(0.7);
	cb.start();
}

function showTutorial(game) {
	var tutQuest = game.getQuestEntry("Tutorial");
	if (tutQuest.hasSubEntry("Abilities")) return;

	var popup = game.createHTMLPopup("tutorial/tutorial_15.html");
    popup.setSize(400, 250);
    popup.show();

    tutQuest.setCurrentSubEntriesCompleted();
    var subEntry = tutQuest.createSubEntry("Abilities");
    subEntry.setShowTitle(false);
    subEntry.addExternalText("tutorial/tutorial_15.html");
	
	game.put("playerMustUseAbility", true);
}