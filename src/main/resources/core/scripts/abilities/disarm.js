function isTargetValid(game, target, slot) {
	var weapon = target.inventory.getEquippedMainHand();
	
	if (weapon == null || weapon.getTemplate().getBaseWeapon().getName().equals("Unarmed"))
		return false;
	
	return true;
}

function canActivate(game, parent) {
	if (!parent.timer.canAttack()) return false;
	
	var weapon = parent.getMainHandWeapon();
	
	return weapon.isMelee();
}

function onActivate(game, slot) {
	var creatures = game.ai.getAttackableCreatures(slot.getParent());

	for (var i = 0; i < creatures.size(); i++) {
		if ( !isTargetValid(game, creatures.get(i)) ) {
			creatures.remove(i);
			i--;
		}
	}
	
	var targeter = game.createListTargeter(slot);
	targeter.addAllowedCreatures(creatures);

	targeter.setMenuTitle(slot.getAbility().getName());

	targeter.activate();
}

function onTargetSelect(game, targeter) {
	targeter.getSlot().activate();

	var ability = targeter.getSlot().getAbility();

	// perform the attack in a new thread as the melee touch attack will block
	var cb = ability.createDelayedCallback("performAttack");
	cb.addArgument(targeter);
	cb.start();
}

function performAttack(game, targeter) {
	var parent = targeter.getParent();
	var target = game.currentArea().getCreatureAtGridPoint(targeter.getSelected());

	if (!isTargetValid(game, target))
		return;
	
	var parentWeapon = parent.getMainHandWeapon();
	var targetWeapon = target.inventory.getEquippedMainHand();
	
	var bonus = 0;
	if (!targetWeapon.isMelee()) {
		bonus = 30;
	}
	
	var effect = parent.createEffect();
	effect.setTitle(targeter.getSlot().getAbility().getName());
	effect.getBonuses().addBonus('Attack', 'Stackable', bonus);
	parent.applyEffect(effect);
	
	parent.timer.performAttack();
	
	if (game.meleeTouchAttack(parent, target)) {
		// touch attack succeeded
		
		var checkDC = 50 + 2 * (parent.stats.getStr() - 10) +
			parent.stats.getLevelAttackBonus() / 2;
		
		if (!target.stats.getReflexResistanceCheck(checkDC)) {
			// target failed reflex check
			
			target.inventory.removeEquippedItem("MainHand");
			game.addItemToArea(targetWeapon, target.getLocation());
			
			game.addMessage("red", parent.getName() + " disarms " + target.getName() + ".");
			
		} else {
			game.addMessage("red", parent.getName() + " fails to disarm " + target.getName() + ".");
		}
	} else {
		game.addMessage("red", parent.getName() + " misses disarm attempt.");
	}
	
	parent.removeEffect(effect);
}