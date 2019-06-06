/*
 * A solid ring rotating around a position
 * You need to set the position and duration
 */

var g1 = game.createParticleGenerator("Point", "Continuous", "particles/ring72", 8.0);
g1.setDrawParticlesInOpaque(true);
g1.setStopParticlesAtOpaque(false);
g1.setDurationDistribution(game.getFixedDistribution(1.0));
var color = game.getFixedDistribution(1.0);
g1.setRedDistribution(color);
g1.setGreenDistribution(color);
g1.setBlueDistribution(color);
g1.setAlphaDistribution(game.getFixedDistribution(0.2));
g1.setRotationSpeedDistribution(game.getUniformDistribution(100.0, 200.0));

g1;