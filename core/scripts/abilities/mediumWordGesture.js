function onActivate(game, slot) {
   var abilityID = slot.getAbilityID();
   var parent = slot.getParent();
   var duration = 2;
   
   slot.setActiveRoundsLeft(duration);
   slot.activate();
   parent.timer.performAction(5000);
	   
   var effect = slot.createEffect("abilities/mediumWordGesture");
   effect.setDuration(duration);
   
   // do a flash icon animation
   var anim = game.getBaseAnimation("iconFlash");
   if (parent.drawsWithSubIcons()) {
	  anim.addFrame(parent.getIconRenderer().getIcon("BaseForeground"));
	  anim.setColor(parent.getIconRenderer().getColor("BaseForeground"));
		
	  var pos = parent.getSubIconScreenPosition("BaseForeground");
	  anim.setPosition(pos.x, pos.y);
   } else {
	  anim.addFrameAndSetColor(parent.getTemplate().getIcon());
	  var pos = parent.getLocation().getCenteredScreenPoint();
	  anim.setPosition(pos.x, pos.y);
   }
   
   if (abilityID.startsWith("Word")) {
	  var wordString = abilityID.substring(4);
	  effect.setTitle("Medium Word: " + wordString);
      effect.put("parentMediumValue", "roleMediumWord" + wordString);
      parent.put("roleMediumWord" + wordString, true);
	  
	  anim.setSecondaryRed(0.0);
	  
   } else if (abilityID.startsWith("Gesture")) {
	  var gestureString = abilityID.substring(7);
	  effect.setTitle("Medium Gesture: " + gestureString);
      effect.put("parentMediumValue", "roleMediumGesture" + gestureString);
      parent.put("roleMediumGesture" + gestureString, true);
	  
	  anim.setSecondaryGreen(0.0);
   }
   
   game.runAnimationNoWait(anim);
   game.lockInterface(anim.getSecondsRemaining());
   
   parent.applyEffect(effect);
}

function canActivate(game, parent, slot) {
	var abilityID = slot.getAbilityID();
    var parent = slot.getParent();
	
	var numWords = game.runExternalScript("abilities/mediumInvocation", "getNumActiveWords", parent);
	var numGestures = game.runExternalScript("abilities/mediumInvocation", "getNumActiveGestures", parent);
	
	if (abilityID.startsWith("Word")) {
		// a word
		if (numWords + 1 > numGestures) {
			return parent.timer.canPerformAction(5000);
		}
	} else if (abilityID.startsWith("Gesture")) {
		// a gesture
		if (numGestures + 1 > numWords) {
			return parent.timer.canPerformAction(5000);
		}
	}
	
	return true;
}

// called on the removal of the word / gesture effect
function onRemove(game, effect) {
	effect.getTarget().put(effect.get("parentMediumValue"), false);
}
