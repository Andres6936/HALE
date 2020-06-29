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

package main.java.hale.quickbar;

import main.java.hale.Game;
import main.java.hale.Keybindings;
import main.java.hale.icon.Icon;
import main.java.hale.icon.IconFactory;
import main.java.hale.view.DragTarget;
import main.java.hale.view.DropTarget;
import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.Color;
import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ThemeInfo;

/**
 * A Button for viewing and activating an individual Quickbar slot.  Each
 * QuickbarSlotButton shows the icon for one Quickbar and an index indicating
 * the keyboard hotkey.
 *
 * @author Jared Stephen
 */

public class QuickbarSlotButton extends Button implements DropTarget
{
    private QuickbarSlot slot;
    private Quickbar quickbar;

    private Icon overrideIcon;

    private Icon icon;

    private Icon secondaryIcon;

    private final int index;

    private Label primaryLabel, indexLabel;
    private String emptyTooltip;

    private QuickbarSlot dragSlotToAdd;

    private boolean disabledExceptActivate;

    /**
     * Creates a new QuickbarSlotButton that can be used to activate a QuickbarSlot.
     *
     * @param index the index that will displayed by this Quickbar.  In general, this
     *              will not be the actual QuickbarSlot index.  It is instead the index within
     *              the currently displayed set of QuickbarSlots by the QuickbarViewer.
     */

    public QuickbarSlotButton(int index)
    {
        this.index = index;

        // key the keyboard mapping character
        int keyCode = Game.config.getKeyForAction(Keybindings.UseQuickbarSlot + Integer.toString(index));

        if (keyCode != -1) {
            String keyChar = Event.getKeyNameForCode(keyCode);
            indexLabel = new Label(keyChar);
        } else {
            indexLabel = new Label();
        }

        indexLabel.setTheme("indexlabel");
        this.add(indexLabel);

        primaryLabel = new Label();
        primaryLabel.setTheme("primarylabel");
        this.add(primaryLabel);

        this.icon = IconFactory.emptyIcon;
    }

    /**
     * Sets whether or not the label showing the viewer index is shown
     *
     * @param show
     */

    public void setShowIndexLabel(boolean show)
    {
        indexLabel.setVisible(show);
    }

    /**
     * Sets whether all actions except for the basic left click should be disabled
     *
     * @param disable whether all actions (show right click menu, drag & drop) except
     *                for left click activate are disabled
     */

    public void setDisabledExceptActivate(boolean disable)
    {
        this.disabledExceptActivate = disable;
    }

    @Override
    protected void layout()
    {
        super.layout();

        setSize(getPreferredWidth(), getPreferredHeight());

        indexLabel.setPosition(getInnerRight() - indexLabel.getPreferredWidth(), getInnerY() + indexLabel.getPreferredHeight() / 2);
        primaryLabel.setPosition(getInnerX(), getInnerBottom() - primaryLabel.getPreferredHeight() / 2);
    }

    /**
     * Sets the override Icon for this Button.  This causes the Button to display
     * the specified Icon while the override is in effect.  Once the override is
     * cleared with {@link #clearOverrideIcon()}, the Button goes back to displaying
     * the usual Icon(s).  This is used by the QuickbarDragHandler as a cue for drag
     * and drop.
     *
     * @param icon
     */

    public void setOverrideIcon(Icon icon)
    {
        this.overrideIcon = icon;
    }

    public void clearOverrideIcon()
    {
        this.overrideIcon = null;
    }

    /**
     * Sets this QuickbarSlotButton to use the specified QuickbarSlot.  If the
     * passed parameter is null, the icon and text displayed by this button are
     * cleared.
     *
     * @param slot     the QuickbarSlot to display.
     * @param quickbar the Quickbar containing the specified slot
     */

    public void setSlot(QuickbarSlot slot, Quickbar quickbar)
    {
        this.slot = slot;
        this.quickbar = quickbar;

        if (slot == null) {
            if (this.getTooltipContent() != emptyTooltip) {
                this.setTooltipContent(emptyTooltip);
            }

            this.icon = IconFactory.emptyIcon;
            this.primaryLabel.setText("");
            this.secondaryIcon = null;
        } else {

            if (slot.isActivateable()) {
                this.icon = slot.getIcon();
                this.secondaryIcon = slot.getSecondaryIcon();
            } else {
                this.icon = slot.getIcon().multiplyByColor(new Color(0xFF7F7F7F));

                if (slot.getSecondaryIcon() != null) {
                    this.secondaryIcon = slot.getSecondaryIcon().multiplyByColor(new Color(0xFF7F7F7F));
                }
            }

            primaryLabel.setText(slot.getLabelText());

            String tooltip = slot.getTooltipText();
            if (!tooltip.equals(getTooltipContent())) {
                this.setTooltipContent(tooltip);
            }
        }
    }

    /**
     * Returns the index within the set of displayed QuickbarSlots of this
     * QuickbarSlotButton.
     *
     * @return the index of this QuickbarSlotButton
     */

    public int getIndex()
    {
        return index;
    }

