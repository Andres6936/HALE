function canActivate(game, parent) {
	if (!parent.timer.canAttack()) return false;
	
	var weapon = parent.getMainHandWeapon();
	
	return weapon.getTemplate().getBaseWeapon().getName() == "Unarmed";
}

function onActivate(game, slot) {
   var creatures = game.ai.getAttackableCreatures(slot.getParent());

   var targeter = game.createListTargeter(slot);
   targeter.addAllowedCreatures(creatures);
   targeter.activate();
}

function onTargetSelect(game, targeter) {
   targeter.getSlot().activate();
   
   var ability = targeter.getSlot().getAbility();

   // perform the attack in a new thread as the standardAttack will
   // block
   var cb = ability.createDelayedCallback("performAttack");
   cb.addArgument(targeter);
   cb.start();
}

function performAttack(game, targeter) {
	var parent = targeter.getParent();
	var target = targeter.getSelectedCreature();
	
	if (game.standardAttack(parent, target)) {
		// attack suceeded
		var checkDC = 50 + 4 * (parent.stats.getWis() - 10) +
			parent.roles.getLevel("Monk") * 4;
		
		if (target.stats.has("CriticalHitImmunity")) {
			game.addMessage("red", parent.getName() + " attempted Slaying Blow against " + target.getName() + ", but they are immune.");
			return;
		}
		
		if (!target.stats.getPhysicalResistanceCheck(checkDC)) {
			// target failed check
			applyEffect(game, parent, target, targeter, 2.0/3.0);
			
			game.addMessage("red", parent.getName() + " succeeded on Slaying Blow against " + target.getName() + ".");
		} else {
			applyEffect(game, parent, target, targeter, 1.0/3.0);
			
			game.addMessage("red", parent.getName() + " failed on Slaying Blow against " + target.getName() + ".");
			game.addFadeAway("Failed", target.getLocation().getX(), target.getLocation().getY(), "gray");
		}
	} else {
		game.addMessage("red", parent.getName() + " missed on Slaying Blow attempt.");
	}
}

function applyEffect(game, parent, target, targeter, fraction) {
	var callback = targeter.getSlot().getAbility().createDelayedCallback("applyDamage");
	callback.setDelay(1.0);
	callback.addArguments([target, target.stats.getMaxHP() * fraction]);
	callback.start();
	
	if (target.drawsWithSubIcons()) {
		var anim = game.getBaseAnimation("iconFlash");
		anim.addFrame(target.getIconRenderer().getIcon("BaseForeground"));
		anim.setColor(target.getIconRenderer().getColor("BaseForeground"));
		
		var pos = target.getSubIconScreenPosition("BaseForeground");
		anim.setPosition(pos.x, pos.y);
	} else {
		var anim = game.getBaseAnimation("iconFlash");
		anim.addFrameAndSetColor(target.getTemplate().getIcon());
		var pos = target.getLocation().getCenteredScreenPoint();
		anim.setPosition(pos.x, pos.y);
	}
	
	anim.setSecondaryGreen(0.0);
    anim.setSecondaryBlue(0.0);
	game.runAnimationNoWait(anim);
	game.lockInterface(anim.getSecondsRemaining());
}

function applyDamage(game, target, amount) {
	target.takeDamage(amount, "Effect");
}