/*
 * Hale is highly moddable tactical RPG.
 * Copyright (C) 2011 Jared Stephen
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

import java.util.ArrayList;
import java.util.List;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.PopupWindow;
import de.matthiasmann.twl.ThemeInfo;
import de.matthiasmann.twl.Widget;

/**
 * A popup showing a list of messages with a button to close the popup
 *
 * @author Jared
 */

public class ErrorPopup extends PopupWindow
{
    private Content content;
    private List< String > messages;
    private Runnable callback;

    /**
     * Create a new ErrorPopup with the specified parent widget
     *
     * @param parent   the parent (owner) Widget
     * @param messages the list of messages to display in this popup
     */

    public ErrorPopup( Widget parent, List< String > messages )
    {
        super( parent );
        this.setCloseOnClickedOutside( false );
        this.setCloseOnEscape( false );

        this.messages = new ArrayList< String >( );
        this.messages.addAll( messages );

        content = new Content( );
        this.add( content );
    }

    /**
     * Sets a callback that is run when this popup is closed by the user clicking "ok"
     *
     * @param callback
     */

    public void setCallback( Runnable callback )
    {
        this.callback = callback;
    }

    private class Content extends Widget
    {
        private int gap;

        private Content( )
        {
            for ( String message : messages )
            {
                Label label = new Label( message );
                label.setTheme( "messagelabel" );
                add( label );
            }

            Button accept = new Button( );
            accept.setTheme( "acceptbutton" );
            accept.addCallback( new Runnable( )
            {
                @Override
                public void run( )
                {
                    if ( callback != null ) callback.run( );

                    closePopup( );
                }
            } );
            add( accept );
        }

        @Override
        public int getPreferredHeight( )
        {
            int height = getBorderVertical( );
            for ( int i = 0; i < getNumChildren( ); i++ )
            {
                Widget child = getChild( i );

                height += child.getPreferredHeight( ) + gap;
            }

            // gap was counted one extra time
            height -= gap;

            return height;
        }

        @Override
        public int getPreferredWidth( )
        {
            int width = 0;

            for ( int i = 0; i < getNumChildren( ); i++ )
            {
                Widget child = getChild( i );

                width = Math.max( width, child.getPreferredWidth( ) );
            }

            return width + getBorderHorizontal( );
        }

        @Override
        protected void layout( )
        {
            int centerX = getInnerX( ) + getInnerWidth( ) / 2;
            int curY = getInnerY( );

            for ( int i = 0; i < getNumChildren( ); i++ )
            {
                Widget child = getChild( i );

                child.setSize( child.getPreferredWidth( ), child.getPreferredHeight( ) );
                child.setPosition( centerX - child.getWidth( ) / 2, curY );

                curY = child.getBottom( ) + gap;
            }
        }

        @Override
        protected void applyTheme( ThemeInfo themeInfo )
        {
            super.applyTheme( themeInfo );

            gap = themeInfo.getParameter( "gap", 0 );
        }
    }
}
