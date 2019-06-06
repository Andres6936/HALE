function isTargetValid(game, target, slot) {
	var armor = target.inventory.getEquippedArmor();
	
	if (armor == null || armor.getTemplate().getArmorType().getName().equals("None")) {
		return false;
	}
	
	return true;
}

function onActivate(game, slot) {
	if (slot.getParent().abilities.has("ResistWeapons")) {
		showResistWeaponsMenu(game, slot);
	} else {
		createTargeter(game, slot, null);
	}
}

function showResistWeaponsMenu(game, slot) {
	if (!game.addMenuLevel(slot.getAbility().getName())) return;

	var types = ["Slashing", "Piercing", "Blunt"];
	
	for (var index = 0; index < types.length; index++ ) {
		var type = types[index];
	
		var cb = game.createButtonCallback(slot, "createTargeter");
		cb.addArgument(type);
		
		game.addMenuButton(type, cb);
	}
	
	game.showMenu();
}

function createTargeter(game, slot, type) {
	var creatures = game.ai.getTouchableCreatures(slot.getParent(), "Friendly");
	
	for (var i = 0; i < creatures.size(); i++) {
		if ( !isTargetValid(game, creatures.get(i)) ) {
			creatures.remove(i);
			i--;
		}
	}
	
	var targeter = game.createListTargeter(slot);
	targeter.addAllowedCreatures(creatures);
	targeter.addCallbackArgument(type);
	targeter.activate();
}

function onTargetSelect(game, targeter, type) {
	var spell = targeter.getSlot().getAbility();
	var parent = targeter.getParent();
	var target = targeter.getSelectedCreature();
	var casterLevel = parent.stats.getCasterLevel();
	
	if (!isTargetValid(game, target, targeter.getSlot())) return;
	
	var duration = parseInt(3 + casterLevel / 4);
	
	targeter.getSlot().setActiveRoundsLeft(duration);
	targeter.getSlot().activate();
	
	if (!spell.checkSpellFailure(parent, target)) return;
	
	var effect = targeter.getSlot().createEffect();
	effect.setDuration(duration);
	effect.setTitle(spell.getName());
	effect.getBonuses().addDamageReduction("Physical", 4 + parseInt(casterLevel / 6));
	effect.getBonuses().addBonus('ArmorPenalty', 10);
	
	if (parent.abilities.has("ResistWeapons")) {
		effect.getBonuses().addDamageImmunity(type, 20 + casterLevel);
	}
	
	// we already checked that the armor exists
	var armor = target.inventory.getEquippedArmor();
	armor.applyEffect(effect);
		
	if (target.drawsWithSubIcons() && target.getIconRenderer().getSubIcon("Torso") != null) {
		var anim = game.getBaseAnimation("subIconFlash");
		anim.addFrame(target.getIconRenderer().getIcon("Torso"));
		anim.setColor(target.getIconRenderer().getColor("Torso"));
			
		var pos = target.getSubIconScreenPosition("Torso");
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
	
	var armor = target.inventory.getEquippedArmor();
	var effect = armor.getEffects().getEffectCreatedBySlot(slot.getAbilityID());
	
	if (effect != null) {
		return false;
	} else {
		return true;
	}
}