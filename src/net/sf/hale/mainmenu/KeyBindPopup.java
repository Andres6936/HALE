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

package net.sf.hale.mainmenu;

import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.PopupWindow;
import de.matthiasmann.twl.Widget;

public class KeyBindPopup extends PopupWindow
{
    private final Callback callback;
    private final Content content;

    /**
     * Creates a new KeyBindPopup
     */

    public KeyBindPopup( Widget parent, Callback callback )
    {
        super( parent );

        setCloseOnEscape( false );
        setCloseOnClickedOutside( true );

        this.callback = callback;

        content = new Content( );
        add( content );
    }

    @Override
    public int getPreferredInnerWidth( )
    {
        return content.getPreferredWidth( );
    }

    @Override
    public int getPreferredInnerHeight( )
    {
        return content.getPreferredHeight( );
    }

    private class Content extends Widget
    {
        private Label title;
        private Label label;

        private Content( )
        {
            title = new Label( "Binding for " + callback.getActionName( ) );
            title.setTheme( "titlelabel" );
            add( title );

            label = new Label( );
            label.setTheme( "textlabel" );
            add( label );
        }

        @Override
        protected void layout( )
        {
            title.setSize( title.getPreferredWidth( ), title.getPreferredHeight( ) );
            title.setPosition( getInnerX( ) + getInnerWidth( ) / 2 - title.getWidth( ) / 2,
                               getInnerY( ) + getInnerHeight( ) / 4 - title.getHeight( ) );

            label.setSize( label.getPreferredWidth( ), label.getPreferredHeight( ) );
            label.setPosition( getInnerX( ) + getInnerWidth( ) / 2 - label.getWidth( ) / 2,
                               getInnerY( ) + getInnerHeight( ) / 2 - label.getHeight( ) );

            this.requestKeyboardFocus( );
        }

        @Override
        public int getPreferredInnerWidth( )
        {
            return Math.max( label.getPreferredWidth( ), title.getPreferredWidth( ) );
        }

        @Override
        public int getPreferredInnerHeight( )
        {
            return 2 * ( label.getPreferredHeight( ) + title.getPreferredHeight( ) );
        }

        @Override
        public boolean handleEvent( Event evt )
        {
            switch ( evt.getType( ) )
            {
                case KEY_PRESSED:
                    int key = evt.getKeyCode( );

                    callback.keyBound( key );
                    KeyBindPopup.this.closePopup( );
                    break;
                case MOUSE_ENTERED:
                    return true;
                default:
            }

            return super.handleEvent( evt );
        }
    }

    /**
     * An interface to be used as a callback when the user has selected a
     * key
     *
     * @author Jared
     */

    public interface Callback
    {
        /**
         * Called whenever a key is pressed to be bound
         *
         * @param keyCode
         */

        public void keyBound( int keyCode );

        /**
         * Returns the action name for the key that is currently being bound
         *
         * @return the action name
         */

        public String getActionName( );
    }
}
