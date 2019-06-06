/*
 * rune - one of several types of runes can be displayed around the target's feet
 * 
 * This script can work with frames "rune1-" or "rune2-"
 * 
 * After creating this, you need to add the frames for the appropriate runes and set the position.
 * You will most likely want to set a long duration as well.
 */

var anim = game.createAnimation("animations/rune1-1", 0.1);

anim.setAlpha(1.0);
anim.setDrawingMode("BelowEntities");
anim.clearFrames();

anim;