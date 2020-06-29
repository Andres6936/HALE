function onUse(game, item, target) {
    var quality = item.getQuality().getModifier() + 100;
    var chaBonus = 3 * quality / 100;
    
    game.addMessage("blue", target.getName() + " drinks a potion of charisma.");

    var effect = target.createEffect();
    effect.setTitle("Potion of Charisma");
    effect.setDuration(10 + quality / 100);
    effect.getBonuses().addBonus('Cha', parseInt(chaBonus));
    target.applyEffect(effect);

    target.inventory.remove(item);
    
    game.ai.provokeAttacksOfOpportunity(target);
}