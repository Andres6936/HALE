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

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.PopupWindow;
import de.matthiasmann.twl.ThemeInfo;
import de.matthiasmann.twl.Widget;

/**
 * A popup displaying the status of the current update task to the user
 *
 * @author Jared
 */

public class UpdatePopup extends PopupWindow
{
    private Updater updater;

    private String currentTaskUpdate;

    private Content content;

    /**
     * Creates a new UpdatePopup
     *
     * @param parent  the parent widget
     * @param updater the updater that is running the update task
     */

    public UpdatePopup( Widget parent, Updater updater )
    {
        super( parent );

        this.updater = updater;

        this.setCloseOnClickedOutside( false );
        this.setCloseOnEscape( false );

        content = new Content( );
        add( content );
    }

    @Override
    public void paint( GUI gui )
    {
        super.paint( gui );

        // check for updated current task text
        if ( currentTaskUpdate != null )
        {
            content.currentTask.setText( currentTaskUpdate );
            currentTaskUpdate = null;
        }
    }

    /**
     * Updates the current task text with the specified string.  This method is thread safe,
     * the text will be updated on the next GUI update
     *
     * @param description
     */

    public void updateCurrentTask( String description )
    {
        this.currentTaskUpdate = description;
    }

    private class Content extends Widget
    {
        private Label title;
        private Label currentTask;
        private Button cancel;

        private int titleGap;

        private Content( )
        {
            title = new Label( );
            title.setTheme( "titlelabel" );
            add( title );

            currentTask = new Label( );
            currentTask.setTheme( "currenttasklabel" );
            add( currentTask );

            cancel = new Button( );
            cancel.setTheme( "cancelbutton" );
            cancel.addCallback( new Runnable( )
            {
                @Override
                public void run( )
                {
                    updater.cancel( );
                    closePopup( );
                }
            } );
            add( cancel );
        }

        @Override
        protected void applyTheme( ThemeInfo themeInfo )
        {
            super.applyTheme( themeInfo );

            titleGap = themeInfo.getParameter( "titlegap", 0 );
        }

        @Override
        protected void layout( )
        {
            title.setSize( title.getPreferredWidth( ), title.getPreferredHeight( ) );
            currentTask.setSize( currentTask.getPreferredWidth( ), currentTask.getPreferredHeight( ) );
            cancel.setSize( cancel.getPreferredWidth( ), cancel.getPreferredHeight( ) );

            title.setPosition( getInnerX( ) + getInnerWidth( ) / 2 - title.getWidth( ) / 2, getInnerY( ) );
            currentTask.setPosition( getInnerX( ) + getInnerWidth( ) / 2 - currentTask.getWidth( ) / 2,
                                     title.getBottom( ) + titleGap );

            cancel.setPosition( getInnerRight( ) - cancel.getWidth( ), getInnerBottom( ) - cancel.getHeight( ) );
        }
    }
}
