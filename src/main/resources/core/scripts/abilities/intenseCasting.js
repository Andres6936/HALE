function onActivate(game, slot) {
   if (!game.addMenuLevel("Intense Casting")) return;

   for (var i = 5; i <= 15; i += 5) {
      var cb = game.createButtonCallback(slot, "enableIntenseCasting");
      cb.addArgument(i);

	  var failure = 5 + i;
	  
      game.addMenuButton("Bonus +" + i + ", Failure +" + failure, cb);
   }
   
   game.showMenu();
}

function enableIntenseCasting(game, slot, level) {
	var effect = slot.createEffect();
	effect.setTitle(slot.getAbility().getName());
	effect.addPositiveIcon("items/enchant_spellDamage_small");
	
	effect.getBonuses().addPenalty('SpellFailure', -(level + 5));
	effect.getBonuses().addBonus('SpellDamage', level);
	effect.getBonuses().addBonus('SpellHealing', level);
	effect.getBonuses().addBonus('SpellDuration', level);
	
	effect.setRemoveOnDeactivate(true);
	slot.getParent().applyEffect(effect);
	slot.activate();
}

function onDeactivate(game, slot) {
   slot.deactivate();
}