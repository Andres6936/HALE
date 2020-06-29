function onUse(game, item, target) {
    var quality = item.getQuality().getModifier() + 100;
    var intBonus = 3 * quality / 100;
    
    game.addMessage("blue", target.getName() + " drinks a potion of intelligence.");

    var effect = target.createEffect();
    effect.setTitle("Potion of Intelligence");
    effect.setDuration(10 + quality / 100);
    effect.getBonuses().addBonus('Int', parseInt(intBonus));
    target.applyEffect(effect);

    target.inventory.remove(item);
    
    game.ai.provokeAttacksOfOpportunity(target);
}