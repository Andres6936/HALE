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

import de.matthiasmann.twl.Widget;

/**
 * The popup window showing the list of loadable saved games
 * for the current campaign
 *
 * @author Jared Stephen
 */

public class LoadGamePopup extends AbstractSaveGamePopup implements Runnable
{
    private Callback callback;
    private boolean showLoadWarning;
    private String saveGame;

    /**
     * Creates a new LoadGamePopup with the specified parent Widget
     *
     * @param parent          the parent Widget
     * @param showLoadWarning whether to show a warning about losing unsaved
     *                        progress when loading a game
     */

    public LoadGamePopup( Widget parent, boolean showLoadWarning )
    {
        super( parent );

        this.showLoadWarning = showLoadWarning;
    }

    // the callback for the load game without saving confirmation
    @Override
    public void run( )
    {
        callback.loadGameAccepted( saveGame );
        closePopup( );
    }

    /**
     * Sets the callback that is called whenever the user accepts a selection
     * from this popup window
     *
     * @param callback the callback to be run
     */

    public void setCallback( Callback callback )
    {
        this.callback = callback;
    }

    @Override
    protected void selectionAccepted( Selector selector )
    {
        saveGame = selector.getSaveGame( );

        if ( ! showLoadWarning || ! selector.checkVersionID( ) )
        {
            ConfirmationPopup popup = new ConfirmationPopup( getParent( ) );

            if ( ! showLoadWarning )
            {
                popup.setTitleText( "Load " + saveGame + "? You will lose any unsaved progress." );
            }
            else
            {
                popup.setTitleText( "Load " + saveGame + "?" );
            }

            if ( ! selector.checkVersionID( ) )
            {
                popup.setWarningText( "Warning!  This save file is from an earlier version." );
            }

            popup.addCallback( LoadGamePopup.this );
            popup.openPopupCentered( );
        }
        else
        {
            callback.loadGameAccepted( saveGame );
            closePopup( );
        }
    }

    @Override
    protected List< Selector > getValidSelectors( )
    {
        List< Selector > selectors = new ArrayList< Selector >( );

        for ( String saveGame : SaveGameUtil.getSaveGames( ) )
        {
            SaveGameSelector selector = new SaveGameSelector( saveGame, getDateFormat( ) );
            selector.addCallback( new AbstractSaveGamePopup.SelectorCallback( selector ) );
            selectors.add( selector );
        }

        return selectors;
    }

    /**
     * The interface that must be implemented by any classes wishing to
     * be used as a callback for a LoadGamePopup.
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

        public void loadGameAccepted( String saveGame );
    }
}
