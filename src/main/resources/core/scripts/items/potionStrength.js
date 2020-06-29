function onUse(game, item, target) {
    var quality = item.getQuality().getModifier() + 100;
    var strBonus = 3 * quality / 100;
    
    game.addMessage("blue", target.getName() + " drinks a potion of strength.");

    var effect = target.createEffect();
    effect.setTitle("Potion of Strength");
    effect.setDuration(10 + quality / 100);
    effect.getBonuses().addBonus('Str', parseInt(strBonus));

    target.applyEffect(effect);
    
    target.inventory.remove(item);
    
    game.ai.provokeAttacksOfOpportunity(target);
}