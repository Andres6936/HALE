function onActivate(game, slot) {
	var targeter = game.createListTargeter(slot);
	targeter.addAllowedCreature(slot.getParent());
	targeter.activate();
}

function onTargetSelect(game, targeter) {
	var ability = targeter.getSlot().getAbility();
	var parent = targeter.getParent();
	var target = parent;
	
	var duration = 3;
	
	// fire the ability
	targeter.getSlot().setActiveRoundsLeft(duration);
	targeter.getSlot().activate();
	
	var chaBonus = (parent.stats.getCha() - 10) * 2;
	if (parent.abilities.has("PersonalMagnetism"))
		chaBonus = chaBonus * 2;
	
	var lvlBonus = parent.roles.getLevel("Paladin");
	
	var amount = chaBonus + lvlBonus;
	
	var weapon = target.getMainHandWeapon();
	
	var effect = targeter.getSlot().createEffect();
	effect.setDuration(duration);
	effect.setTitle(ability.getName());
	effect.getBonuses().addStandaloneDamageBonus('Divine', amount, amount);
	weapon.applyEffect(effect);
	
	if (target.drawsWithSubIcons()) {
		var anim = game.getBaseAnimation("subIconFlash");
		anim.addFrame(target.getIconRenderer().getIcon("MainHandWeapon"));
		anim.setColor(target.getIconRenderer().getColor("MainHandWeapon"));
		
		var pos = target.getSubIconScreenPosition("MainHandWeapon");
		anim.setPosition(pos.x, pos.y);
	} else {
		var anim = game.getBaseAnimation("iconFlash");
		anim.addFrameAndSetColor(target.getTemplate().getIcon());
		var pos = target.getLocation().getCenteredScreenPoint();
		anim.setPosition(pos.x, pos.y);
	}
		
	game.runAnimationNoWait(anim);
	game.lockInterface(anim.getSecondsRemaining());
}
