function onUse(game, item, target) {
    var quality = item.getQuality().getModifier() + 100;
    var conBonus = 3 * quality / 100;
    
    game.addMessage("blue", target.getName() + " drinks a potion of constitution.");

    var effect = target.createEffect();
    effect.setTitle("Potion of Constitution");
    effect.setDuration(10 + quality / 100);
    effect.getBonuses().addBonus('Con', parseInt(conBonus));
    target.applyEffect(effect);

    target.inventory.remove(item);
    
    game.ai.provokeAttacksOfOpportunity(target);
}