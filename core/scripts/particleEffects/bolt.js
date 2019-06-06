/*
 * A bolt - can be used for firebolt, icebolt, etc
 * 
 * You need to set the position, velocity, and duration
 * Using setVelocityAndDurationBasedOnSpeed is often the best approach
 * 
 * You can optionally set the Red, Green, or Blue color distributions to change the effect to fire, for example
 */

var generator = game.createParticleGenerator("Point", "Continuous", "particles/circle17", 100.0);
generator.setAlphaDistribution(game.getUniformDistribution(0.5, 1.0));

var color = game.getFixedDistribution(1.0);

generator.setRedDistribution(color);
generator.setGreenDistribution(color);
generator.setBlueDistribution(color);

generator.setVelocityDistribution(game.getUniformAngleDistribution(10.0, 30.0));
generator.setDurationDistribution(game.getUniformDistribution(0.8, 1.3));
generator.setAlphaSpeedDistribution(game.getGaussianDistribution(-1.0, 0.05));

generator;