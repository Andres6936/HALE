function onAttack(game, attack, effect) {
	var attacker = attack.getAttacker();
	var target = attack.getDefender();
	
	if (!isBackstab(game, attack)) return;
	
	var mastery = attacker.abilities.has("BackstabMastery");
	
	var charLevel = attacker.roles.getLevel("Rogue");
	
	var extraDamage = 3 + game.dice().randInt(charLevel / 2, charLevel);
	var extraAttack = 10 + charLevel;
	
	if (mastery) {
		extraDamage += 3;
		extraAttack += parseInt(charLevel / 2);
	}
	
	game.addMessage("red", attack.getAttacker().getName() + " gets Backstab against " + attack.getDefender().getName());
	
	attack.addExtraDamage(extraDamage);
	attack.addExtraAttack(extraAttack);
}

function onAttackHit(game, attack, damage, effect) {
	var attacker = attack.getAttacker();
	var target = attack.getDefender();

	var cripple = attacker.abilities.has("CripplingBackstab");
	if (!cripple) return;

	if (!isBackstab(game, attack)) return;
	
	var mastery = attacker.abilities.has("BackstabMastery");
	
	var charLevel = attacker.roles.getLevel("Rogue");
	
	var dc = 50 + charLevel * 4;
	
	if (mastery) {
		dc += charLevel;
	}
	
	if (!target.stats.getPhysicalResistanceCheck(dc)) {
		var crippleEffect = attacker.createEffect();
		
		crippleEffect.setDuration(3);
		crippleEffect.setTitle("Crippling Backstab");
		
		crippleEffect.getBonuses().addPenalty('Str', 'Stackable', -1);
		crippleEffect.getBonuses().addPenalty('Dex', 'Stackable', -1);
		crippleEffect.addNegativeIcon("items/enchant_strength_small");
		crippleEffect.addNegativeIcon("items/enchant_dexterity_small");
		target.applyEffect(crippleEffect);
	}
}

function isBackstab(game, attack) {
	var attacker = attack.getAttacker();
	var target = attack.getDefender();
	
	var improved = attacker.abilities.has("ImprovedBackstab");
	var ranged = attacker.abilities.has("RangedBackstab");
	var mastery = attacker.abilities.has("BackstabMastery");
	
	var rangedDistance = 3;
	if (mastery) rangedDistance += 1;
	
	// attack must be melee unless the attacker has ranged backstab,
	// in which case max distance is 3 or 4 tiles
	if (!attack.isMeleeWeaponAttack()) {
		if (!ranged || attacker.getLocation().getDistance(target) > rangedDistance) {
			return false;
		}
	}
	
	// attacker must be hidden or target helpless / immobilized unless
	// attacker has improved backstab, in which case flanking is sufficient
	if (!improved || !attack.isFlankingAttack()) {
		if (!attacker.stats.has("Hidden") && !target.stats.isHelpless() && !target.stats.isImmobilized()) {
			return false;
		}
	}
	
	return true;
}