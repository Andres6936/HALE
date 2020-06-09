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

/**
 * The class is used by the main menu and in game menu to indicate a status
 * for the main game loop.  Each status indicates a different action that should
 * be taken.
 *
 * @author Jared Stephen
 */

public class MainMenuAction
{
    private Runnable preActionCallback;

    private final Action action;
    private String loadGameFile;

    // an error popup message that should be shown when this action is taken
    private final List<String> errorPopupMessages;

    /**
     * The type of action that should be taken by the main game loop
     *
     * @author Jared Stephen
     */

    public enum Action
    {
        /**
         * Exit the game entirely
         */
        Exit,

        /**
         * Recreate the display mode from scratch and then re-show the main menu
         */
        Restart,

        /**
         * Shows the main menu without recreating the display mode
         */
        ShowMainMenu,

        /**
         * Starts a new game from scratch using the currently selected game and party
         */
        NewGame,

        /**
         * Loads a game within the currently selected campaign
         */
        LoadGame;
    }

    /**
     * Creates a MainMenuAction of the specified type.  This type can be one of
     * Action.Exit, Action.Restart, Action.LaunchEditor, or Action.NewGame.  For Action.LoadGame,
     * you must use the separate constructor.
     *
     * @param action the action to take
     */

    public MainMenuAction( Action action )
    {
        this.errorPopupMessages = new ArrayList<>();
        this.action = action;

        if ( action == Action.LoadGame )
        { throw new IllegalArgumentException( "Use MainMenuAction(String) instead." ); }
    }

    /**
     * Creates a MainMenuAction indicating that the specified load game file should be loaded.
     * The action type will be Action.LoadGame;
     *
     * @param loadGameFile the save game file to load
     */

    public MainMenuAction( String loadGameFile )
    {
        this.errorPopupMessages = new ArrayList< String >( );
        this.action = Action.LoadGame;
        this.loadGameFile = loadGameFile;
    }

    /**
     * Runs the pre action callback, if one exists.  otherwise, does nothing
     */

    public void runPreActionCallback( )
    {
        if ( preActionCallback != null ) preActionCallback.run( );
    }

    /**
     * Sets the callback that is run before the main menu action is taken
     *
     * @param callback
     */

    public void setPreActionCallback( Runnable callback )
    {
        this.preActionCallback = callback;
    }

    /**
     * Adds the error message that will be shown with this main menu action.  See {@link #getErrorPopupMessages()}
     * The specified string is appended to the list of error popup messages
     *
     * @param message the message to show
     */

    public void addErrorPopupMessage( String message )
    {
        this.errorPopupMessages.add( message );
    }

    /**
     * Returns the String text of the error message to be displayed when this MainMenuAction is performed,
     * or an empty list if no messages are displayed.  This String is only used with the ShowMainMenu action.
     * One String is shown per line in the error popup
     *
     * @return the error text to display in a popup
     */

    public List< String > getErrorPopupMessages( )
    {
        return errorPopupMessages;
    }

    /**
     * Returns the action specified in the creation of this Object
     *
     * @return the action for this Object
     */

    public Action getAction( )
    {
        return action;
    }

    /**
     * If the Action is Action.LoadGame, returns the load game file that
     * should be loaded.
     *
     * @return the load game file to load
     * @throws UnsupportedOperationException if the mode is not Action.LoadGame
     */

    public String getLoadGameFile( )
    {
        if ( action != Action.LoadGame )
        { throw new UnsupportedOperationException( "Only valid for load game action type." ); }

        return loadGameFile;
    }
}
