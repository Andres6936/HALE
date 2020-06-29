function onAddItem(game, parent, item) {
    var tutQuest = game.getQuestEntry("Tutorial");
	if (tutQuest.hasSubEntry("Equipping an item")) return;

    var popup = game.createHTMLPopup("tutorial/tutorial_05.html");
    popup.setSize(400, 250);
    popup.addCallback("items/tutorial_club", "tutorialAddClosed", parent);
    popup.show();
    
    tutQuest.setCurrentSubEntriesCompleted();
    var subEntry = tutQuest.createSubEntry("Equipping an item");
    subEntry.setShowTitle(false);
    subEntry.addExternalText("tutorial/tutorial_05.html");
}

function onEquipItem(game, parent, item) {
	var tutQuest = game.getQuestEntry("Tutorial");
	if (tutQuest.hasSubEntry("Basic Character Statistics")) return;

	if (parent.get("tutorialDaggerEquipped") != null) return;

    parent.put("tutorialDaggerEquipped", true);
	
    var popup = game.createHTMLPopup("tutorial/tutorial_06.html");
    popup.setSize(400, 250);
    popup.addCallback("items/tutorial_club", "tutorialEquipClosed", parent);
    popup.show();

    tutQuest.setCurrentSubEntriesCompleted();
    var subEntry = tutQuest.createSubEntry("Basic Character Statistics");
    subEntry.setShowTitle(false);
    subEntry.addExternalText("tutorial/tutorial_06.html");
}

function tutorialEquipClosed(game, parent) {
    parent.put("equipClosed", true);

    if (parent.get("addClosed") != null) {
        tutorialScrolling(game, parent);
    }
}

function tutorialAddClosed(game, parent) {
    parent.put("addClosed", true);

    if (parent.get("equipClosed") != null) {
        tutorialScrolling(game, parent);
    }
}

function tutorialScrolling(game, parent) {
	var tutQuest = game.getQuestEntry("Tutorial");
	if (tutQuest.hasSubEntry("Scrolling the view")) return;

    var popup = game.createHTMLPopup("tutorial/tutorial_07.html");
    popup.setSize(400, 250);
    popup.show();

    tutQuest.setCurrentSubEntriesCompleted();
    var subEntry = tutQuest.createSubEntry("Scrolling the view");
    subEntry.setShowTitle(false);
    subEntry.addExternalText("tutorial/tutorial_07.html");
}

function onAttack(game, weapon, attack) {
	var parent = attack.getAttacker();

	if (game.get("playerMustDrinkPotion") == true) {
		var cb = weapon.getTemplate().getScript().createDelayedCallback("reshowPotionTutorial");
		cb.setDelay(1.0);
		cb.addArgument(attack.getDefender());
		game.lockInterface(1.0);
		cb.start();
	}
	
	if (game.get("playerMustUseAbility") == true) {
		var cb = weapon.getTemplate().getScript().createDelayedCallback("reshowAbilityTutorial");
		cb.setDelay(1.0);
		cb.addArgument(attack.getAttacker());
		cb.addArgument(attack.getDefender());
		game.lockInterface(1.0);
		cb.start();
	}
	
    if (parent.get("tutorialDaggerAttacked") != null) return;

    parent.put("tutorialDaggerAttacked", true);

	game.lockInterface(1.0);
	var cb = weapon.getTemplate().getScript().createDelayedCallback("attackTutorial");
	cb.setDelay(1.0);
	cb.start();
}

function reshowAbilityTutorial(game, player, target) {
	target.healDamage(100);
	player.healDamage(100);
	
	var popup = game.createHTMLPopup("tutorial/tutorial_15.html");
    popup.setSize(400, 250);
    popup.show();
}

function reshowPotionTutorial(game, target) {
	target.healDamage(100);

	var popup = game.createHTMLPopup("tutorial/tutorial_14.html");
	popup.setSize(400, 250);
	popup.show();
}

function attackTutorial(game) {
	var tutQuest = game.getQuestEntry("Tutorial");
	if (tutQuest.hasSubEntry("Running Combat")) return;

    var popup = game.createHTMLPopup("tutorial/tutorial_10.html");
    popup.setSize(400, 280);
    popup.show();

    tutQuest.setCurrentSubEntriesCompleted();
    var subEntry = tutQuest.createSubEntry("Running Combat");
    subEntry.setShowTitle(false);
    subEntry.addExternalText("tutorial/tutorial_10.html");
}

function onAttackHit(game, weapon, attack, damage) {
    if (attack.getDefender().isDead()) {
		if (game.get("readyForFinalTutorial")) {
			var orcs = game.currentArea().getEntitiesWithID("tutorialOrc2");
		
			var allDead = true;
			for (var i = 0; i < orcs.size(); i++) {
				if (!orcs.get(i).isDead()) allDead = false;
			}
		
			if (allDead) {
				var cb = weapon.getTemplate().getScript().createDelayedCallback("tutorial17");
				cb.setDelay(0.7);
				cb.start();
			}
		}
	
		if (game.get("alreadyHit")) return;
		game.put("alreadyHit", true);
	
        var cb = weapon.getTemplate().getScript().createDelayedCallback("attackHitTutorial");
		cb.setDelay(1.0);
		cb.start();
    }
}

function attackHitTutorial(game) {
	var tutQuest = game.getQuestEntry("Tutorial");
	if (tutQuest.hasSubEntry("Area Transitions")) return;

	var popup = game.createHTMLPopup("tutorial/tutorial_11.html");
    popup.setSize(400, 250);
    popup.show();
  
    game.activateTransition("startAreaToDarkCave");

    tutQuest.setCurrentSubEntriesCompleted();
    var subEntry = tutQuest.createSubEntry("Area Transitions");
    subEntry.setShowTitle(false);
    subEntry.addExternalText("tutorial/tutorial_11.html");
}

function tutorial17(game) {
	var tutQuest = game.getQuestEntry("Tutorial");
	if (tutQuest.hasSubEntry("Completion")) return;

	var popup = game.createHTMLPopup("tutorial/tutorial_17.html");
	popup.addCallback("items/tutorial_club", "finishTutorial");
	popup.setSize(400, 250);
    popup.show();

	tutQuest.setCurrentSubEntriesCompleted();
	var subEntry = tutQuest.createSubEntry("Completion");
	subEntry.setShowTitle(false);
	subEntry.addExternalText("tutorial/tutorial_17.html");
}

function finishTutorial(game) {
	game.exitToMainMenu();
}