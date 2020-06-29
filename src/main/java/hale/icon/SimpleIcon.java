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

package hale.icon;

import org.lwjgl.opengl.GL11;

import hale.loading.JSONOrderedObject;
import hale.resource.Sprite;
import hale.resource.SpriteManager;
import hale.util.SimpleJSONObject;
import de.matthiasmann.twl.Color;

/**
 * A basic implementation of an icon with a sprite and a color
 *
 * @author Jared
 */

public class SimpleIcon implements Icon
{
    private final String spriteResourceID;
    private final Color color;

    /**
     * Creates a new SimpleIcon from the specified JSON data
     *
     * @param data the JSON data to parse
     */

    public SimpleIcon(SimpleJSONObject data)
    {
        this.spriteResourceID = data.get("sprite", null);

        if (data.containsKey("color")) {
            this.color = Color.parserColor(data.get("color", null));
        } else {
            this.color = Color.WHITE;
        }
    }

    /**
     * Creates a new SimpleIcon displaying the specified sprite
     *
     * @param spriteID the sprite to display
     */

    protected SimpleIcon(String spriteID)
    {
        this.spriteResourceID = spriteID;
        this.color = Color.WHITE;
    }

    /**
     * Creates a new SimpleIcon with the specified sprite and color
     *
     * @param spriteResourceID
     * @param color
     */

    protected SimpleIcon(String spriteResourceID, Color color)
    {
        this.spriteResourceID = spriteResourceID;
        this.color = color;
    }

    /**
     * Gets the resource ID of the sprite drawn by this Icon
     *
     * @return the sprite's resource ID
     */

    public String getSpriteID()
    {
        return spriteResourceID;
    }

    /**
     * Gets the color that this Icon is drawn in
     *
     * @return the color
     */

    public Color getColor()
    {
        return color;
    }

    @Override
    public SimpleIcon multiplyByColor(Color color)
    {
        Color newColor = this.color.multiply(color);

        return new SimpleIcon(this.spriteResourceID, newColor);
    }

    @Override
    public void draw(int x, int y)
    {
        GL11.glColor4ub(color.getR(), color.getG(), color.getB(), color.getA());
        SpriteManager.getSprite(spriteResourceID).draw(x, y);
    }

    @Override
    public void drawCentered(int x, int y, int width, int height)
    {
        Sprite sprite = SpriteManager.getSprite(spriteResourceID);

        GL11.glColor4ub(color.getR(), color.getG(), color.getB(), color.getA());
        sprite.draw(x + (width - sprite.getWidth()) / 2, y + (height - sprite.getHeight()) / 2);
    }

    @Override
    public int getWidth()
    {
        return SpriteManager.getSprite(spriteResourceID).getWidth();
    }

    @Override
    public int getHeight()
    {
        return SpriteManager.getSprite(spriteResourceID).getHeight();
    }

    @Override
    public JSONOrderedObject save()
    {
        JSONOrderedObject out = new JSONOrderedObject();

        out.put("sprite", spriteResourceID);
        out.put("color", "#" + Integer.toHexString(color.toARGB()));

        return out;
    }

    @Override
    public boolean equals(Object other)
    {
        if (!(other instanceof SimpleIcon)) return false;

        return ((SimpleIcon)other).spriteResourceID.equals(this.spriteResourceID);
    }

    @Override
    public int hashCode()
    {
        return spriteResourceID.hashCode();
    }
}
