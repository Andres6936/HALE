/*
 * iconFlash - causes an icon typically representing an entire creature to flash
 * and then shortly turn back to normal
 *
 * This script assumes creatures/human01 exists, and it must be used with a 72x72 creature icon.
 *
 * After creating this, you need to add a frame with the appropriate icon and set the position,
 * using Entity.getIcon() and Entity.getLocation().getCenteredScreenPoint()
 */

var anim = game.createAnimation("creatures/human01", 2.0);

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