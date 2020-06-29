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

package hale;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hale.*;
import hale.mainmenu.InGameMenu;
import hale.quickbar.Quickbar;
import hale.util.Logger;
import hale.util.SaveGameUtil;
import hale.interfacelock.MovementHandler;
import de.matthiasmann.twl.Color;
import de.matthiasmann.twl.Widget;

/**
 * A class for managing a set of keybindings
 *
 * @author Jared
 */

public class Keybindings
{
    public static final String UseQuickbarSlot = "UseQuickbarSlot";

    private Map<Integer, Binding> bindingsByKey;

    /**
     * Creates a new Keybindings object with the default bindings.  This must be initialized
     * after the mainviewer.  Keybinding actions are mapped based on the values stored in the
     * current config
     */

    public Keybindings()
    {
        // create the list of all bindings
        List<Binding> bindings = new ArrayList<Binding>();

        bindings.add(new ToggleWindow(Game.mainViewer.messagesWindow, "MessagesWindow"));
        bindings.add(new ToggleWindow(Game.mainViewer.characterWindow, "CharacterWindow"));
        bindings.add(new ToggleWindow(Game.mainViewer.inventoryWindow, "InventoryWindow"));
        bindings.add(new ToggleWindow(Game.mainViewer.logWindow, "LogWindow"));
        bindings.add(new ToggleWindow(Game.mainViewer.miniMapWindow, "MiniMap"));
        bindings.add(new ToggleWindow(Game.mainViewer.scriptConsole, "ScriptConsole"));
        bindings.add(new CancelMovement());
        bindings.add(new ShowMenu());
        bindings.add(new EndTurn());
        bindings.add(new Quicksave());
        bindings.add(new ToggleMovementMode());

        for (int i = 0; i < Quickbar.ItemSlots; i++) {
            bindings.add(new UseQuickbarSlot(i));
        }

        // now find the keyboard key for each binding from the config

        bindingsByKey = new HashMap<Integer, Binding>();

        for (Binding binding : bindings) {
            int keyCode = Game.config.getKeyForAction(binding.getActionName());

            if (bindingsByKey.containsKey(keyCode)) {
                Logger.appendToWarningLog("Warning: duplicate key binding for " + bindingsByKey.get(keyCode).getActionName() +
                        ", " + binding.getActionName());
            }

            if (keyCode != -1) {
                bindingsByKey.put(keyCode, binding);
            }
        }
    }

    /**
     * Checks whether the specified key is the key associated with closing
     * the script console.  If it is, then toggles the script console
     *
     * @param key
     * @return true if the key is the key to close the script console
     */

    public boolean checkToggleScriptConsole(int key)
    {
        if (key == Game.config.getKeyForAction("ToggleScriptConsole")) {
            bindingsByKey.get(key).run();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Activates the event associated with the given key binding, or does nothing
     * if no binding exists with that key
     *
     * @param key
     */

    public void fireKeyEvent(int key)
    {
        if (bindingsByKey.containsKey(key)) {
            bindingsByKey.get(key).run();
        }
    }

    /**
     * A base class for a callback providing a key action name
     *
     * @author Jared
     */

    public static abstract class Binding implements Runnable
    {

        /**
         * Returns the descriptive action name for this binding, used in the
         * configuration file when defining key bindings
         *
         * @return the action name String
         */

        public String getActionName()
        {
            return getClass().getSimpleName();
        }
    }

    /**
     * A callback for toggling a window
     *
     * @author Jared
     */

    public static class ToggleWindow extends Binding
    {
        private final Widget window;
        private final String windowName;

        /**
         * Creates a new ToggleWindow callback for the specified window with the specified name
         *
         * @param window
         * @param windowName
         */

        public ToggleWindow(Widget window, String windowName)
        {
            this.window = window;
            this.windowName = windowName;
        }

        @Override
        public String getActionName()
        {
            return "Toggle" + windowName;
        }

        @Override
        public void run()
        {
            window.setVisible(!window.isVisible());
        }
    }

    /**
     * A callback for toggling the current movement mode
     *
     * @author Jared
     */

    public static class ToggleMovementMode extends Binding
    {
        @Override
        public void run()
        {
            if (Game.interfaceLocker.getMovementMode() == MovementHandler.Mode.Party) {
                Game.interfaceLocker.setMovementMode(MovementHandler.Mode.Single);
            } else {
                Game.interfaceLocker.setMovementMode(MovementHandler.Mode.Party);
            }

            Game.mainViewer.getMainPane().setMovementModeIcon();
        }
    }

    /**
     * A Callback for canceling all current movement
     *
     * @author Jared
     */

    public static class CancelMovement extends Binding
    {
        @Override
        public void run()
        {
            Game.mainViewer.getMainPane().cancelAllOrders();
        }
    }

    /**
     * A callback for showing the in game menu
     *
     * @author Jared
     */

    public static class ShowMenu extends Binding
    {
        @Override
        public void run()
        {
            InGameMenu menu = new InGameMenu(Game.mainViewer);
            menu.openPopupCentered();
        }
    }

    /**
     * A callback for ending the current turn with the end turn button
     *
     * @author Jared
     */

    public static class EndTurn extends Binding
    {
        @Override
        public void run()
        {
            if (Game.mainViewer.getMainPane().isEndTurnEnabled()) {
                Game.areaListener.nextTurn();
            }
        }
    }

    /**
     * A callback to perform a quicksave
     *
     * @author Jared
     */

    public static class Quicksave extends Binding
    {
        @Override
        public void run()
        {
            Game.mainViewer.updateInterface();

            if (Game.isInTurnMode() || Game.curCampaign.party.isDefeated()) {
                Game.mainViewer.addMessage("red", "You cannot save the game while in combat mode.");
                return;
            }

            File fout = SaveGameUtil.getNextQuickSaveFile();

            try {
                SaveGameUtil.saveGame(fout);
                Game.mainViewer.addMessage("link", "Quicksave successful.");
                Game.mainViewer.addFixedFadeAway("Quicksave successful.", 10, 10, Color.RED);
            } catch (Exception e) {
                Logger.appendToErrorLog("Error when quicksaving to " + fout.getPath(), e);
                Game.mainViewer.addMessage("red", "Error saving game!");
                fout.delete();
            }
        }
    }

    /**
     * A callback for using a specified quickbar slot
     *
     * @author Jared
     */

    public static class UseQuickbarSlot extends Binding
    {
        private final int index;

        private UseQuickbarSlot(int index)
        {
            this.index = index;
        }

        @Override
        public String getActionName()
        {
            return "UseQuickbarSlot" + index;
        }

        @Override
        public void run()
        {
            Game.mainViewer.getQuickbarViewer().getButtonAtViewIndex(index).activateSlot(Game.mainViewer.mouseX - 2,
                    Game.mainViewer.mouseY - 25);
        }
    }
}
