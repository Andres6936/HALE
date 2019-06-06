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

import net.sf.hale.util.SaveGameUtil;

import de.matthiasmann.twl.EditField;
import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ThemeInfo;
import de.matthiasmann.twl.Widget;

/**
 * The popup window showing the list of save games that can be overwritten
 * with a new save and allows creation of a new save game
 *
 * @author Jared Stephen
 */

public class SaveGamePopup extends AbstractSaveGamePopup implements Runnable
{
    private List< String > currentSaves;
    private Selector newSaveSelector;

    private Callback callback;

    private String selectedSave;

    /**
     * Creates a new SaveGamePopup with the specified parent Widget
     *
     * @param parent the parent Widget
     */

    public SaveGamePopup( Widget parent )
    {
        super( parent );
    }

    /**
     * Sets the callback that will be called when the user accepts a selection
     *
     * @param callback the callback
     */

    public void setCallback( Callback callback )
    {
        this.callback = callback;
    }

    // the overwrite save game confirmation popup callback
    @Override
    public void run( )
    {
        callback.saveGameAccepted( selectedSave );
        closePopup( );
    }

    @Override
    protected void selectionAccepted( Selector selector )
    {
        selectedSave = selector.getSaveGame( );

        if ( selector != newSaveSelector )
        {
            ConfirmationPopup popup = new ConfirmationPopup( getParent( ) );
            popup.setTitleText( "Overwrite save file " + selectedSave + "?" );
            popup.addCallback( SaveGamePopup.this );
            popup.openPopupCentered( );
        }
        else
        {
            callback.saveGameAccepted( selectedSave );
            closePopup( );
        }
    }

    @Override
    protected List< Selector > getValidSelectors( )
    {
        currentSaves = new ArrayList< String >( );

        List< Selector > selectors = new ArrayList< Selector >( );

        newSaveSelector = new NewSaveSelector( );
        selectors.add( newSaveSelector );

        for ( String saveGame : SaveGameUtil.getSaveGames( ) )
        {
            // do not show quicksaves
            if ( SaveGameUtil.isQuickSave( saveGame ) ) continue;

            SaveGameSelector selector = new SaveGameSelector( saveGame, getDateFormat( ) );
            selector.addCallback( new AbstractSaveGamePopup.SelectorCallback( selector ) );
            selectors.add( selector );

            currentSaves.add( saveGame );
        }

        return selectors;
    }

    /**
     * The interface that must be implemented by any classes wishing to
     * be used as a callback for a SaveGamePopup.
     *
     * @author Jared Stephen
     */

    public interface Callback
    {
        /**
         * Called whenever the user has made a selection and presses the
         * accept button
         *
         * @param saveGame the saveGame String ID that the user has selected
         */
        public void saveGameAccepted( String saveGame );
    }

    private class NewSaveSelector extends AbstractSaveGamePopup.Selector implements Runnable
    {
        private Label name;
        private EditField editField;
        private Label error;

        private int editFieldGap;
        private int topRowCenter;

        private NewSaveSelector( )
        {
            addCallback( this );

            this.name = new Label( );
            name.setTheme( "namelabel" );
            add( name );

            editField = new EditField( );
            editField.setTheme( "nameeditfield" );
            editField.addCallback( new EditField.Callback( )
            {
                @Override
                public void callback( int key )
                {
                    switch ( key )
                    {
                        case Event.KEY_RETURN:
                            if ( editFieldHasValidSaveName( ) )
                            { SaveGamePopup.this.selectionAccepted( NewSaveSelector.this ); }
                            break;
                        default:
                            if ( editFieldHasValidSaveName( ) )
                            {
                                setActiveSelector( NewSaveSelector.this );
                                setDeleteEnabled( false );
                            }
                            else
                            {
                                setActiveSelector( null );
                                setDeleteEnabled( false );
                            }
                    }
                }
            } );
            add( editField );

            error = new Label( );
            error.setTheme( "errorlabel" );
            add( error );
        }

        private boolean editFieldHasValidSaveName( )
        {
            if ( editField.getTextLength( ) == 0 )
            {
                error.setText( "" );
                return false;
            }

            if ( editField.getTextLength( ) > 12 )
            {
                error.setText( "You must use at most 12 characters." );
                return false;
            }

            String text = editField.getText( );
            for ( String save : currentSaves )
            {
                if ( save.equals( text ) )
                {
                    error.setText( "A save of that name already exists." );
                    return false;
                }
            }

            if ( SaveGameUtil.isQuickSave( text ) )
            {
                error.setText( "Invalid save name." );
                return false;
            }

            // determine if the string is a valid filename
            // this is much more restrictive than valid filenames,
            // but any string passing this text should be fine
            for ( char c : text.toCharArray( ) )
            {
                if ( Character.isDigit( c ) ) continue;
                if ( Character.isLetter( c ) ) continue;

                if ( c == ' ' || c == '-' || c == '_' || c == '.' ) continue;

                error.setText( "Invalid save name." );
                return false;
            }

            error.setText( "" );
            return true;
        }

        @Override
        public void run( )
        {
            // don't allow the widget to be set active by clicking
            setActiveSelector( null );
            setDeleteEnabled( false );
            setActive( false );
        }

        @Override
        protected boolean handleEvent( Event evt )
        {
            // hack to get edit field to take keyboard events, no idea
            // why it isn't working the normal way
            switch ( evt.getType( ) )
            {
                case KEY_PRESSED:
                case KEY_RELEASED:
                    return editField.handleEvent( evt );
                default:
            }

            return super.handleEvent( evt );
        }

        @Override
        protected void applyTheme( ThemeInfo themeInfo )
        {
            super.applyTheme( themeInfo );

            editFieldGap = themeInfo.getParameter( "editFieldGap", 0 );
            topRowCenter = themeInfo.getParameter( "topRowCenter", 0 );
        }

        @Override
        protected void layout( )
        {
            super.layout( );

            int centerY = getInnerY( ) + topRowCenter;

            name.setPosition( getInnerX( ), centerY );

            editField.setSize( editField.getPreferredWidth( ), editField.getPreferredHeight( ) );
            editField.setPosition( getInnerX( ) + name.getPreferredWidth( ) + editFieldGap,
                                   centerY - editField.getHeight( ) / 2 );

            error.setPosition( editField.getX( ), editField.getBottom( ) + error.getPreferredHeight( ) / 2 );
        }

        @Override
        public String getSaveGame( )
        {
            return editField.getText( );
        }
    }
}
