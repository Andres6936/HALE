function canUse(game, trap, parent) {
    return trap.canAttemptPlace(parent);
}

function onUse(game, trap, parent) {
    trap.attemptPlace(parent);
}

function onSpringTrap(game, trap, target) {
    
}

function onTrapReflexFailed(game, trap, target) {
    
}
