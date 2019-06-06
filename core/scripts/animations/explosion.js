/*
 * A High quality explosion animation
 *
 * You need to set the position, optionally the color
 * Default color is orange
 */

var anim = game.createAnimation("explosion/frame00", 0.08);
anim.addFrames("explosion/frame", 1, 35, 2);
anim.setAlpha(1.0);
anim.setDuration(2.96);

anim;