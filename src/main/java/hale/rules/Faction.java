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

package hale.rules;

import java.util.HashMap;
import java.util.Map;

import hale.Game;
import hale.entity.Entity;
import hale.loading.JSONOrderedObject;
import hale.loading.Saveable;
import hale.util.SimpleJSONObject;

/**
 * A faction is a group of creatures that work together in game.  Factions are either Friendly,
 * Neutral, or Hostile towards each other
 *
 * @author Jared
 */

public class Faction
{
    public enum Relationship
    {
        Friendly, Neutral, Hostile
    }

    ;

    private String name;
    private Map<String, Relationship> relationships;

    /**
     * Creates a new Faction with the specified identifying name
     *
     * @param name
     */

    public Faction(String name)
    {
        this.name = name;
        this.relationships = new HashMap<String, Relationship>();
    }

    public boolean isFriendly(Entity other)
    {
        return (getRelationship(other) == Relationship.Friendly);
    }

    public boolean isHostile(Entity other)
    {
        return (getRelationship(other) == Relationship.Hostile);
    }

    public Relationship getRelationship(Entity other)
    {
        if (other == null) return null;

        Faction f = other.getFaction();

        if (f == null) return Relationship.Neutral;

        return getRelationship(f);
    }

    public Relationship getRelationship(String otherName)
    {
        Relationship relationship = relationships.get(otherName);

        if (relationship == null) {
            return Relationship.Neutral;
        } else {
            return relationship;
        }
    }

    public Relationship getRelationship(Faction other)
    {
        Relationship relationship = relationships.get(other.getName());

        if (relationship == null) {
            return Relationship.Neutral;
        } else {
            return relationship;
        }
    }

    public void setRelationship(Faction other, Relationship relationship)
    {
        relationships.put(other.getName(), relationship);
    }

    public void setRelationship(String otherName, Relationship relationship)
    {
        relationships.put(otherName, relationship);
    }

    public String getName()
    {
        return name;
    }

    /**
     * A custom relationship is a modification to the default faction relationships
     * that has been set during the course of a game via script.  This will be saved
     * as part of a campaign's save data
     *
     * @author Jared
     */

    public static class CustomRelationship implements Saveable
    {
        public final String faction1;
        public final String faction2;
        public final String relationship;

        public CustomRelationship(String faction1, String faction2, String relationship)
        {
            this.faction1 = faction1;
            this.faction2 = faction2;
            this.relationship = relationship;
        }

        @Override
        public Object save()
        {
            JSONOrderedObject data = new JSONOrderedObject();

            data.put("faction1", faction1);
            data.put("faction2", faction2);
            data.put("relationship", relationship);

            return data;
        }

        /**
         * Sets the relationship between the two factions as specified by this CustomRelationship
         */

        public void setFactionRelationships()
        {
            Faction f1 = Game.ruleset.getFaction(faction1);
            Faction f2 = Game.ruleset.getFaction(faction2);

            Faction.Relationship relation = Faction.Relationship.valueOf(relationship);

            f1.setRelationship(f2, relation);
            f2.setRelationship(f1, relation);
        }

        public static CustomRelationship load(SimpleJSONObject data)
        {
            String faction1 = data.get("faction1", null);
            String faction2 = data.get("faction2", null);
            String relationship = data.get("relationship", null);

            return new CustomRelationship(faction1, faction2, relationship);
        }
    }
}
