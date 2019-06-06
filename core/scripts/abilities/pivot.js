function canActivate(game, parent) {
	if (!parent.timer.canAttack()) return false;
	
	var weapon = parent.getMainHandWeapon();
	
	return weapon.isMelee();
}

function onActivate(game, slot) {
   var creatures = game.ai.getAttackableCreatures(slot.getParent());

   var targeter = game.createListTargeter(slot);
   targeter.addAllowedCreatures(creatures);
   targeter.addCallbackArgument("creatureSelect");
   targeter.activate();
}

function onTargetSelect(game, targeter, targeterType, target) {
   if (targeterType == "creatureSelect") {
	   // perform phase two of the target selection
	   var target = targeter.getSelectedCreature();
	   var targeter2 = game.createListTargeter(targeter.getSlot());
	   
	   var adj = game.getAdjacentHexes(targeter.getSelectedCreature().getLocation().toPoint());
	   
	   for ( var i = 0; i < adj.length; i++ ) {
		   if (game.currentArea().isFreeForCreature(adj[i].x, adj[i].y)) {
			   targeter2.addAllowedPoint(adj[i]);
		   }
	   }
	   
	   targeter2.addCallbackArgument("locationSelect");
	   targeter2.addCallbackArgument(targeter.getSelectedCreature());
	   targeter2.activate();
   } else {
	  targeter.getSlot().activate();
   
      var ability = targeter.getSlot().getAbility();

      // perform the attack in a new thread as the standardAttack will
      // block
      var cb = ability.createDelayedCallback("performAttack");
      cb.addArgument(targeter);
	  cb.addArgument(target);
      cb.start();
   }
}

function performAttack(game, targeter, target) {
	var parent = targeter.getParent();
	
	game.standardAttack(parent, target)
	
	var movePoint = targeter.getSelected();
	game.moveCreature(parent, movePoint.x, movePoint.y);
}
