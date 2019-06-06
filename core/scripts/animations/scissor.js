/*
 * scissor.js - two columns of energy come in from the sides and meet in the center in a flash
 *
 * You will need to set the position and possibly color for this
 * animation.  It is not intended to be looped.
 */
 
var anim = game.createAnimation("animations/scissors01", 0.05);
anim.addFrames("animations/scissors", 2, 13, 2);
anim.setAlpha(1.0);
anim.setDuration(0.65);

anim;