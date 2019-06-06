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

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import net.sf.hale.util.SaveGameUtil;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.PopupWindow;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.ThemeInfo;
import de.matthiasmann.twl.ToggleButton;
import de.matthiasmann.twl.Widget;

/**
 * The abstract base class for save and load game popup
 *
 * @author Jared Stephen
 */

public abstract class AbstractSaveGamePopup extends PopupWindow
{
    private Selector selected;
    private Content content;

    private DateFormat format;

    /**
     * Creates a new AbstractSaveGamePopup with the specified parent Widget
     *
     * @param parent the parent Widget
     */

    public AbstractSaveGamePopup( Widget parent )
    {
        super( parent );

        format = new SimpleDateFormat( "H:mm:ss dd MMMMM yyyy" );

        this.setCloseOnClickedOutside( false );
        this.setCloseOnEscape( true );

        content = new Content( );
        add( content );
    }

    /**
     * Returns the standard date format to use for displaying save file modification dates
     *
     * @return the standard date format to use for displaying save file modification dates
     */

    public DateFormat getDateFormat( )
    {
        return format;
    }

    /**
     * Called when the user presses the accept button
     */

    protected abstract void selectionAccepted( Selector selector );

    /**
     * Returns a list of all selectors, in order, that should be shown in this
     * AbstractSaveGamePopup
     *
     * @return the list of all selectors to show
     */

    protected abstract List< Selector > getValidSelectors( );

    /**
     * Sets the currently active selector to the specified selector.
     * The previously active selector, if there is one, is deselected.
     * If null is passed, all selectors are set inactive
     *
     * @param selector the selector to set active
     */

    protected void setActiveSelector( Selector selector )
    {
        if ( this.selected != null )
        {
            this.selected.setActive( false );
        }

        this.selected = selector;

        if ( this.selected != null )
        { selector.setActive( true ); }

        this.content.accept.setEnabled( this.selected != null );
    }

    /**
     * Sets whether the delete button is enabled
     *
     * @param enabled whether the delete button is enabled
     */

    protected void setDeleteEnabled( boolean enabled )
    {
        this.content.delete.setEnabled( enabled );
    }

    private class Content extends Widget
    {
        private Label title;
        private Button cancel;
        private Button accept;
        private Button delete;
        private ScrollPane selectorPane;
        private DialogLayout selectorPaneContent;

        private int acceptCancelGap;

        private Content( )
        {
            title = new Label( );
            title.setTheme( "titlelabel" );
            add( title );

            cancel = new Button( );
            cancel.setTheme( "cancelbutton" );
            cancel.addCallback( new Runnable( )
            {
                @Override
                public void run( )
                {
                    AbstractSaveGamePopup.this.closePopup( );
                }
            } );
            add( cancel );

            accept = new Button( );
            accept.setTheme( "acceptbutton" );
            accept.addCallback( new Runnable( )
            {
                @Override
                public void run( )
                {
                    selectionAccepted( selected );
                }
            } );
            add( accept );

            delete = new Button( );
            delete.setTheme( "deletebutton" );
            delete.addCallback( new Runnable( )
            {
                @Override
                public void run( )
                {
                    ConfirmationPopup popup = new ConfirmationPopup( getParent( ) );
                    popup.setTitleText( "Delete save file " + selected.getSaveGame( ) + "?" );
                    popup.setWarningText( "This action is permanent and cannot be undone." );
                    popup.addCallback( new DeleteSelectorCallback( ) );
                    popup.openPopupCentered( );
                }
            } );
            add( delete );

            selectorPaneContent = new DialogLayout( );
            selectorPaneContent.setTheme( "content" );
            selectorPane = new ScrollPane( selectorPaneContent );
            selectorPane.setFixed( ScrollPane.Fixed.HORIZONTAL );
            selectorPane.setTheme( "selectorpane" );
            add( selectorPane );

            addSelectors( );
        }

        private void addSelectors( )
        {
            selectorPaneContent.removeAllChildren( );

            DialogLayout.Group mainV = selectorPaneContent.createSequentialGroup( );
            DialogLayout.Group mainH = selectorPaneContent.createParallelGroup( );

            for ( Selector selector : getValidSelectors( ) )
            {
                mainV.addWidget( selector );
                mainH.addWidget( selector );
            }

            selectorPaneContent.setHorizontalGroup( mainH );
            selectorPaneContent.setVerticalGroup( mainV );

            // accept is disabled until a selection is made
            accept.setEnabled( false );
            delete.setEnabled( false );
        }

        @Override
        protected void applyTheme( ThemeInfo themeInfo )
        {
            super.applyTheme( themeInfo );

            acceptCancelGap = themeInfo.getParameter( "acceptCancelGap", 0 );
        }

        @Override
        protected void layout( )
        {
            super.layout( );

            int centerX = getInnerX( ) + getWidth( ) / 2;

            title.setSize( title.getPreferredWidth( ), title.getPreferredHeight( ) );
            title.setPosition( centerX - title.getWidth( ) / 2, getInnerY( ) );

            cancel.setSize( cancel.getPreferredWidth( ), cancel.getPreferredHeight( ) );
            accept.setSize( accept.getPreferredWidth( ), accept.getPreferredHeight( ) );

            cancel.setPosition( centerX + acceptCancelGap, getInnerBottom( ) - cancel.getHeight( ) );

            accept.setPosition( centerX - acceptCancelGap - accept.getWidth( ),
                                getInnerBottom( ) - accept.getHeight( ) );

            delete.setSize( delete.getPreferredWidth( ), delete.getPreferredHeight( ) );
            delete.setPosition( getInnerX( ), getInnerBottom( ) - delete.getHeight( ) );

            int selectorBottom = Math.min( cancel.getY( ), accept.getY( ) );
            selectorBottom = Math.min( selectorBottom, delete.getY( ) );

            selectorPane.setSize( getInnerWidth( ), selectorBottom - title.getBottom( ) );
            selectorPane.setPosition( getInnerX( ), title.getBottom( ) );
        }
    }

    private class DeleteSelectorCallback implements Runnable
    {
        @Override
        public void run( )
        {
            String saveGame = selected.getSaveGame( );

            File f = SaveGameUtil.getSaveFile( saveGame );
            f.delete( );

            content.addSelectors( );
        }
    }

    /**
     * A callback for use with a selector
     *
     * @author Jared Stephen
     */

    public class SelectorCallback implements Runnable
    {
        private Selector selector;

        /**
         * Creates a new Selector Callback for the specified selector
         *
         * @param selector the selector to create this Callback for
         */

        public SelectorCallback( Selector selector )
        {
            this.selector = selector;
        }

        @Override
        public void run( )
        {
            setActiveSelector( selector );
            setDeleteEnabled( true );
        }
    }

    /**
     * The abstract base class for save game selectors
     *
     * @author Jared Stephen
     */

    public static abstract class Selector extends ToggleButton
    {
        @Override
        public int getPreferredWidth( )
        {
            return Short.MAX_VALUE;
        }

        /**
         * Should return false for a selector that loads a game that is not the current version
         *
         * @return false if and only if there is a version problem
         */

        public boolean checkVersionID( )
        {
            return true;
        }

        /**
         * returns the string representing the save game shown by this selector
         *
         * @return the string representing the save game shown by this selector
         */

        public abstract String getSaveGame( );
    }
}
