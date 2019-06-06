/*
 * Hale is highly moddable tactical RPG.
 * Copyright (C) 2012 Jared Stephen
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package net.sf.hale.view;

import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.renderer.AnimationState.StateKey;
import net.sf.hale.Game;
import net.sf.hale.widgets.IconViewer;

/**
 * This class is used by widgets that want to have drag and drop functionality
 *
 * @author Jared Stephen
 */

public class DragAndDropHandler
{
    public static final StateKey STATE_DRAG_HOVER = StateKey.get( "draghover" );

    private IconViewer viewer;

    private DragTarget dragTarget;

    private DropTarget dropTarget;

    /**
     * Creates a new handler dragging the specified drag target
     *
     * @param dragTarget the target being drapped
     */

    public DragAndDropHandler( DragTarget dragTarget )
    {
        this.dragTarget = dragTarget;

        viewer = new IconViewer( dragTarget.getDragIcon( ) );
        viewer.setTheme( "" );
        Game.mainViewer.add( viewer );
    }

    /**
     * Handles the specified event.  The drag and drop viewer widget is repositioned under
     * the mouse cursor.  If the user releases the mouse while over a drop target, the
     * drag and drop is applied
     *
     * @param evt the Event to handle
     * @return true if this DragAndDropHandler is still active, false if it has been
     * deactivated (by releasing the mouse button) and can be discarded
     */

    public boolean handleEvent( Event evt )
    {
        int x = evt.getMouseX( );
        int y = evt.getMouseY( );

        if ( Game.interfaceLocker.locked( ) ) return false;

        switch ( evt.getType( ) )
        {
            case MOUSE_DRAGGED:
                viewer.setPosition( x, y );

                handleDrag( x, y );
                return true;
            case MOUSE_BTNUP:
                handleDrop( );

                Game.mainViewer.removeChild( viewer );
                return false;
            default:
        }

        return true;
    }

    private void handleDrag( int x, int y )
    {
        Widget widget = Game.mainViewer.getWidgetAt( x, y );

        DropTarget newTarget;

        if ( ( widget instanceof DropTarget ) && ( widget != dragTarget ) )
        {
            newTarget = ( DropTarget ) widget;
        }
        else
        {
            newTarget = null;
        }

        if ( newTarget != dropTarget )
        {
            if ( dropTarget != null )
            { dropTarget.dragAndDropStopHover( dragTarget ); }

            if ( newTarget != null )
            { newTarget.dragAndDropStartHover( dragTarget ); }
        }

        dropTarget = newTarget;
    }

    private void handleDrop( )
    {
        if ( dropTarget == null ) return;

        dropTarget.dropDragTarget( dragTarget );
    }
}
