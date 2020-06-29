function onUse(game, item, user) {
	// build the list of targets
	var targets = game.ai.getTouchableCreatures(user, "Friendly");
	
	for (var i = 0; i < targets.size(); i++) {
		if (!targets.get(i).isDead()) {
			targets.remove(i);
			i--;
		}
	}
	
	// create the targeter
	var targeter = game.createListTargeter(user, item.getTemplate().getScript());
	targeter.addAllowedCreatures(targets);
	targeter.addCallbackArgument(item);
	targeter.setMenuTitle("Raise Dead");
	targeter.activate();
	
	game.hideOpenWindows();
}

function onTargetSelect(game, targeter, item) {
    var parent = targeter.getParent();
	var target = targeter.getSelectedCreature();
	
	target.raiseFromDead();
	
	game.addMessage("blue", parent.getTemplate().getName() + " uses a scroll of raise dead on " +
	    target.getTemplate().getName());
	
	parent.inventory.remove(item);
	
	game.ai.provokeAttacksOfOpportunity(parent);
}
