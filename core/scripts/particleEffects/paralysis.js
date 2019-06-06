/*
 * Paralysis - a thin circle of particles surrounding a tile
 * The position must be set.  You can optionally  set the color parameters and the linestart
 * and lineend
 */

var g1 = game.createParticleGenerator("Line", "Continuous", "particles/circle9", 30.0);
g1.setDurationInfinite();
g1.setLineStart(-16.0, 0.0);
g1.setLineEnd(16.0, 0.0);
g1.setVelocityDistribution(game.getUniformAngleDistribution(1.0, 2.0));
g1.setDurationDistribution(game.getUniformDistribution(0.8, 1.3));
g1.setAlphaSpeedDistribution(game.getFixedDistribution(-0.5));

var color100 = game.getFixedDistribution(1.0);
g1.setAlphaDistribution(color100);
g1.setRedDistribution(color100);
g1.setBlueDistribution(color100);

g1;