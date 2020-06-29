/*
 * shieldLoop - a loopable shield animation
 *
 * You will want to set the position and possibly the color for this animation
 * You will also most likely want to set the duration as one loop is very short
 */

var anim = game.createAnimation("animations/shield1", 0.1);
anim.addFrames("animations/shield", 2, 4);
anim.setAlpha(1.0);

anim;