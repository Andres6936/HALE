/*
 * inwardBurst - a burst of particles coming from all directions towards
 * a center point.  You need to set the position and may want to set the color
 *
 * You will need to set the velocity distribution towards the center position
 * using VelocityTowardsPointDistribution
 */

var generator = game.createParticleGenerator("Circle", "Burst", "particles/circle9", 100);
generator.setCircleBounds(30.0, 45.0);
generator.setDuration(0.5);
generator.setDurationDistribution(game.getFixedDistribution(0.5));

var color = game.getFixedDistribution(1.0);
generator.setAlphaDistribution(color);
generator.setRedDistribution(color);
generator.setGreenDistribution(color);
generator.setBlueDistribution(color);

generator.setAlphaSpeedDistribution(game.getFixedDistribution(-0.5));

generator;