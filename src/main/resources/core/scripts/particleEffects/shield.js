/*
 * A semi-transparent shield appears and hovers over the target
 * You need to set the position and duration
 */

var g1 = game.createParticleGenerator("Point", "Continuous", "particles/shield", 1.0);

g1.setDurationDistribution(game.getFixedDistribution(1.0));

var color = game.getFixedDistribution(1.0);

g1.setRedDistribution(color);
g1.setGreenDistribution(color);
g1.setBlueDistribution(color);
g1.setAlphaDistribution(game.getFixedDistribution(0.3));
g1.setAlphaSpeedDistribution(game.getFixedDistribution(-0.2));

g1;