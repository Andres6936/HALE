function onActivate(game, slot) {
	var parent = slot.getParent();

	var creatures = game.ai.getTouchableCreatures(slot.getParent(), "Friendly");
	for (var i = 0; i < creatures.size(); i++) {
		if (creatures.get(i) == parent) {
			creatures.remove(i);
			i--;
		}
	}
	
	var targeter = game.createListTargeter(slot);
	targeter.addAllowedCreatures(creatures);
	targeter.activate();
}

function onTargetSelect(game, targeter) {
	var ability = targeter.getSlot().getAbility();
	var parent = targeter.getParent();
	
	var duration = 2;
	
	// fire the ability
	targeter.getSlot().setActiveRoundsLeft(duration);
	targeter.getSlot().activate();
	
	var target = targeter.getSelectedCreature();

	var chaBonus = (parent.stats.getCha() - 10) * 4;
	if (parent.abilities.has("PersonalMagnetism"))
		chaBonus = chaBonus * 2;
	
	var lvlBonus = parent.roles.getLevel("Paladin") * 4;
	
	var amount = chaBonus + lvlBonus;
	
	var effect = targeter.getSlot().createEffect();
	effect.setDuration(duration);
	effect.setTitle(ability.getName());
	effect.addPositiveIconddPositiveIcon("items/enchant_attack_small");
	effect.addPositiveIcon("items/enchant_armor_small");
	effect.getBonuses().add('UndispellableImmobilized');
	effect.getBonuses().addBonus('ArmorClass', 'Deflection', amount);
	effect.getBonuses().addBonus('MeleeSpellFailure', 'Morale', amount);
	effect.getBonuses().addBonus('Attack', 'Morale', amount);
	target.applyEffect(effect);
	
	var parentEffect = targeter.getSlot().createEffect();
	parentEffect.setDuration(duration);
	parentEffect.setTitle(ability.getName());
	parentEffect.getBonuses().add('UndispellableImmobilized');
	parent.applyEffect(parentEffect);
}