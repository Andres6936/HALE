function onUse(game, item, target) {
    var quality = item.getQuality().getModifier() + 100;
    var dexBonus = 3 * quality / 100;
    
    game.addMessage("blue", target.getName() + " drinks a potion of dexterity.");

    var effect = target.createEffect();
    effect.setTitle("Potion of Dexterity");
    effect.setDuration(10 + quality / 100);
    effect.getBonuses().addBonus('Dex', parseInt(dexBonus));
    target.applyEffect(effect);

    target.inventory.remove(item);
    
    game.ai.provokeAttacksOfOpportunity(target);
}