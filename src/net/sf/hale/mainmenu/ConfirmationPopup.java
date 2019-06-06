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
 * A popup with a message and buttons to accept or cancel
 *
 * @author Jared Stephen
 */

public class ConfirmationPopup extends PopupWindow
{
    private Content content;
    private List< Runnable > callbacks;

    /**
     * Create a ConfirmationPopup with the specified parent Widget
     *
     * @param parent the parent Widget
     */

    public ConfirmationPopup( Widget parent )
    {
        super( parent );
        this.setCloseOnClickedOutside( false );

        content = new Content( );
        this.add( content );

        callbacks = new ArrayList< Runnable >( );
    }

    /**
     * Sets the warning message text for this confirmation
     *
     * @param text the warning message text
     */

    public void setWarningText( String text )
    {
        content.warning.setText( text );
    }

    /**
     * Sets the title message text to the specified string
     *
     * @param text the title string to display
     */

    public void setTitleText( String text )
    {
        content.title.setText( text );
    }

    /**
     * Adds the specified callback which is run when the user clicks the
     * accept button
     *
     * @param callback the callback to add
     */

    public void addCallback( Runnable callback )
    {
        callbacks.add( callback );
    }

    private class Content extends Widget
    {
        private Label title;
        private Label warning;
        private Button yes, no;

        private int titleGap, acceptCancelGap;

        private Content( )
        {
            title = new Label( );
            title.setTheme( "titlelabel" );
            add( title );

            warning = new Label( );
            warning.setTheme( "warninglabel" );
            add( warning );

            yes = new Button( "Yes" );
            yes.setTheme( "acceptbutton" );
            yes.addCallback( new Runnable( )
            {
                @Override
                public void run( )
                {
                    setVisible( false );
                    for ( Runnable callback : callbacks )
                    {
                        callback.run( );
                    }
                    closePopup( );
                }
            } );
            add( yes );

            no = new Button( "No" );
            no.setTheme( "cancelbutton" );
            no.addCallback( new Runnable( )
            {
                @Override
                public void run( )
                {
                    closePopup( );
                }
            } );
            add( no );
        }

        @Override
        public int getPreferredWidth( )
        {
            int yesNoWidth = no.getPreferredWidth( ) + yes.getPreferredWidth( ) + 2 * acceptCancelGap;
            int labelWidth = Math.max( title.getPreferredWidth( ), warning.getPreferredWidth( ) );

            return Math.max( yesNoWidth, labelWidth ) + getBorderHorizontal( );
        }

        @Override
        public int getPreferredHeight( )
        {
            int height = title.getPreferredHeight( ) + titleGap;
            height += Math.max( yes.getPreferredHeight( ), no.getPreferredHeight( ) );

            if ( warning.getPreferredHeight( ) != 0 )
            {
                height += warning.getPreferredHeight( ) + titleGap;
            }

            return height + getBorderVertical( );
        }

        @Override
        protected void applyTheme( ThemeInfo themeInfo )
        {
            super.applyTheme( themeInfo );

            titleGap = themeInfo.getParameter( "titleGap", 0 );
            acceptCancelGap = themeInfo.getParameter( "acceptCancelGap", 0 );
        }

        @Override
        protected void layout( )
        {
            super.layout( );

            title.setSize( title.getPreferredWidth( ), title.getPreferredHeight( ) );
            warning.setSize( warning.getPreferredWidth( ), warning.getPreferredHeight( ) );

            int centerX = getInnerX( ) + getInnerWidth( ) / 2;

            title.setPosition( centerX - title.getWidth( ) / 2, getInnerY( ) );

            warning.setPosition( centerX - warning.getWidth( ) / 2, title.getBottom( ) + titleGap );

            yes.setSize( yes.getPreferredWidth( ), yes.getPreferredHeight( ) );
            no.setSize( no.getPreferredWidth( ), no.getPreferredHeight( ) );

            int buttonY = title.getBottom( ) + titleGap;
            if ( warning.getPreferredHeight( ) != 0 )
            {
                buttonY = warning.getBottom( ) + titleGap;
            }

            no.setPosition( centerX + acceptCancelGap, buttonY );
            yes.setPosition( centerX - acceptCancelGap - yes.getWidth( ), buttonY );
        }
    }
}
