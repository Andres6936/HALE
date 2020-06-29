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

package main.java.hale.icon;

import de.matthiasmann.twl.Color;
import main.java.hale.loading.JSONOrderedObject;

/**
 * An Icon is the object used to render an Entity.  It can consist of a
 * single image, or a collection of images (sub-icons)
 * <p>
 * This class is immutable
 *
 * @author Jared
 */

public interface Icon extends IconRenderer
{

    /**
     * Returns the width of the sprite drawn by this icon, or 0
     * if it is empty
     *
     * @return the width of this icon
     */

    public int getWidth();

    /**
     * Returns the height of the sprite drawn by this icon, or 0
     * if it is empty
     *
     * @return the height of this icon
     */

    public int getHeight();

    /**
     * Gets an Icon that is a copy of this Icon, except multiplied
     * by the specified color
     *
     * @param color
     * @return a copy of this Icon with the new Color
     */

    public Icon multiplyByColor(Color color);

    /**
     * Stores a representation of this Icon as JSON
     *
     * @return the JSON object containing the Icon data
     */

    public JSONOrderedObject save();
}