function onActivate(game, slot) {
	var creatures = game.ai.getLiveVisibleCreatures(slot.getParent(), "Hostile");
	
	var targeter = game.createListTargeter(slot);
	targeter.addAllowedCreatures(creatures);
	targeter.setUniqueSelectionsRequired(true);
	
	// don't do multi selection mode unless there are at least two valid selections
	if (creatures.size() > 1 && slot.getParent().abilities.has("MultiDuel"))
		targeter.setNumberOfSelections(2);
	
	targeter.activate();
}

function onTargetSelect(game, targeter) {
	var parent = targeter.getParent();
	var targets = targeter.getSelectedCreatures();
	
	var duration = 3;
	
	targeter.getSlot().setActiveRoundsLeft(duration);
    targeter.getSlot().activate();
	
	var parentTitle = targeter.getSlot().getAbility().getName() + " against";
	
	for (var i = 0; i < targets.size(); i++) {
		var target = targets.get(i);
		
		var effect = targeter.getSlot().createEffect("effects/duel");
		effect.setHasDescription(false);
		effect.setDuration(duration);
		target.applyEffect(effect);
		
		if (i == 0)
			parentTitle = parentTitle + " " + target.getName();
		else
			parentTitle = parentTitle + " and " + target.getName();
	}
	
	var parentEffect = targeter.getSlot().createEffect();
	parentEffect.setTitle(parentTitle);
	parentEffect.setDuration(duration);
	parent.applyEffect(parentEffect);
}
