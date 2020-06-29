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

package main.java.hale.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import main.java.hale.Game;
import main.java.hale.rules.Faction;
import main.java.hale.util.AreaUtil;
import main.java.hale.util.Point;
import main.java.hale.util.PointImmutable;
import main.java.hale.util.SimpleJSONArrayEntry;
import main.java.hale.util.SimpleJSONObject;
import main.java.hale.util.SimpleJSONParser;

/**
 * An encounter consists of a group of creatures, generally located
 * near to each other within a given area
 *
 * @author Jared
 */

public class EncounterTemplate
{

    private class Entry
    {
        private final String creatureID;
        private final PointImmutable position;

        private Entry(String creatureID, int x, int y)
        {
            this.creatureID = creatureID;
            this.position = new PointImmutable(x, y);
        }
    }

    private final String id;

    private final List<Entry> creatures;

    private final int minRandomCreatures, maxRandomCreatures;

    private final int respawnTimeInHours;

    private final int challengeRating;

    private final int radius;

    private final Faction faction;

    /**
     * Creates a new EncounterTemplate
     *
     * @param id     the encounter ID
     * @param parser the JSON data to parse
     */

    public EncounterTemplate(String id, SimpleJSONParser parser)
    {
        this.id = id;

        if (parser.containsKey("respawnTime")) {
            respawnTimeInHours = parser.get("respawnTime", 0);
        } else {
            respawnTimeInHours = -1;
        }

        radius = parser.get("radius", 0);

        String factionID = parser.get("faction", null);
        this.faction = Game.ruleset.getFaction(factionID);

        if (parser.containsKey("minRandomCreatures")) {
            minRandomCreatures = parser.get("minRandomCreatures", 0);
            maxRandomCreatures = parser.get("maxRandomCreatures", 0);
        } else {
            minRandomCreatures = -1;
            maxRandomCreatures = -1;
        }

        creatures = new ArrayList<Entry>();
        for (SimpleJSONArrayEntry entry : parser.getArray("creatures")) {
            SimpleJSONObject object = entry.getObject();

            String creatureID = object.get("id", null);
            int x = 0, y = 0;

            if (object.containsKey("x")) {
                x = object.get("x", 0);
                y = object.get("y", 0);
            }

            creatures.add(new Entry(creatureID, x, y));
        }

        if (parser.containsKey("challengeRating")) {
            challengeRating = parser.get("challengeRating", 0);
        } else {
            // if parser does not contain challenge rating, one will computed dynamically
            // when spawning
            challengeRating = -1;
        }
    }

    /**
     * Returns the Encounter ID of this template
     *
     * @return the Encounter ID
     */

    public String getID()
    {
        return id;
    }

    /**
     * Returns the radius of this encounter, in hexes.  Creatures will
     * spawn within this radius
     *
     * @return the radius of this encounter
     */

    public int getRadius()
    {
        return radius;
    }

    /**
     * Returns true if this encounter generates creatures randomly from its
     * list when spawning, false if it generates fixed creatures at fixed
     * positions
     *
     * @return whether this encounter generates random creatures
     */

    public boolean randomizesCreatures()
    {
        return minRandomCreatures != -1;
    }

    /**
     * Gets the minimum number of creatures that this encounter can spawn randomly,
     * or -1 if this encounter spawns a fixed set of creatures
     *
     * @return the minimum number of creatures to randomly spawn
     */

    public int getMinRandomCreatures()
    {
        return minRandomCreatures;
    }

    /**
     * Gets the maximum number of creatures that this encounter can spawn randomly,
     * or -1 if this encounter spawns a fixed set of creatures
     *
     * @return the maximum number of creatures to randomly spawn
     */

    public int getMaxRandomCreatures()
    {
        return maxRandomCreatures;
    }

    /**
     * Returns true if this encounter will respawn every certain number of
     * hours, false if it will spawn only once
     *
     * @return whether this encounter respawns
     */

    public boolean respawns()
    {
        return respawnTimeInHours != -1;
    }

    /**
     * Returns the minimum number of hours between this Encounter respawning,
     * or -1 if this encounter does not respawn
     *
     * @return the minimum number of hours between this Encounter respawning
     */

    public int getRespawnTimeInHours()
    {
        return respawnTimeInHours;
    }

    /**
     * Returns the challenge rating of this Encounter.  The rule of thumb
     * is a challenge rating of 10 points times the party level is moderately
     * difficult.  (This is defined as "EncounterChallengeFactor" in rules.json)
     *
     * @return the challenge rating
     */

    public int getChallengeRating()
    {
        return challengeRating;
    }

    /**
     * Gets the faction of this Encounter.  All creatures will be spawned
     * as this faction
     *
     * @return the default faction of this Encounter
     */

    public Faction getDefaultFaction()
    {
        return faction;
    }

    /**
     * Creates the set of creatures that can be added to the area by a specific
     * encounter.  Each creature is mapped by its location within the area.  If
     * this template randomizes its spawn, then the creatures are drawn randomly
     * from the list of available creatures.  Otherwise, the fixed positions and
     * creatures in the data file are used.
     *
     * @param location the area and coordinates to spawn at
     * @return A map, with the keys consisting of locations within the area,
     * and the values consisting of the creatures being spawned
     */

    public Map<PointImmutable, NPC> spawnCreatures(Location location)
    {
        Map<PointImmutable, NPC> spawnedCreatures = new HashMap<PointImmutable, NPC>();

        if (randomizesCreatures() && Game.scriptInterface.SpawnRandomEncounters) {
            // spawn random unless it has been disabled
            int numToSpawn = Game.dice.rand(minRandomCreatures, maxRandomCreatures);

            for (int i = 0; i < numToSpawn; i++) {
                // pick a random entry
                Entry entry = creatures.get(Game.dice.rand(0, creatures.size() - 1));

                spawnedCreatures.put(getFreeRandomLocation(spawnedCreatures, location),
                        EntityManager.getNPC(entry.creatureID));
            }

        } else {
            // spawn fixed
            for (Entry entry : this.creatures) {
                // convert relative to absolute area coordinates
                int x = location.getX() + entry.position.x;
                int y = location.getY() + entry.position.y;

                if (entry.position.x % 2 != 0 && location.getX() % 2 != 0) {
                    y += 1;
                }

                spawnedCreatures.put(new PointImmutable(x, y), EntityManager.getNPC(entry.creatureID));
            }
        }

        return spawnedCreatures;
    }

    private PointImmutable getFreeRandomLocation(Map<PointImmutable, NPC> spawnedCreatures, Location location)
    {
        // give up eventually if we can't find a spot
        for (int count = 0; count < 100; count++) {
            int r = Game.dice.rand(0, this.radius);
            int i = Game.dice.rand(0, r * 6);

            Point spawnPoint = AreaUtil.convertPolarToGrid(location.toPoint(), r, i);

            // check the area
            if (!location.getArea().isFreeForCreature(spawnPoint.x, spawnPoint.y)) {
                continue;
            }

            boolean spawnPointIsFree = true;
            // check the already placed creatures
            for (PointImmutable occupiedPoint : spawnedCreatures.keySet()) {
                if (occupiedPoint.x == spawnPoint.x && occupiedPoint.y == spawnPoint.y) {
                    spawnPointIsFree = false;
                    break;
                }
            }

            if (spawnPointIsFree) {
                return new PointImmutable(spawnPoint.x, spawnPoint.y);
            }
        }

        return null;
    }
}
