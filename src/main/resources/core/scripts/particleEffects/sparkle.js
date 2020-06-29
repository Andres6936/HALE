/*
 * Sparkle - can be used for status effects, etc
 * 
 * You need to set the position and duration
 * 
 * You can optionally set the color velocity - red, green, blue
 */


var generator = game.createParticleGenerator("Rect", "Continuous", "particles/sparkle25", 5.0);
generator.setRectBounds(-22.0, 22.0, -30.0, -15.0);
//generator.setLineStart(-20.0, -20.0);
//generator.setLineEnd(20.0, -20.0);

generator.setVelocityDistribution(game.getFixedAngleDistribution(5.0, 35.0, 3.14159 / 2.0));

generator.setDurationDistribution(game.getUniformDistribution(0.8, 1.3));

var color = game.getFixedDistribution(1.0);

generator.setRedDistribution(color);
generator.setGreenDistribution(color);
generator.setBlueDistribution(color);
generator.setAlphaDistribution(game.getUniformDistribution(0.8, 1.0));

generator.setAlphaSpeedDistribution(game.getGaussianDistribution(-0.3, 0.05));

generator;