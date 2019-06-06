/*
 * shieldFlash - a shield appears at the specified position and then quickly fades away
 *
 * After creating this, you will need to set the position and may want to set the color
 * and color velocity
 *
 */
 
var anim = game.createAnimation("particles/shield", 1.5);
anim.setAlpha(1.0);
anim.setAlphaSpeed(-0.5);
 
anim;