function enableDefensiveFighting(game, slot, level) {
   var effect = slot.createEffect();

   effect.setTitle(slot.getAbility().getName());
   effect.addPositiveIcon("items/enchant_armor_small");
   effect.getBonuses().addPenalty('Attack', 'Stackable', -level);
   effect.getBonuses().addBonus('ArmorClass', 'Deflection', level);
   effect.setDuration(10);   

   slot.getParent().applyEffect(effect);

   slot.setActiveRoundsLeft(10);

   slot.activate();
}

function onActivate(game, slot) {
   if (!game.addMenuLevel("Defensive Fighting")) return;

   for (var i = 5; i <= 20; i += 5) {
      var cb = game.createButtonCallback(slot, "enableDefensiveFighting");
      cb.addArgument(i);

      game.addMenuButton("AC +" + i + ", Attack -" + i, cb);
   }
   
   game.showMenu();
}

function onDeactivate(game, slot) {
   slot.deactivate();
}
