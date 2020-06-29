/*
 * An explosion - used for a fireball or similar effects.
 * 
 * You need to set the velocity distribution - this will determine the blast radius
 * 
 * You can optionally set the color velocity distributions - red, green, blue to make fire, for example
 */

var generator = game.createParticleGenerator("Point", "Burst", "particles/circle17", 1000.0);

generator.setDuration(3.0);
generator.setStopParticlesAtOpaque(true);
generator.setDurationDistribution(game.getFixedDistribution(2.0));

var color = game.getFixedDistribution(1.0);

generator.setAlphaDistribution(game.getUniformDistribution(0.5, 1.0));
generator.setRedDistribution(color);
generator.setGreenDistribution(color);
generator.setBlueDistribution(color);

generator.setAlphaSpeedDistribution(game.getFixedDistribution(-0.5));

generator;