/*
 * crossFlash - a cross appears at the specified position and then quickly fades away
 *
 * After creating this, you will need to set the position and generally will want to set
 * the color and color velocity
 *
 */
 
var anim = game.createAnimation("particles/cross29", 1.5);
anim.setVelocity(0.0, -25.0);
anim.setAlpha(1.0);
anim.setAlphaSpeed(-0.5);
 
anim;