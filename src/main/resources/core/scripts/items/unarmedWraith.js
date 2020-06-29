function onAttackHit(game, weapon, attack, damage) {
    var attacker = attack.getAttacker();
    var defender = attack.getDefender();
    
    var effect = attacker.createEffect();
    effect.setTitle("Wraith Drain");
    effect.setDuration(game.dice().rand(2, 6));
    effect.getBonuses().addPenalty('Con', 'Stackable', -1);
    
    defender.applyEffect(effect);
}
