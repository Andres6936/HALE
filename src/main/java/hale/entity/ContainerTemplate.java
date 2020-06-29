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

import hale.Game;
import hale.util.SimpleJSONObject;

/**
 * A template for containers including treasure chests and similar objects, workbenches,
 * armor stands, etc.
 *
 * @author Jared
 */

public class ContainerTemplate extends OpenableTemplate
{

    private final ItemList defaultItems;
    private final LootList loot;
    private final boolean isWorkbench;

    /**
     * Creates a new ContainerTemplate
     *
     * @param id   the Entity ID
     * @param data the JSON to parse
     */

    public ContainerTemplate(String id, SimpleJSONObject data)
    {
        super(id, data);

        if (data.containsKey("loot")) {
            this.loot = new LootList(data.getArray("loot"));
        } else {
            this.loot = null;
        }

        this.isWorkbench = data.get("isWorkbench", false);

        if (data.containsKey("defaultItems")) {
            defaultItems = new ItemList(data.getArray("defaultItems"));
        } else {
            defaultItems = new ItemList();
        }
    }

    @Override
    public Container createInstance()
    {
        return new Container(this);
    }

    /**
     * Returns the list of default items for this container.  Note that this method
     * makes a copy of the default item list and returns that copy, so that the
     * internal default items list cannot be modified
     *
     * @return the list of default items
     */

    public ItemList getDefaultItems()
    {
        return new ItemList(defaultItems);
    }

    /**
     * Generates an ItemList with randomly generated loot for this container
     *
     * @return the loot that was generated
     */

    public ItemList generateLoot()
    {
        if (loot == null) return new ItemList();

        return loot.generate();
    }

    /**
     * Returns true if this container is a workbench (can be used for crafting recipes)
     * or false otherwise
     *
     * @return whether this is a workbench
     */

    public boolean isWorkbench()
    {
        return isWorkbench;
    }

    /**
     * Returns true if and only if this container is a temporary container used for storing
     * items that were dropped directly in the area.  Temporary containers are automatically
     * removed when they no longer hold any items
     *
     * @return whether this is a temporary container
     */

    public boolean isTemporary()
    {
        return getID().equals(Game.ruleset.getString("TemporaryContainerID"));
    }

}
