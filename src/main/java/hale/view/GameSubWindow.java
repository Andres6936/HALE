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

package hale.view;

import hale.Game;
import de.matthiasmann.twl.ResizableFrame;
import de.matthiasmann.twl.ThemeInfo;

/**
 * The base class for all GUI Windows in the game.  Windows can be moved, resized,
 * and closed.  They automatically gain focus when becoming visible.
 *
 * @author Jared Stephen
 */

public abstract class GameSubWindow extends ResizableFrame implements Runnable
{
    private int defaultPositionX, defaultPositionY;
    private int defaultWidth, defaultHeight;

    /**
     * Create a new GameSubWindow with the default theme and Close Callback
     */

    public GameSubWindow()
    {
        this.addCloseCallback(this);
    }

    /**
     * Returns the close callback for this GameSubWindow
     *
     * @return the close callback for this GameSubWindow
     */

    public Runnable getCloseCallback()
    {
        return this;
    }

    /**
     * Sets the position of this Window as centered in the overall Game window
     */

    public void setPositionCentered()
    {
        int w = Game.config.getResolutionX();
        int h = Game.config.getResolutionY();

        this.setPosition((w - getInnerWidth()) / 2, (h - getInnerHeight()) / 2 - 15);
    }

    /**
     * Sets the position of this GameSubWindow to the default position, defined
     * by params defaultX and defaultY in this Widget's theme.  This function should
     * only be called by the parent Widget's layout()
     */

    public void setPositionDefault()
    {
        setPosition(super.getInnerX() + defaultPositionX, super.getInnerY() + defaultPositionY);
    }

    /**
     * Sets the size of this GameSubWindow to the default size,
     * defined by params defaultWidth and defaultHeight in this Widget's theme.  This function
     * should only be called by the parent Widget's layout()
     */

    public void setSizeDefault()
    {
        setSize(defaultWidth, defaultHeight);
    }

    @Override
    public void setVisible(boolean visible)
    {
        if (this.isVisible() != visible) {
            super.setVisible(visible);
        }

        if (visible) requestKeyboardFocus();
    }

    @Override
    protected void applyTheme(ThemeInfo themeInfo)
    {
        super.applyTheme(themeInfo);

        defaultPositionX = themeInfo.getParameter("defaultX", 0);
        defaultPositionY = themeInfo.getParameter("defaultY", 0);
        defaultWidth = themeInfo.getParameter("defaultWidth", 0);
        defaultHeight = themeInfo.getParameter("defaultHeight", 0);

        setSizeDefault();
        setPositionDefault();
    }

    @Override
    protected void layout()
    {
        super.layout();

        // restrict window to within screen on creation
        int x = getX();
        int y = getY();

        x = Math.max(0, x);
        y = Math.max(0, y);

        x = Math.min(x, Game.config.getResolutionX() - getWidth());
        y = Math.min(y, Game.config.getResolutionY() - getHeight());

        if (x != getX() || y != getY()) setPosition(x, y);
    }

    // close callback
    @Override
    public void run()
    {
        setVisible(false);
    }
}
