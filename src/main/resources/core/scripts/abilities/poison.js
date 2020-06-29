function canActivate(game, parent) {
	var weapon = parent.inventory.getEquippedMainHand();
	
	return weapon != null;
}

function onActivate(game, slot) {
	var ability = slot.getAbility();
	var parent = slot.getParent();
	
	var weapon = parent.inventory.getEquippedMainHand();
	if (weapon == null) return;
	
	var lvls = parent.roles.getLevel("Assassin");
	
	var duration = 5;
	if (parent.abilities.has("LingeringPoison"))
		duration += 3;
	
	slot.setActiveRoundsLeft(duration);
	slot.activate();
	
	var effect = slot.createEffect("effects/poison");
	effect.setDuration(duration);
	effect.addPositiveIcon("items/enchant_acid_small");
	effect.setTitle(ability.getName());
	effect.put("attacksLeft", lvls + 3);
	
	var generator = game.getBaseParticleGenerator("flame");
	generator.setVelocityDistribution(game.getFixedAngleDistribution(20.0, 35.0, 3.14159 / 2));
	generator.setGreenSpeedDistribution(game.getFixedDistribution(0.0));
	generator.setRedDistribution(game.getFixedDistribution(0.5));
	generator.setBlueDistribution(game.getFixedDistribution(0.6));
	generator.setRedSpeedDistribution(game.getGaussianDistribution(-0.5, 0.05));
	generator.setBlueSpeedDistribution(game.getGaussianDistribution(-4.0, 0.05));
	if (parent.drawsWithSubIcons()) {
		var subIconType = weapon.getTemplate().getSubIconTypeOverride();
		
		if (subIconType != null) {
			var pos = parent.getSubIconScreenPosition(subIconType.name());
			generator.setPosition(pos.x, pos.y);
		} else {
			var pos = parent.getSubIconScreenPosition("MainHandWeapon");
			generator.setPosition(pos.x - 5.0, pos.y - 5.0);
		}
	} else {
		var pos = parent.getLocation().getCenteredScreenPoint();
		generator.setPosition(pos.x - 15.0, pos.y - 15.0);
	}
	effect.addAnimation(generator);
	
	weapon.applyEffect(effect);
}
