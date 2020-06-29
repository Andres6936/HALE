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

package main.java.hale.widgets;

import java.util.ArrayList;
import java.util.List;

import main.java.hale.Game;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.PopupWindow;
import de.matthiasmann.twl.Widget;

/**
 * The menu shown when the user right clicks on a variety of widgets
 * in the interface as well as on the main area map
 *
 * @author Jared Stephen
 */

public class RightClickMenu extends PopupWindow
{
    private final List<RightClickMenuLevel> levels;
    private final Widget content;
    private boolean popupShouldToggle = false;

    /**
     * Creates a new RightClickMenu with the specified parent Widget
     *
     * @param parent the parent Widget
     */

    public RightClickMenu(Widget parent)
    {
        super(parent);

        content = new MenuContent();
        this.add(content);

        this.setCloseOnClickedOutside(true);
        this.setCloseOnEscape(true);

        levels = new ArrayList<RightClickMenuLevel>();
    }

    /**
     * Returns true if this menu is opening and will toggle on the next mainviewer update
     *
     * @return true if this menu will open
     */

    public boolean isOpening()
    {
        return popupShouldToggle && !isOpen();
    }

    /**
     * Adds a new menu level with the specified title string shown
     * at the top of the new level.  The new menu level will be shown
     * to the right of any existing levels.
     *
     * @param title the title string for the new menu level
     * @return true if the menu level was added, false if it could not be added
     */

    public boolean addMenuLevel(String title)
    {
        popupShouldToggle = false;

        // first disallow adding a level with the same title as a previous
        // this is a bit of a hacky way to prevent duplicates
        for (RightClickMenuLevel level : levels) {
            if (level.getTitle().equals(title)) {
                return false;
            }
        }

        RightClickMenuLevel newMenu;

        if (levels.size() == 0) {
            newMenu = new RightClickMenuLevel(null, null);
        } else {
            RightClickMenuLevel topMenu = levels.get(levels.size() - 1);
            Button focused = topMenu.getButtonWithFocus();
            if (focused != null) {
                newMenu = new RightClickMenuLevel(topMenu, focused);
            } else {
                newMenu = new RightClickMenuLevel(topMenu, null);
            }
        }

        newMenu.setTitle(title);

        levels.add(newMenu);
        content.add(newMenu);

        return true;
    }

    /**
     * Returns the lowest (most recently opened) menu level in this menu, or null
     * if no menu levels are open
     *
     * @return the most recently opened menu level
     */

    public RightClickMenuLevel getLowestMenuLevel()
    {
        if (levels.isEmpty()) return null;

        return levels.get(levels.size() - 1);
    }

    /**
     * Disables all buttons currently in the right click menu.  This does not have any
     * effect on buttons added to the menu after this call
     */

    public void disableAllButtons()
    {
        for (RightClickMenuLevel level : levels) {
            level.disableAllButtons();
        }
    }

    /**
     * Adds the specified button as a menu option to the current
     * top most (most recently added) menu level
     *
     * @param button the button to add
     */

    public void addButton(Button button)
    {
        levels.get(levels.size() - 1).addButton(button);
    }

    /**
     * Removes all menu levels with an index greater than or equal to the specified
     * index in this RightClickMenu
     *
     * @param menuLevel the menu level index; all levels with an index greater than
     *                  or equal to this will be removed
     */

    public void removeMenuLevelsAbove(int menuLevel)
    {
        int curLevel = menuLevel;
        int count = levels.size() - menuLevel;

        for (int i = 0; i < count; i++) {
            content.removeChild(levels.get(curLevel));
            levels.remove(curLevel);
        }
    }

    /**
     * Removes all menu levels and all buttons from this RightClickMenu
     * so that the content Widget is empty with no children.
     */

    public void clear()
    {
        for (RightClickMenuLevel level : levels) {
            level.removeAllChildren();
        }

        levels.clear();
        content.removeAllChildren();
    }

    /**
     * Tells this Widget to show itself on the next MainViewer update.  This delay
     * prevent thread concurrency issues.
     */

    public void show()
    {
        if (levels.size() != 0 && !this.isOpen()) {
            popupShouldToggle = true;
        } else {
            popupShouldToggle = false;
        }

        adjustSize();
    }

    /**
     * Tells this Widget to hide itself during the next MainViewer update.  This delay
     * prevents thread concurrency issues.
     */

    public void hide()
    {
        if (this.isOpen()) {
            popupShouldToggle = true;
        } else {
            popupShouldToggle = false;
        }
    }

    /**
     * Opens this PopupWindow if it is closed, or closes it if it is open.  Immediately after this
     * method is called {@link #shouldPopupToggle()} will return false;
     * <p>
     * This method should only be called by the MainViewer (main thread) during its
     * normal update cycle.  Calling this method from a non-main thread can cause
     * concurrency issues.
     */

    public synchronized void togglePopup()
    {
        popupShouldToggle = false;

        if (this.isOpen()) {
            this.closePopup();
        } else {
            openPopup();
        }

        adjustSize();
    }

    /**
     * Returns true if this PopupWindow should have {@link #togglePopup()} called
     * during the next MainViewer update.
     *
     * @return true if this PopupWindow should be toggled, false otherwise
     */

    public boolean shouldPopupToggle()
    {
        return popupShouldToggle;
    }

    @Override
    protected void layout()
    {
        super.layout();

        // restrict widget to screen area
        int x = Math.max(0, getX());
        int y = Math.max(0, getY());

        x = Math.min(x, Game.config.getResolutionX() - getWidth());
        y = Math.min(y, Game.config.getResolutionY() - getHeight());

        if (x != getX() || y != getY()) {
            setPosition(x, y);
        }
    }

    private class MenuContent extends Widget
    {
        private MenuContent()
        {
            this.setTheme("content");
        }

        @Override
        protected void layout()
        {
            super.layout();

            for (RightClickMenuLevel level : levels) {
                Widget parentLevel = level.getParentMenuLevel();
                if (parentLevel == null) {
                    level.setPosition(getInnerX(), getInnerY());
                } else {
                    Widget button = level.getParentButton();
                    if (button == null) {
                        level.setPosition(parentLevel.getRight(), parentLevel.getY());
                    } else {
                        level.setPosition(parentLevel.getRight(), button.getY() - level.getButtonsOffsetY());
                    }
                }

                level.setSize(level.getPreferredWidth(), level.getPreferredHeight() - level.getY() + getInnerY());
            }
        }

        @Override
        public int getPreferredWidth()
        {
            int width = getBorderHorizontal();
            for (RightClickMenuLevel level : levels) {
                width += level.getPreferredWidth();
            }

            return width;
        }

        @Override
        public int getPreferredHeight()
        {
            int maxHeight = 0;
            for (RightClickMenuLevel level : levels) {
                maxHeight = Math.max(maxHeight, level.getPreferredHeight());
            }

            return maxHeight + getBorderVertical();
        }

        @Override
        public boolean handleEvent(Event evt)
        {
            switch (evt.getType()) {
                case MOUSE_ENTERED:
                case MOUSE_BTNDOWN:
                    return true;
                case MOUSE_BTNUP:
                    hide();
                default:
                    return false;
            }
        }
    }
}
