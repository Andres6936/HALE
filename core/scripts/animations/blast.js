/*
 * blast.js - a generic explosion with a blue tinge
 *
 * You will need to set the position and possibly color for this
 * animation.  It is not intended to be looped.
 */

var anim = game.createAnimation("animations/blast1", 0.075);
anim.addFrames("animations/blast", 2, 8);
anim.setAlpha(1.0);
anim.setDuration(0.6);

anim;