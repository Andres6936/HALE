function onUse(game, item, target) {
    var creatures = game.ai.getTouchableCreatures(target, "Friendly");

    game.hideOpenWindows();
    
    var targeter = game.createListTargeter(target, item.getTemplate().getScript());
    targeter.addAllowedCreatures(creatures);
    targeter.setMenuTitle(item.getName());
    targeter.addCallbackArgument(item);
    
    targeter.activate();
}

function onTargetSelect(game, targeter, item) {
    var parent = targeter.getParent();
    var target = targeter.getSelectedCreature();
    
    var hp = game.dice().rand(1, 5);
    
    target.healDamage(hp);
    parent.inventory.remove(item);
    
    game.ai.provokeAttacksOfOpportunity(parent);
}
