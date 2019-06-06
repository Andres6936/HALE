/*
 * Fog - designed for use as a fog or similar effect over an Area
 * 
 * You need to set the position and duration
 * You can optionally set the color and color velocity
 */
 
var generator = game.createParticleGenerator("Circle", "Continuous", "particles/circle33", 30.0);
generator.setCircleBounds(0.0, 36.0);
//generator.setRectBounds(-30.0, 30.0, -30.0, 30.0);

generator.setVelocityDistribution(game.getUniformAngleDistribution(20.0, 40.0));

generator.setDurationDistribution(game.getFixedDistribution(2.0));

var color = game.getFixedDistribution(0.5);

generator.setRedDistribution(color);
generator.setGreenDistribution(color);
generator.setBlueDistribution(color);
generator.setAlphaDistribution(game.getFixedDistribution(0.4));

generator.setAlphaSpeedDistribution(game.getFixedDistribution(-0.2));

generator;