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

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ThemeInfo;
import de.matthiasmann.twl.Widget;

/**
 * A menu level within the overall RightClickMenu.  The menu level will
 * contain a title label and generally one or more buttons.
 *
 * @author Jared Stephen
 */

public class RightClickMenuLevel extends Widget
{
    private int titleBorder;

    private final List<Button> buttons;
    private final Label title;

    private RightClickMenuLevel parentLevel;
    private Widget parentButton;

    /**
     * Creates a new RightClickMenuLevel with the specified parent level and button.  When the
     * widget position is set, it is based on the right side of the parent menu level and the
     * y coordinate of the parent button.  If there are no parents, the position is set to the
     * inner coordinates of the RightClickMenu PopupWindow
     *
     * @param parentLevel  the parent MenuLevel.  If this is null, there is no parent menu
     *                     level
     * @param parentButton the parent button.  If this is null, there is no parent menu button.
     */

    public RightClickMenuLevel(RightClickMenuLevel parentLevel, Widget parentButton)
    {
        this.parentLevel = parentLevel;
        this.parentButton = parentButton;
        buttons = new ArrayList<Button>();

        title = new Label();
        title.setTheme("titlelabel");
        this.add(title);
    }

    /**
     * Returns the text on the button for the specified selection
     *
     * @param index the index of the selection to get
     * @return the button text
     */

    public String getSelectionText(int index)
    {
        return buttons.get(index).getText();
    }

    /**
     * Runs the callback of the selection at the specified index, as if the corresponding
     * menu button has been pressed
     *
     * @param index the button index
     */

    public void activateSelection(int index)
    {
        buttons.get(index).getModel().fireActionCallback();
    }

    /**
     * Returns the number of different selections (buttons) in this menu level
     *
     * @return the number of different selections
     */

    public int getNumSelections()
    {
        return buttons.size();
    }

    /**
     * Returns the parent button for this RightClickMenuLevel
     *
     * @return the parent button
     */

    public Widget getParentButton()
    {
        return parentButton;
    }

    /**
     * Returns the parent menu level for this RightClickMenuLevel
     *
     * @return the parent menu level
     */

    public Widget getParentMenuLevel()
    {
        return parentLevel;
    }

    /**
     * Returns the start y position of the buttons in this level relative
     * to the widget top
     *
     * @return the start y position of this buttons in this level
     */

    public int getButtonsOffsetY()
    {
        return title.getPreferredHeight() + getBorderTop();
    }

    /**
     * Sets the title text for this Menu Level
     *
     * @param text the title text
     */

    public void setTitle(String text)
    {
        title.setText(text);
    }

    /**
     * the title
     *
     * @return the title for this menu level as a string
     */

    public String getTitle()
    {
        return title.getText();
    }

    /**
     * Returns the last button that was clicked in this Menu Level.  Used to determine
     * the parent button for new menu levels created from this menu level
     *
     * @return the last button that was clicked in this Menu Level
     */

    public Button getButtonWithFocus()
    {
        for (Button button : buttons) {
            if (button.hasKeyboardFocus()) return button;
        }

        return null;
    }

    /**
     * Disables all buttons currently in this menu level.  This does not
     * have any effect on buttons added after this call
     */

    public void disableAllButtons()
    {
        for (Button button : buttons) {
            button.setEnabled(false);
        }
    }

    /**
     * Adds the specified button to this Menu Level.  It is added at the bottom
     * of all existing buttons
     *
     * @param button the button to add
     */

    public void addButton(Button button)
    {
        buttons.add(button);
        button.setTheme("menubutton");
        this.add(button);
    }

    @Override
    protected void applyTheme(ThemeInfo themeInfo)
    {
        super.applyTheme(themeInfo);

        titleBorder = themeInfo.getParameter("titleBorder", 0);
    }

    @Override
    public int getPreferredWidth()
    {
        int maxW = title.getPreferredWidth() + titleBorder;
        for (Button b : buttons) {
            maxW = Math.max(maxW, b.getPreferredWidth());
        }

        return maxW + getBorderHorizontal();
    }

    @Override
    public int getPreferredHeight()
    {
        int totalHeight = getBorderBottom();
        for (Button b : buttons) {
            totalHeight += b.getPreferredHeight();
        }

        if (parentButton != null) {
            return totalHeight + parentButton.getY() - parentLevel.getY();
        } else {
            return totalHeight + title.getPreferredHeight() + getBorderTop();
        }
    }

    @Override
    protected void layout()
    {
        super.layout();

        title.setSize(title.getPreferredWidth(), title.getPreferredHeight());
        title.setPosition(getInnerX() + getInnerWidth() / 2 - title.getWidth() / 2, getInnerY());

        int curY = title.getBottom();
        for (Button b : buttons) {
            b.setSize(getInnerWidth(), b.getPreferredHeight());
            b.setPosition(getInnerX(), curY);
            curY = b.getBottom();
        }
    }
}
