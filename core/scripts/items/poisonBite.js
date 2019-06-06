function onAttackHit(game, weapon, attack, damage) {
    var defender = attack.getDefender();
    var parent = attack.getAttacker();
    
    
    if (!defender.stats.getPhysicalResistanceCheck(70)) {
        var effect = parent.createEffect("effects/damageOverTime");
        effect.setDuration(2);
        effect.setTitle("Snake Bite Poison");
        effect.put("damagePerRound", 3);
        effect.put("damageType", "Acid");
        defender.applyEffect(effect);
        game.addMessage("red", defender.getName() + " has been poisoned!");
    }
}
