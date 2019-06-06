function isTargetValid(game, target, slot) {
	var weapon = target.inventory.getEquippedMainHand();
	
	if (weapon == null || !weapon.isMelee())
		return false;
		
	return true;
}

function onActivate(game, slot) {
	var creatures = game.ai.getTouchableCreatures(slot.getParent(), "Friendly");
	
	for (var i = 0; i < creatures.size(); i++) {
		if ( !isTargetValid(game, creatures.get(i), slot) ) {
			creatures.remove(i);
			i--;
		}
	}
	
	var targeter = game.createListTargeter(slot);
	targeter.addAllowedCreatures(creatures);
	targeter.activate();
}

function onTargetSelect(game, targeter) {
	var spell = targeter.getSlot().getAbility();
	var parent = targeter.getParent();
	var target = targeter.getSelectedCreature();
	var casterLevel = parent.stats.getCasterLevel();
	
	if (!isTargetValid(game, target)) return;
	
	var duration = game.dice().randInt(5, 10);
	
	targeter.getSlot().setActiveRoundsLeft(duration);
	targeter.getSlot().activate();
	
	if (!spell.checkSpellFailure(parent, target)) return;
	
	var effect = targeter.getSlot().createEffect();
	effect.setDuration(duration);
	effect.setTitle(spell.getName());
	
	// we have already validated the weapon
	var weapon = target.inventory.getEquippedMainHand();
	
	effect.getBonuses().addBonus("WeaponAttack", 5 + casterLevel);
	effect.getBonuses().addBonus("WeaponCriticalChance", 10);
	
	if (targeter.getSlot().getParent().abilities.has("FlamingWeapon")) {
		// apply the flaming weapon effect
	
		var minDamage = parseInt(1 + casterLevel / 4);
		var maxDamage = parseInt(4 + casterLevel / 4);
		effect.getBonuses().addStandaloneDamageBonus("Fire", minDamage, maxDamage);
		
		var generator = game.getBaseParticleGenerator("flame");
		
		if (target.drawsWithSubIcons()) {
			var pos = target.getSubIconScreenPosition("MainHandWeapon");
			generator.setPosition(pos.x - 5.0, pos.y - 5.0);
		} else {
			var pos = target.getLocation().getCenteredScreenPoint();
			generator.setPosition(pos.x - 15.0, pos.y - 15.0);
		}
		
		effect.addAnimation(generator);
	}
	
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

function aiCheckTargetValid(game, slot, targetPosition) {
	var target = targetPosition.getCreature();
	if (target == null) {
		return false;
	}

	if ( !isTargetValid(game, target) ) {
		return false;
	}
	
	var weapon = target.inventory.getEquippedMainHand();
	var effect = weapon.getEffects().getEffectCreatedBySlot(slot.getAbilityID());
	
	if (effect != null) {
		return false;
	} else {
		return true;
	}
}