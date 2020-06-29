/*
 * iceCrystal.js - a blue-ish ice crystal forms
 *
 * You will need to set the position and possibly color for this
 * animation.  It is not intended to be looped.
 */

var anim = game.createAnimation("animations/icicle1", 0.1);
anim.addFrames("animations/icicle", 2, 4);
anim.setAlpha(1.0);
anim.setDuration(0.4);

anim;