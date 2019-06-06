/*
 * A short duration cone shaped spray.
 *
 * You need to set the velocity distribution - use UniformArcDistribution
 * You also need to set the position
 * You can optionally set the color velocity distributions - red, green, blue to make fire, for example
 */

var generator = game.createParticleGenerator("Point", "Continuous", "particles/circle17", 2000.0);
generator.setDuration(0.5);

generator.setStopParticlesAtOpaque(true);
generator.setDurationDistribution(game.getFixedDistribution(2.0));

var color = game.getFixedDistribution(1.0);

generator.setAlphaDistribution(game.getUniformDistribution(0.5, 1.0));
generator.setRedDistribution(color);
generator.setGreenDistribution(color);
generator.setBlueDistribution(color);

generator.setAlphaSpeedDistribution(game.getFixedDistribution(-1.0));

generator;