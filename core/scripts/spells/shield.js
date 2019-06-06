function onActivate(game, slot) {
	if (slot.getParent().abilities.has("ElementalShield")) {
		showElementalMenu(game, slot);
	} else {
		var targeter = game.createListTargeter(slot);
		targeter.addAllowedCreature(slot.getParent());
		targeter.activate();
	}
}

function showElementalMenu(game, slot) {
	if (!game.addMenuLevel("Elemental Shield")) return;

	var types = ["Fire", "Cold", "Electrical", "Acid"];
	
	for (var index = 0; index < types.length; index++ ) {
		var type = types[index];
	
		var cb = game.createButtonCallback(slot, "castSpell");
		cb.addArgument(type);
		
		game.addMenuButton(type, cb);
	}
	
	game.showMenu();
}

function castSpell(game, slot, type) {
	var targeter = game.createListTargeter(slot);
	targeter.addAllowedCreature(slot.getParent());
	targeter.addCallbackArgument(type);
	targeter.activate();
}

function onTargetSelect(game, targeter, type) {
	var spell = targeter.getSlot().getAbility();
	var parent = targeter.getParent();
	var target = targeter.getSelectedCreature();
	var casterLevel = parent.stats.getCasterLevel();
	
	var duration = parseInt(3 + casterLevel / 3);
	
	targeter.getSlot().setActiveRoundsLeft(duration);
	targeter.getSlot().activate();
	
	if (!spell.checkSpellFailure(parent, target)) return;
	
	var effect;
	
	if (targeter.getSlot().getParent().abilities.has("ElementalShield")) {
		var dr = parseInt(5 + casterLevel / 2);
	
		effect = targeter.getSlot().createEffect("effects/elementalShield");
		effect.getBonuses().addDamageReduction(type, dr);
		effect.put("type", type);
		
		var g1 = game.getBaseParticleGenerator("continuousRing");
		g1.setPosition(target.getLocation());
		if (type == "Fire") {
			g1.setGreenSpeedDistribution(game.getGaussianDistribution(-1.0, 0.05));
			g1.setBlueSpeedDistribution(game.getGaussianDistribution(-6.0, 0.05));
		} else if (type == "Cold") {
			// default white color is fine
		} else if (type == "Electrical") {
			g1.setGreenDistribution(game.getFixedDistribution(0.9));
			g1.setRedDistribution(game.getFixedDistribution(0.5));
			g1.setGreenSpeedDistribution(game.getGaussianDistribution(-1.0, 0.05));
			g1.setRedSpeedDistribution(game.getGaussianDistribution(-1.0, 0.05));
		} else if (type == "Acid") {
			g1.setRedDistribution(game.getFixedDistribution(0.5));
			g1.setBlueDistribution(game.getFixedDistribution(0.5));
			g1.setBlueSpeedDistribution(game.getGaussianDistribution(-1.0, 0.05));
			g1.setRedSpeedDistribution(game.getGaussianDistribution(-2.0, 0.05));
		}

		effect.addAnimation(g1);
		
	} else {
		effect = targeter.getSlot().createEffect();
	}
	
	effect.addPositiveIcon("items/enchant_armor_small");
	
	// add the basic shield animation
	var anim = game.getBaseAnimation("shieldLoop");
	var position = target.getLocation().getCenteredScreenPoint();
	anim.setPosition(position.x, position.y - 10.0);
	anim.setDurationInfinite();
	effect.addAnimation(anim);
	
	effect.setDuration(duration);
	effect.setTitle(spell.getName());
	
	var lvls = parent.roles.getLevel("War Wizard");
	var acBonus = parseInt(15 + casterLevel + 4 * lvls);
	
	
	effect.getBonuses().addBonus('ArmorClass', 'Shield', acBonus);
	
	target.applyEffect(effect);
	
	var anim = game.getBaseAnimation("shieldFlash");
	var position = target.getLocation().getCenteredScreenPoint();
	anim.setPosition(position.x, position.y);
	
	game.runAnimationNoWait(anim);
	game.lockInterface(anim.getSecondsRemaining());
}