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

package hale.widgets;

import hale.Game;
import hale.Keybindings;
import hale.entity.Creature;
import hale.interfacelock.MovementHandler;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.ThemeInfo;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.renderer.AnimationState.StateKey;
import de.matthiasmann.twl.renderer.Image;

/**
 * The interface widget containing many of the buttons used to access
 * the various screens of the interface
 *
 * @author Jared Stephen
 */

public class MainPane extends Widget
{
    public static final StateKey STATE_NOTIFICATION = StateKey.get("notification");

    private final HotKeyButton endTurn;

    private final HotKeyButton[] windowButtons;

    private final MovementModeButton movementMode;

    private final HotKeyButton stop;

    private int buttonGap;
    private int rowGap;

    private int leftForLayout;

    /**
     * Create a new Widget
     */

    public MainPane()
    {
        this.setTheme("mainpane");

        windowButtons = new HotKeyButton[6];

        windowButtons[0] = new HotKeyButton();
        windowButtons[0].setTheme("menubutton");
        windowButtons[0].setHotKeyBinding(new Keybindings.ShowMenu());

        windowButtons[1] = new HotKeyButton();
        windowButtons[1].setTheme("mapbutton");
        windowButtons[1].setHotKeyBinding(new Keybindings.ToggleWindow(Game.mainViewer.miniMapWindow, "MiniMap"));

        windowButtons[2] = new HotKeyButton();
        windowButtons[2].setTheme("inventorybutton");
        windowButtons[2].setHotKeyBinding(new Keybindings.ToggleWindow(Game.mainViewer.inventoryWindow, "InventoryWindow"));

        windowButtons[3] = new HotKeyButton();
        windowButtons[3].setTheme("characterbutton");
        windowButtons[3].setHotKeyBinding(new Keybindings.ToggleWindow(Game.mainViewer.characterWindow, "CharacterWindow"));

        windowButtons[4] = new LogButton();
        windowButtons[4].setHotKeyBinding(new Keybindings.ToggleWindow(Game.mainViewer.logWindow, "LogWindow"));

        windowButtons[5] = new HotKeyButton();
        windowButtons[5].setTheme("messagesbutton");
        windowButtons[5].setHotKeyBinding(new Keybindings.ToggleWindow(Game.mainViewer.messagesWindow, "MessagesWindow"));

        for (Button button : windowButtons) {
            add(button);
        }

        endTurn = new HotKeyButton();
        endTurn.setTheme("endturnbutton");
        endTurn.setHotKeyBinding(new Keybindings.EndTurn());
        add(endTurn);

        movementMode = new MovementModeButton();
        movementMode.setHotKeyBinding(new Keybindings.ToggleMovementMode());
        add(movementMode);

        stop = new HotKeyButton();
        stop.setTheme("stopbutton");
        stop.setHotKeyBinding(new Keybindings.CancelMovement());
        add(stop);
    }

    /**
     * Get the x coordinate of the leftmost button in the mainpane
     *
     * @return the x coordinate of the leftmost button in the mainpane
     */

    public int getLeftForLayout()
    {
        return leftForLayout;
    }

    /**
     * Cancels all currently pending movement orders.  Used by the "stop" button
     */

    public void cancelAllOrders()
    {
        Game.interfaceLocker.interruptMovement();
    }

    @Override
    protected void applyTheme(ThemeInfo themeInfo)
    {
        super.applyTheme(themeInfo);

        buttonGap = themeInfo.getParameter("buttonGap", 0);
        rowGap = themeInfo.getParameter("rowGap", 0);
    }

    @Override
    protected void layout()
    {
        super.layout();

        movementMode.setSize(movementMode.getPreferredWidth(), movementMode.getPreferredHeight());
        stop.setSize(stop.getPreferredWidth(), stop.getPreferredHeight());

        stop.setPosition(getInnerRight() - stop.getWidth(), getInnerY());
        movementMode.setPosition(stop.getX(), stop.getBottom() + buttonGap);

        for (Button button : windowButtons) {
            button.setSize(button.getPreferredWidth(), button.getPreferredHeight());
        }

        int bWidth = windowButtons[0].getWidth();

        windowButtons[0].setPosition(stop.getX() - rowGap - bWidth, getInnerY());
        windowButtons[1].setPosition(windowButtons[0].getX() - buttonGap - bWidth, getInnerY());
        windowButtons[5].setPosition(windowButtons[1].getX() - buttonGap - bWidth, getInnerY());

        windowButtons[2].setPosition(stop.getX() - rowGap - bWidth, getInnerY() + windowButtons[0].getHeight() + buttonGap);
        windowButtons[3].setPosition(windowButtons[2].getX() - buttonGap - bWidth, windowButtons[2].getY());
        windowButtons[4].setPosition(windowButtons[3].getX() - buttonGap - bWidth, windowButtons[2].getY());


        endTurn.setSize(endTurn.getPreferredWidth(), endTurn.getPreferredHeight());

        endTurn.setPosition(getInnerRight() - endTurn.getWidth(),
                getInnerBottom() - endTurn.getHeight());

        leftForLayout = windowButtons[4].getX();
    }

    /**
     * Sets the icon currently being shown in the movement mode button
     */

    public void setMovementModeIcon()
    {
        if (Game.interfaceLocker.getMovementMode() == MovementHandler.Mode.Party) {
            movementMode.setOverlay(movementMode.partyIcon);
        } else {
            movementMode.setOverlay(movementMode.singleIcon);
        }
    }

    /**
     * Sets the new log entry notification state to the specified value
     *
     * @param newLogEntry whether a  new log entry notification should be
     *                    shown
     */

    public void setLogNotification(boolean newLogEntry)
    {
        ((LogButton)windowButtons[4]).setNotification(newLogEntry);
    }

    /**
     * Resets the view of this Widget to the default state
     */

    public void resetView()
    {
        ((LogButton)windowButtons[4]).setNotification(false);
    }

    /**
     * Updates the state of this widget with any changes
     */

    public void update()
    {
        endTurn.setEnabled(isEndTurnEnabled());

        Creature current = Game.curCampaign.party.getSelected();
        if (Game.isInTurnMode()) current = Game.areaListener.getCombatRunner().lastActiveCreature();

        endTurn.getAnimationState().setAnimationState(STATE_NOTIFICATION, !current.timer.canAttack());
    }

    /**
     * Returns true if the end turn functionality of the end turn button
     * is currently enabled, false otherwise.  Note that this function can
     * return a different value than the actual state of the end turn button.
     * The end turn button is only updated when {@link #update()} is called
     *
     * @return whether the end turn functionality is enabled
     */

    public boolean isEndTurnEnabled()
    {
        return !Game.interfaceLocker.locked() && Game.isInTurnMode() &&
                !Game.areaListener.getTargeterManager().isInTargetMode();
    }

    private class LogButton extends HotKeyButton
    {
        private void setNotification(boolean notification)
        {
            getAnimationState().setAnimationState(STATE_NOTIFICATION, notification);
        }
    }

    private class MovementModeButton extends HotKeyButton
    {
        private Image singleIcon, partyIcon;

        @Override
        protected void applyTheme(ThemeInfo themeInfo)
        {
            super.applyTheme(themeInfo);

            singleIcon = themeInfo.getImage("singleicon");
            partyIcon = themeInfo.getImage("partyicon");
        }
    }
}
