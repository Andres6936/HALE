package net.sf.hale;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.input.Input;

/**
 * Class overridding TWL LWJGL Input, in order to support scaling
 *
 * @author Jared
 */

public class HaleLWJGLInput implements Input
{
    private boolean wasActive;

    public boolean pollInput( GUI gui )
    {
        boolean active = Display.isActive( );
        if ( wasActive && ! active )
        {
            wasActive = false;
            return false;
        }
        wasActive = active;

        if ( Keyboard.isCreated( ) )
        {
            while ( Keyboard.next( ) )
            {
                gui.handleKey(
                        Keyboard.getEventKey( ),
                        Keyboard.getEventCharacter( ),
                        Keyboard.getEventKeyState( ) );
            }
        }
        if ( Mouse.isCreated( ) )
        {
            while ( Mouse.next( ) )
            {
                gui.handleMouse(
                        Mouse.getEventX( ) / Game.config.getScaleFactor( ),
                        gui.getHeight( ) - Mouse.getEventY( ) / Game.config.getScaleFactor( ) - 1,
                        Mouse.getEventButton( ), Mouse.getEventButtonState( ) );

                int wheelDelta = Mouse.getEventDWheel( );
                if ( wheelDelta != 0 )
                {
                    gui.handleMouseWheel( wheelDelta / 120 );
                }
            }
        }
        return true;
    }

}
