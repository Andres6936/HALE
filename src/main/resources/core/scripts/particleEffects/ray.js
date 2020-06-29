/*
 * A ray that moves rapidly towards its target, creating a solid line of particles behind it
 * You need to set the position, velocity, and duration
 * Using setVelocityAndDurationBasedOnSpeed is often the best approach
 * 
 * You can optionally set the Red, Green, or Blue color distributions to change the effect to fire, for example
 */

var generator = game.createParticleGenerator("Point", "Continuous", "particles/circle17", 100.0);
generator.setAlphaDistribution(game.getFixedDistribution(1.0));

var color = game.getFixedDistribution(1.0);

generator.setRedDistribution(color);
generator.setGreenDistribution(color);
generator.setBlueDistribution(color);

generator;