    @Override
    protected boolean handleEvent(Event evt)
    {
        if (Game.interfaceLocker.locked()) return super.handleEvent(evt);

        switch (evt.getType()) {
            case MOUSE_DRAGGED:
            case MOUSE_BTNUP:
                if (!isMouseInside(evt)) break;
                switch (evt.getMouseButton()) {
                    case Event.MOUSE_LBUTTON:
                        activateSlot(getRight(), getY());
                        break;
                    case Event.MOUSE_RBUTTON:
                        if (disabledExceptActivate) {
                            slot.showExamineWindow(this);
                        } else {
                            createRightClickMenu(getRight(), getY());
                        }
                        break;
                }
            default:
        }

        return super.handleEvent(evt);
    }

    @Override
    protected void applyTheme(ThemeInfo themeInfo)
    {
        super.applyTheme(themeInfo);

        emptyTooltip = themeInfo.getParameter("emptytooltip", (String)null);
    }

    @Override
    public void paintWidget(GUI gui)
    {
        super.paintWidget(gui);

        if (overrideIcon != null) {
            overrideIcon.drawCentered(getInnerX(), getInnerY(), getInnerWidth(), getInnerHeight());
        } else
            if (secondaryIcon != null) {
                secondaryIcon.drawCentered(getInnerX() + 5, getInnerY(), getInnerWidth(), getInnerHeight());
                icon.drawCentered(getInnerX() - 5, getInnerY(), getInnerWidth(), getInnerHeight());
            } else {
                icon.drawCentered(getInnerX(), getInnerY(), getInnerWidth(), getInnerHeight());
            }
    }

    /**
     * Activates the quickbar slot held by this QuickbarSlotButton.
     * Any menu that is opened as a result of activating will be opened at the
     * specified position
     *
     * @param menuPositionX the menu x coordinate if a menu is opened
     * @param menuPositionY the menu y coordinate if a menu is opened
     */

    public void activateSlot(int menuPositionX, int menuPositionY)
    {
        if (slot == null) return;

        Game.mainViewer.getMenu().clear();
        Game.mainViewer.getMenu().setPosition(menuPositionX, menuPositionY);

        slot.activate(this);
    }

    /**
     * Creates a new right click menu at the specified coordinates using the
     * current slot for this QuickbarSlotButton.  If this QuickbarSlotButton
     * is empty, no action is taken
     *
     * @param menuPositionX the x screen coordinate
     * @param menuPositionY the y screen coordinate
     */

    public void createRightClickMenu(int menuPositionX, int menuPositionY)
    {
        if (slot == null) return;

        Game.mainViewer.getMenu().clear();
        Game.mainViewer.getMenu().setPosition(menuPositionX, menuPositionY);

        slot.createRightClickMenu(this);
    }

    /**
     * Returns an activate callback for this button
     *
     * @param slot
     * @return the activate callback
     */

    public Runnable getActivateSlotCallback(QuickbarSlot slot)
    {
        return new ActivateSlotCallback(slot);
    }

    /**
     * Returns a callback which will clear this button
     *
     * @return the clear callback
     */

    public Runnable getClearSlotCallback()
    {
        return new ClearSlotCallback(this);
    }

    /**
     * A callback for use in clearing this QuickbarSlotButton
     *
     * @author Jared Stephen
     */

    private static class ClearSlotCallback implements Runnable
    {
        private QuickbarSlotButton button;

        /**
         * Creates a new ClearSlotCallback
         *
         * @param button the QuickbarSlotButton to clear
         */

        public ClearSlotCallback(QuickbarSlotButton button)
        {
            this.button = button;
        }

        @Override
        public void run()
        {
            button.quickbar.setSlot(null, button.index);
            button.setSlot(null, button.quickbar);
            Game.mainViewer.getMenu().hide();
        }
    }

    /**
     * A callback for use in activating a QuickbarSlot. this callback can be used
     * instead of the normal left click activate for this slot
     *
     * @author Jared Stephen
     */

    private class ActivateSlotCallback implements Runnable
    {
        private QuickbarSlot slot;

        /**
         * Creates a new ActivateSlotCallback
         *
         * @param slot the QuickbarSlot to activate
         */

        public ActivateSlotCallback(QuickbarSlot slot)
        {
            this.slot = slot;
        }

        @Override
        public void run()
        {
            Game.mainViewer.getMenu().hide();
            slot.activate(QuickbarSlotButton.this);
        }
    }

    @Override
    public void dragAndDropStartHover(DragTarget target)
    {
        if (target.getItem() != null && target.getParentPC() == this.quickbar.getParent()) {
            dragSlotToAdd = Quickbar.getQuickbarSlot(target.getItem(), target.getParentPC());
        } else {
            dragSlotToAdd = null;
        }

        if (dragSlotToAdd != null) {
            this.setOverrideIcon(target.getDragIcon().multiplyByColor(new Color(0xFF555555)));
        }
    }

    @Override
    public void dragAndDropStopHover(DragTarget target)
    {
        this.clearOverrideIcon();
    }

    @Override
    public void dropDragTarget(DragTarget target)
    {
        this.clearOverrideIcon();

        if (dragSlotToAdd != null) {
            quickbar.setSlot(dragSlotToAdd, index);
            this.setSlot(quickbar.getSlot(index), quickbar);
            Game.mainViewer.updateInterface();
        }
    }
}
