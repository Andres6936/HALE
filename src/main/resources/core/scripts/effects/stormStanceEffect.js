function onRoundElapsed(game, effect) {
	var parent = effect.getSlot().getParent();
	
	if (effect.get("stance") == "Stone Form" && parent.abilities.has("StrengthOfTheEarth")) {
		if (!parent.isDying() && !parent.isDead() && parent.getCurrentHitPoints() < parent.stats.getMaxHP()) {
			parent.healDamage(parent.stats.getWis() - 10);
		}
	}
}

function onAttackHit(game, attack, damage, effect) {
	var parent = attack.getAttacker();
	var target = attack.getDefender();
	
	if (effect.get("stance") == "Burning Palm" && parent.abilities.has("SearingImpact")) {
		searingImpact(game, effect, parent, target);
	} else if (effect.get("stance") == "Hurricane Claw" && parent.abilities.has("SonicStrike")) {
		sonicStrike(game, effect, parent, target);
	}
}

function searingImpact(game, effect, parent, target) {
	var checkDC = 50 + 4 * (parent.stats.getWis() - 10) + parent.roles.getLevel("Storm Hand") * 4;
	if (!target.stats.getPhysicalResistanceCheck(checkDC)) {
		var callback = effect.createDelayedCallback("applySearingImpactEffect");
		callback.setDelay(0.5);
		callback.addArguments([ effect, target ]);
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
	}
}

function applySearingImpactEffect(game, effect, target) {
	var targetEffect = effect.getSlot().createEffect();
	targetEffect.setDuration(5);
	targetEffect.setTitle("Searing Impact");
	targetEffect.getBonuses().addPenalty("Con", "Stackable", -1);
	targetEffect.addNegativeIcon("items/enchant_death_small");
	target.applyEffect(targetEffect);
}

function sonicStrike(game, effect, parent, target) {
	var anim = game.createAnimation("particles/ring72", 0.5);
	anim.setAlpha(1.0);
	anim.setAlphaSpeed(-3.0);
	var pos = target.getLocation().getCenteredScreenPoint();
	anim.setPosition(pos.x, pos.y);
	
	game.runAnimationNoWait(anim);
	
	var callback = effect.createDelayedCallback("applySonicStrikeDamage");
    callback.setDelay(0.5);
    callback.addArguments([effect, parent, target]);
	callback.start();
}

function applySonicStrikeDamage(game, effect, parent, target) {
	target.takeDamage(game.dice().d4(), "Sonic");
	var adj = game.getAdjacentHexes(target.getLocation().toPoint());
	for ( var i = 0; i < adj.length; i++ ) {
		var subTarget = game.currentArea().getCreatureAtGridPoint(adj[i]);
		
		if (subTarget == null) continue;
		
		if (parent.getFaction().isHostile(subTarget)) {
			subTarget.takeDamage(game.dice().d4(), "Sonic");
		}
	}
}
