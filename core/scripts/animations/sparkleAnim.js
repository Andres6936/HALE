/*
 * sparkleAnim.js - a short particle animation with green highlights
 *
 * You will need to set the position and possibly color for this
 * animation.  It is not intended to be looped.
 */

var anim = game.createAnimation("animations/sparkle1", 0.15);
anim.addFrames("animations/sparkle", 2, 6);
anim.setAlpha(1.0);
anim.setDuration(0.9);

anim;
