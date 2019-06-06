function enableSong(game, slot, song) {
	slot.getParent().put("lastBardSong", song);

	var aura = slot.createAura("effects/" + song);
	aura.setTitle(slot.getAbility().getName());
	aura.setRemoveOnDeactivate(true);
	aura.setAuraMaxRadius(8);
	
	aura.getBonuses().addPenalty("ActionPoint", -50);
	
	var generator = game.createParticleGenerator("Line", "Continuous", "particles/musicNote", 2.0);
	generator.setLineStart(-15.0, 0.0);
	generator.setLineEnd(15.0, 0.0);
	generator.setDurationInfinite();
	
	var position = slot.getParent().getLocation().getCenteredScreenPoint();
	generator.setPosition(position.x, position.y - 20.0);
	generator.setStopParticlesAtOpaque(false);
	generator.setDrawParticlesInOpaque(true);
	generator.setVelocityDistribution(game.getFixedAngleDistribution(10.0, 15.0, -3.14159 / 2));
	generator.setDurationDistribution(game.getUniformDistribution(1.5, 2.2));
	generator.setAlphaSpeedDistribution(game.getFixedDistribution(-0.5));
	generator.setAlphaDistribution(game.getFixedDistribution(1.0));
	generator.setRedDistribution(game.getFixedDistribution(1.0));
	generator.setGreenDistribution(game.getUniformDistribution(0.8, 1.0));
	generator.setBlueDistribution(game.getFixedDistribution(0.0));
	aura.addAnimation(generator);
	
	slot.getParent().applyEffect(aura);
	slot.activate();
}

function addButton(game, slot, cbFunc, name) {
	var cb = game.createButtonCallback(slot, "enableSong");
	cb.addArgument(cbFunc);
	game.addMenuButton(name, cb);
}

function onReactivate(game, slot) {
	slot.resetCooldown();
	var song = slot.getParent().get("lastBardSong");
	
	if (song != null) {
		enableSong(game, slot, song);
	}
}

function onActivate(game, slot) {
	var abilities = slot.getParent().abilities;

	if (!game.addMenuLevel("Bardsong")) return;
	
	if (abilities.has("SongOfLuck"))
		addButton(game, slot, "songOfLuck", "Song of Luck");
	
	if (abilities.has("SongOfSlaying"))
		addButton(game, slot, "songOfSlaying", "Song of Slaying");
	
	if (abilities.has("DefendersRhythm"))
		addButton(game, slot, "defendersRhythm", "Defender's Rhythm");
	
	if (abilities.has("HealingSong"))
		addButton(game, slot, "healingSong", "Healing Song");
		
	if (abilities.has("CurseSong"))
		addButton(game, slot, "curseSong", "Curse Song");
	
	if (abilities.has("MortalitySong"))
		addButton(game, slot, "mortalitySong", "Mortality Song");
	
	game.showMenu();
}

function onDeactivate(game, slot) {
   slot.deactivate();
}
