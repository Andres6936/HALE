/*
 * A continuous ring that surrounds a creature while it is active.
 *
 * You need to set the position and may want to specify different color
 * distributions.  The default is pure white with a negative alpha speed.
 *
 * You may also wish to change the circle bounds.  The default surround a single tile.
 */

var g1 = game.createParticleGenerator("Circle", "Continuous", "particles/circle17", 150.0);
g1.setCircleBounds(35.0, 40.0);
g1.setDurationInfinite();
g1.setDurationDistribution(game.getFixedDistribution(0.8));
g1.setVelocityDistribution(game.getUniformAngleDistribution(5.0, 8.0));
g1.setAlphaSpeedDistribution(game.getFixedDistribution(-0.6));

var color = game.getFixedDistribution(1.0);
g1.setAlphaDistribution(color);
g1.setRedDistribution(color);
g1.setGreenDistribution(color);
g1.setBlueDistribution(color);

g1;