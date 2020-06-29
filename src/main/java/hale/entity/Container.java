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

package hale.entity;

import hale.area.Area;
import hale.loading.JSONOrderedObject;
import hale.loading.LoadGameException;
import hale.loading.ReferenceHandler;
import hale.util.SimpleJSONObject;

/**
 * A container is an entity that holds items
 *
 * @author Jared
 */

public class Container extends Openable
{

    private final ContainerTemplate template;

    private final ItemList currentItems;

    private boolean lootGenerated;

    @Override
    public void load(SimpleJSONObject data, Area area, ReferenceHandler refHandler) throws LoadGameException
    {
        super.load(data, area, refHandler);

        lootGenerated = data.get("lootGenerated", false);
        currentItems.load(data.getArray("currentItems"));
    }

    @Override
    public JSONOrderedObject save()
    {
        JSONOrderedObject out = super.save();

        out.put("lootGenerated", lootGenerated);
        out.put("currentItems", currentItems.save());

        return out;
    }

    /**
     * Creates a new Container
     *
     * @param template
     */

    protected Container(ContainerTemplate template)
    {
        super(template);

        this.template = template;

        currentItems = template.getDefaultItems();

        lootGenerated = false;
    }

    @Override
    public ContainerTemplate getTemplate()
    {
        return template;
    }

    @Override
    public boolean attemptOpen(Creature opener)
    {
        boolean isOpen = super.attemptOpen(opener);

        if (isOpen && !lootGenerated) {
            currentItems.addAll(template.generateLoot());
            lootGenerated = true;
        }

        return isOpen;
    }

    /**
     * Returns the set of items currently contained in this container.  Note
     * that modifying this set modifies what is held in the container
     *
     * @return the set of held items
     */

    public ItemList getCurrentItems()
    {
        return currentItems;
    }

    @Override
    public int compareTo(Entity other)
    {
        if (other instanceof Creature) return -1;
        if (other instanceof Door) return -1;

        return super.compareTo(other);
    }
}
