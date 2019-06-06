/*
 * a lance - similar to a bolt, but leaves behind a trail of stationary particles
 */


var generator = game.createParticleGenerator("Rect", "Continuous", "particles/icicle", 20.0);

generator.setRectBounds(-10.0, 10.0, -10.0, 10.0);

var color = game.getFixedDistribution(1.0);

generator.setRedDistribution(color);
generator.setGreenDistribution(color);
generator.setBlueDistribution(color);

generator.setDurationDistribution(game.getUniformDistribution(1.5, 2.0));

generator.setAlphaDistribution(color);

generator;