/*
 * flame - a small point flame that shows continuously at a specified location
 *
 * You will need to set the position of the particle generator.  To create effects
 * other than fire, you can also set the color and color velocity distributions.
 */

var generator = game.createParticleGenerator("Point", "Continuous", "particles/circle9", 30.0);
generator.setDurationInfinite();
generator.setVelocityDistribution(game.getFixedAngleDistribution(20.0, 35.0, -3.14159 / 2));
generator.setDurationDistribution(game.getUniformDistribution(0.8, 1.3));

generator.setAlphaDistribution(game.getUniformDistribution(0.5, 1.0));
generator.setAlphaSpeedDistribution(game.getGaussianDistribution(-1.0, 0.05));

var color = game.getFixedDistribution(1.0);

generator.setRedDistribution(color);
generator.setGreenDistribution(color);
generator.setBlueDistribution(color);

generator.setGreenSpeedDistribution(game.getGaussianDistribution(-1.0, 0.05));
generator.setBlueSpeedDistribution(game.getGaussianDistribution(-6.0, 0.05));

generator;