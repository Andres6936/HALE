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

import java.util.ArrayList;
import java.util.List;

import main.java.hale.loading.JSONOrderedObject;
import main.java.hale.util.SimpleJSONArrayEntry;
import main.java.hale.util.SimpleJSONObject;
import de.matthiasmann.twl.Color;

/**
 * A composed icon consists of one or more icons layered on top of one another
 *
 * @author Jared
 */

public class ComposedIcon implements Icon
{
    private int width, height;

    private List<Icon> icons;

    /**
     * Creates a new ComposedIcon by parsing the specified JSON data
     *
     * @param data
     */

    public ComposedIcon(SimpleJSONObject data)
    {
        this.icons = new ArrayList<Icon>();

        for (SimpleJSONArrayEntry entry : data.getArray("composed")) {
            this.icons.add(IconFactory.createIcon(entry.getObject()));
        }

        ((ArrayList<Icon>)this.icons).trimToSize();
    }

    /**
     * Creates a new composed icon consisting of the specified icon, layered
     * such that the final icon is on top, with previous ones below that in order
     *
     * @param icons
     */

    public ComposedIcon(Icon... icons)
    {
        this.icons = new ArrayList<Icon>(icons.length);

        int width = 0;
        int height = 0;
        for (Icon icon : icons) {
            this.icons.add(icon);
            width = Math.max(width, icon.getWidth());
            height = Math.max(height, icon.getHeight());
        }

        this.width = width;
        this.height = height;
    }

    @Override
    public void draw(int x, int y)
    {
        for (Icon icon : icons) {
            icon.draw(x, y);
        }
    }

    @Override
    public void drawCentered(int x, int y, int width, int height)
    {
        for (Icon icon : icons) {
            icon.drawCentered(x, y, width, height);
        }
    }

    @Override
    public int getWidth()
    {
        return width;
    }

    @Override
    public int getHeight()
    {
        return height;
    }

    @Override
    public Icon multiplyByColor(Color color)
    {
        Icon[] newIcons = new Icon[icons.size()];

        for (int i = 0; i < newIcons.length; i++) {
            newIcons[i] = this.icons.get(i).multiplyByColor(color);
        }

        return new ComposedIcon(newIcons);
    }

    @Override
    public JSONOrderedObject save()
    {
        JSONOrderedObject[] icons = new JSONOrderedObject[this.icons.size()];

        int i = 0;
        for (Icon icon : this.icons) {
            icons[i] = icon.save();

            i++;
        }

        JSONOrderedObject data = new JSONOrderedObject();
        data.put("composed", icons);
        return data;
    }
}
