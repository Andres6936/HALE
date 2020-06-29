function onUse(game, item, target) {
    var quality = item.getQuality().getModifier() + 100;
    var wisBonus = 3 * quality / 100;
    
    game.addMessage("blue", target.getName() + " drinks a potion of wisdom.");

    var effect = target.createEffect();
    effect.setTitle("Potion of Wisdom");
    effect.setDuration(10 + quality / 100);
    effect.getBonuses().addBonus('Wis', parseInt(wisBonus));
    target.applyEffect(effect);

    target.inventory.remove(item);
    
    game.ai.provokeAttacksOfOpportunity(target);
}