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

package main.java.hale.icon;

/**
 * An IconRenderer is a class that draws an Icon.  Most icons are their own renderers,
 * but ComposedCreatureIcons have a special SubIconRenderer class
 *
 * @author Jared
 */

public interface IconRenderer
{

    /**
     * Draws the icon at the specified position
     *
     * @param x the x coordinate
     * @param y the y coordinate
     */

    public void draw(int x, int y);

    /**
     * Draws this icon centered on the specified position
     *
     * @param x      the x coordinate
     * @param y      the y coordinate
     * @param width  the width of the region to center on
     * @param height the height of the region to center on
     */

    public void drawCentered(int x, int y, int width, int height);
}
