/*
 * A ring with an outlined border
 * 
 * You need to set the velocity distribution - this will determine the blast radius
 * 
 * You can optionally set the color velocity distributions - red, green, blue to make fire, for example
 */

var generator = game.createParticleGenerator("Point", "Burst", "particles/circleborder", 500.0);

generator.setRotationDistribution(game.getFixedDistributionWithBase(game.getAngleDistributionBase(), 180.0 / 3.14159, 0.0));
generator.setDuration(3.0);
generator.setStopParticlesAtOpaque(true);
generator.setDurationDistribution(game.getFixedDistribution(2.0));

var color = game.getFixedDistribution(1.0);

generator.setAlphaDistribution(game.getUniformDistribution(0.5, 1.0));
generator.setRedDistribution(color);
generator.setGreenDistribution(color);
generator.setBlueDistribution(color);

generator.setAlphaSpeedDistribution(game.getFixedDistribution(-0.8));

//Example for fireball
//ParticleGenerator g1 = Game.particleManager.get("bolt");
//g1.setVelocityDurationBasedOnSpeed(Game.selectedEntity.getLocation().toPoint(), gridPoint, 400.0f);
//g1.setGreenVelocityDistribution(new GaussianDistribution(-1.0f, 0.01f));
//g1.setBlueVelocityDistribution(new GaussianDistribution(-6.0f, 0.01f));
//
//ParticleGenerator g2 = Game.particleManager.get("explosion");
//g2.setVelocityDistribution(new EquallySpacedAngleDistribution(150.0f, 200.0f, 5.0f, 2000.0f, 10.0f));
//g2.setGreenVelocityDistribution(new UniformDistributionWithBase(new SpeedDistributionBase(), -0.005f, 0.0f, 0.05f));
//g2.setBlueVelocityDistribution(new UniformDistributionWithBase(new SpeedDistributionBase(), -0.01f, 0.0f, 0.05f));
//g1.addSubGeneratorAtEnd(g2);
//
//ParticleGenerator g3 = Game.particleManager.get("explosion");
//g3.setVelocityDistribution(new EquallySpacedAngleDistribution(0.0f, 50.0f, 5.0f, 1000.0f, 10.0f));
//g3.setGreenVelocityDistribution(new UniformDistributionWithBase(new SpeedDistributionBase(), -0.005f, 0.0f, 0.05f));
//g3.setBlueVelocityDistribution(new UniformDistributionWithBase(new SpeedDistributionBase(), -0.01f, 0.0f, 0.05f));
//
//g1.addSubGeneratorAtEnd(g3);

generator;