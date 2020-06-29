function onRoundElapsed(game, effect) {
	var spell = effect.getSlot().getAbility();
	var parent = effect.getSlot().getParent();
	var casterLevel = parent.stats.getCasterLevel();
	
	var hasLightning = parent.abilities.has("LightningStorm");
	
	var targets = effect.getTarget().getAffectedCreatures(effect);
	for (var i = 0; i < targets.size(); i++) {
		var target = targets.get(i);
		
		if (hasLightning) checkLightning(game, effect, target);
		
		var damage = parseInt(game.dice().d6(1) + casterLevel / 3);
		
		spell.applyDamage(parent, target, damage, "Cold");
	}
}

function checkLightning(game, effect, target) {
	var roll = game.dice().d3();
	// 33% probability of being struck
	if (roll != 3) return;

	var spell = effect.getSlot().getAbility();
	var parent = effect.getSlot().getParent();
	var casterLevel = parent.stats.getCasterLevel();
	
	var damage = game.dice().d10() + casterLevel;
	
	var callback = spell.createDelayedCallback("applyLightningDamage");
	callback.setDelay(0.2);
	callback.addArguments([parent, target, damage, spell]);
		
	// create the animation
	var anim = game.getBaseAnimation("blast");
	var position = target.getLocation().getCenteredScreenPoint();
	anim.setPosition(position.x, position.y);
		
	game.runAnimationNoWait(anim);
	game.lockInterface(anim.getSecondsRemaining());
	callback.start();
}

function onTargetEnter(game, target, effect) {
	var spell = effect.getSlot().getAbility()
	var parent = effect.getSlot().getParent();
	var casterLevel = parent.stats.getCasterLevel();
	var damage = parseInt(game.dice().d4(2) + casterLevel / 2);
	
	spell.applyDamage(parent, target, damage, "Cold");
}
