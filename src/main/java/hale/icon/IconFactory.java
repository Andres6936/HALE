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

package hale.icon;

import de.matthiasmann.twl.Color;
import hale.loading.JSONOrderedObject;
import hale.util.SimpleJSONObject;

/**
 * Class for creating the right type of Icon based on JSON data
 *
 * @author Jared
 */

public class IconFactory
{
    /**
     * An empty Icon that draws nothing and does nothing when caching sprites
     */

    public static Icon emptyIcon = new EmptyIcon();

    /**
     * Creates an Icon of the correct type based on the specified JSON
     *
     * @param data the JSON to parse
     * @return a newly created Icon
     */

    public static Icon createIcon(SimpleJSONObject data)
    {
        if (data.containsKey("frames")) {
            return new AnimatedIcon(data);
        } else
            if (data.containsKey("subIcons")) {
                return new ComposedCreatureIcon(data);
            } else
                if (data.containsKey("composed")) {
                    return new ComposedIcon(data);
                } else {
                    return new SimpleIcon(data);
                }
    }

    /**
     * Creates an Icon displaying the sprite with the specified resource location
     *
     * @param spriteID
     * @return a newly created Icon displaying the Sprite
     */

    public static Icon createIcon(String spriteID)
    {
        return new SimpleIcon(spriteID);
    }

    /**
     * Creates an Icon displaying the sprite with the specified resource location
     *
     * @param spriteID
     * @param color
     * @return a newly created Icon displaying the Sprite
     */

    public static Icon createIcon(String spriteID, Color color)
    {
        return new SimpleIcon(spriteID, color);
    }

    private static class EmptyIcon implements Icon
    {
        @Override
        public void drawCentered(int x, int y, int width, int height)
        {
        }

        @Override
        public void draw(int x, int y)
        {
        }

        @Override
        public int getWidth()
        {
            return 0;
        }

        @Override
        public int getHeight()
        {
            return 0;
        }

        @Override
        public JSONOrderedObject save()
        {
            return new JSONOrderedObject();
        }

        @Override
        public EmptyIcon multiplyByColor(Color color)
        {
            return this;
        }
    }
}
