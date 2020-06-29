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

package main.java.hale.entity;

import main.java.hale.area.Area;
import main.java.hale.loading.JSONOrderedObject;
import main.java.hale.loading.LoadGameException;
import main.java.hale.loading.ReferenceHandler;
import main.java.hale.rules.Damage;
import main.java.hale.util.SimpleJSONObject;
import main.java.hale.util.SimpleJSONParser;

/**
 * Class for all non player characters (NPCs).  NPCs are not controllable
 * by the player.  They can be hostile or friendly.
 *
 * @author Jared
 */

public final class NPC extends Creature
{
    private final NPCTemplate template;

    @Override
    public void load(SimpleJSONObject data, Area area, ReferenceHandler refHandler) throws LoadGameException
    {
        super.load(data, area, refHandler);
    }

    @Override
    public JSONOrderedObject save()
    {
        JSONOrderedObject out = super.save();

        return out;
    }

    /**
     * Creates a new NPC by parsing the specified JSON.  The template
     * has already been defined, and then additional data is read from the JSON
     * to fully define the NPC
     *
     * @param template
     * @param parser
     */

    public NPC(NPCTemplate template, SimpleJSONParser parser)
    {
        super(template, parser);
        this.template = template;
    }

    /**
     * Creates a new copy of the specified NPC.  Permanent creature data such as
     * stats, inventory, roles, skills, and abilities are copied.  No other data is copied,
     * however
     *
     * @param other the creature to copy
     */

    public NPC(NPC other)
    {
        super(other);
        this.template = other.template;
    }

    /**
     * Creates a new NPC from the specified template
     *
     * @param template the template containing the immutable parts of the NPC definition
     */

    public NPC(NPCTemplate template)
    {
        super(template);
        this.template = template;
    }

    @Override
    public NPCTemplate getTemplate()
    {
        return template;
    }

    @Override
    public void takeDamage(Damage damage)
    {
        if (!template.isImmortal()) {
            super.takeDamage(damage);
        }
    }

    @Override
    public boolean isDead()
    {
        // only objects do not have any roles, and objects are neither living
        // nor dead
        if (roles.getBaseRole() == null) return false;

        return getCurrentHitPoints() <= 0;
    }
}
