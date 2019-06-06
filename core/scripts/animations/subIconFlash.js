/*
 * subIconFlash - causes a single equipped item / body part to flash white briefly then fade back to normal
 * 
 * This script assumes all the subIcons will be the same size and that subIcons/hair001 exists
 * 
 * After creating this, you need to add a frame with the appropriate sub icon and set the color and position
 * This can be done most easily with Creature.getIconRenderer().getIcon() and Entity.getSubIconScreenPosition()
 */

var anim = game.createAnimation("subIcons/hair001", 2.0);

anim.setSecondaryRed(1.0);
anim.setSecondaryGreen(1.0);
anim.setSecondaryBlue(1.0);

anim.setSecondaryRedSpeed(-0.5);
anim.setSecondaryBlueSpeed(-0.5);
anim.setSecondaryGreenSpeed(-0.5);

anim.setAlpha(0.9);
anim.setAlphaSpeed(-0.3);
anim.clearFrames();

anim;