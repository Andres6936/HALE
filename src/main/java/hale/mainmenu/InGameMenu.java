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

package hale.mainmenu;

import java.io.File;

import hale.Game;
import hale.util.Logger;
import hale.util.SaveGameUtil;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.Color;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.PopupWindow;
import de.matthiasmann.twl.Widget;

/**
 * The menu that is shown when the user presses escape or the menu button while
 * in game.  It contains options to save, load, and quit.
 *
 * @author Jared Stephen
 */

public class InGameMenu extends PopupWindow implements LoadGamePopup.Callback, SaveGamePopup.Callback
{
    private final Widget content;

    /**
     * Creates a new InGameMenu with the specified widget as parent
     *
     * @param parent the parent Widget
     */

    public InGameMenu(Widget parent)
    {
        super(parent);
        this.setCloseOnClickedOutside(false);
        this.setCloseOnEscape(true);

        content = new Content();
        this.add(content);
    }

    @Override
    public void loadGameAccepted(String saveGame)
    {
        Game.mainViewer.setLoadGame(saveGame);
        Game.mainViewer.setVisible(false);
        Game.mainViewer.exitToMainMenu();
    }

    @Override
    public void saveGameAccepted(String saveGame)
    {
        File fout = SaveGameUtil.getSaveFile(saveGame);

        try {
            SaveGameUtil.saveGame(fout);
        } catch (Exception e) {
            Logger.appendToErrorLog("Error saving game to file " + fout.getName(), e);
            Game.mainViewer.addMessage("link", "Error saving game!");
            Game.mainViewer.addFixedFadeAway("Error saving game!", 10, 10, Color.RED);
            fout.delete();
            return;
        }

        Game.mainViewer.addMessage("link", "Game saved successfully.");
        Game.mainViewer.addFixedFadeAway("Game saved successfully.", 10, 10, Color.RED);
        Game.mainViewer.updateInterface();

        closePopup();
    }

    private class Content extends DialogLayout
    {
        private final Button back, save, load, options, quit, exit;

        private Content()
        {
            back = new Button();
            back.setTheme("backbutton");
            back.addCallback(new Runnable()
            {
                @Override
                public void run()
                {
                    InGameMenu.this.closePopup();
                }
            });
            back.setEnabled(!Game.curCampaign.party.isDefeated());

            save = new Button();
            save.setTheme("savebutton");
            save.addCallback(new Runnable()
            {
                @Override
                public void run()
                {
                    if (Game.isInTurnMode() || Game.curCampaign.party.isDefeated()) {
                        save.setEnabled(false);
                        return;
                    }

                    SaveGamePopup popup = new SaveGamePopup(InGameMenu.this);
                    popup.setCallback(InGameMenu.this);
                    popup.openPopupCentered();
                }
            });

            load = new Button();
            load.setTheme("loadbutton");
            load.addCallback(new Runnable()
            {
                @Override
                public void run()
                {
                    // show the load game popup with a warning about losing progress
                    LoadGamePopup popup = new LoadGamePopup(InGameMenu.this, false);
                    popup.setCallback(InGameMenu.this);
                    popup.openPopupCentered();
                }
            });
            load.setEnabled(SaveGameUtil.getSaveGames().size() > 0);

            options = new Button();
            options.setTheme("optionsbutton");
            options.addCallback(new Runnable()
            {
                @Override
                public void run()
                {
                    new InGameOptionsPopup(InGameMenu.this).openPopupCentered();
                }
            });

            quit = new Button();
            quit.setTheme("menubutton");
            quit.addCallback(new Runnable()
            {
                @Override
                public void run()
                {
                    new ConfirmQuitPopup(InGameMenu.this,
                            ConfirmQuitPopup.QuitMode.QuitToMenu).openPopupCentered();
                }
            });

            exit = new Button();
            exit.setTheme("exitbutton");
            exit.addCallback(new Runnable()
            {
                @Override
                public void run()
                {
                    new ConfirmQuitPopup(InGameMenu.this,
                            ConfirmQuitPopup.QuitMode.ExitGame).openPopupCentered();
                }
            });

            save.setEnabled(!Game.isInTurnMode() && !Game.curCampaign.party.isDefeated());

            setHorizontalGroup(createParallelGroup(back, save, load, options, quit, exit));
            setVerticalGroup(createSequentialGroup(back, save, load, options, quit, exit));
        }
    }
}
