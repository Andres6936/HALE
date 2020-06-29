function onUse(game, item, target) {
    var quality = item.getQuality().getModifier() + 100;

    var bonus = parseInt(quality / 100);
    
    game.addMessage("blue", target.getName() + " drinks a potion.");
    
    var effect = target.createEffect();
    effect.setTitle(item.getName());
    effect.setDuration(10 + quality / 100);
    effect.getBonuses().addDamageReduction('Physical', bonus);
    target.applyEffect(effect);
    
    target.inventory.remove(item);
    game.ai.provokeAttacksOfOpportunity(target);
}
