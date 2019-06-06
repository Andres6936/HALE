function onUse(game, item, target) {
    var quality = item.getQuality().getModifier() + 100;
    var hp = game.dice().d((10 * quality) / 100, 3);
    
    game.addMessage("blue", target.getName() + " drinks a potion.");
    
    target.healDamage(hp);
    
    target.inventory.remove(item);
    
    game.ai.provokeAttacksOfOpportunity(target);
}