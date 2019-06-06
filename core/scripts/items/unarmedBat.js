
function onAttackHit(game, weapon, attack, damage) {
    var parent = attack.getAttacker();
    
    parent.healDamage(parseInt(damage / 2));
}
