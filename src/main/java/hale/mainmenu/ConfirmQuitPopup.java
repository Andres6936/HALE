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

package main.java.hale.mainmenu;

import main.java.hale.Game;
import de.matthiasmann.twl.Widget;

/**
 * A popup window that appears and asks the user whether they would
 * really like to quit or not.  Supports quiting to the main menu
 * or exiting the program entirely
 *
 * @author Jared Stephen
 */

public class ConfirmQuitPopup extends ConfirmationPopup implements Runnable
{
    /**
     * The two available quit modes representing the action this
     * window will take when confirmed
     *
     * @author Jared Stephen
     */

    public enum QuitMode
    {
        /**
         * Quit to the Main Menu
         */
        QuitToMenu,

        /**
         * Exit the game entirely
         */
        ExitGame;
    }

    ;

    private final QuitMode mode;

    /**
     * Creates a new ConfirmQuitPopup with the specified parent Widget and
     * performing the action specified by the specified QuitMode
     *
     * @param parent the parent Widget
     * @param mode   the mode representing the action that will be performed
     *               if the user confirms the quit selection
     */

    public ConfirmQuitPopup(Widget parent, QuitMode mode)
    {
        super(parent);

        this.mode = mode;
        addCallback(this);

        switch (mode) {
            case QuitToMenu:
                setTitleText("Are you sure you wish to quit?");
                break;
            case ExitGame:
                setTitleText("Are you sure you wish to exit?");
                break;
        }
    }

    @Override
    public void run()
    {
        switch (ConfirmQuitPopup.this.mode) {
            case QuitToMenu:
                Game.mainViewer.exitToMainMenu();
                break;
            case ExitGame:
                Game.mainViewer.exitGame();
                break;
        }
    }
}
