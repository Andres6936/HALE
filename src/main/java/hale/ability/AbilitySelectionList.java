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

package main.java.hale.ability;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import main.java.hale.Game;
import main.java.hale.util.Pointf;
import main.java.hale.util.SimpleJSONArray;
import main.java.hale.util.SimpleJSONArrayEntry;
import main.java.hale.util.SimpleJSONObject;
import main.java.hale.util.SimpleJSONParser;

/**
 * An AbilitySelectionList provides a means for choosing one of a specific subset
 * of Abilities when increasing in role level.  At some levels, a role can specify
 * that the character can pick any ability from a given list of Abilities.  The
 * chosen Ability will always be subject to any prereqs.
 * <p>
 * An AbilitySelectionList is immutable once created.
 *
 * @author Jared Stephen
 */

public class AbilitySelectionList
{
    private final String id;
    private final String name;

    private final Map<Ability, Pointf> abilities;

    private final Map<String, Pointf> subLists;

    private List<Connector> connectors;

    /**
     * Create a new AbilitySelectionList with the specified id and using the resource
     * at the specified resourcePath to determine the name and contents of the List
     *
     * @param id           the ID String for this List
     * @param resourcePath the location of the Resource to read for this List
     */

    public AbilitySelectionList(String id, String resourcePath)
    {
        this.id = id;

        SimpleJSONParser parser = new SimpleJSONParser(resourcePath);

        this.name = parser.get("name", null);

        if (parser.containsKey("subLists")) {
            this.subLists = new HashMap<String, Pointf>();
            SimpleJSONObject subListsIn = parser.getObject("subLists");

            for (String listID : subListsIn.keySet()) {
                SimpleJSONObject subListIn = subListsIn.getObject(listID);

                this.subLists.put(listID, new Pointf(subListIn.get("x", 0.0f), subListIn.get("y", 0.0f)));
            }

        } else {
            this.subLists = Collections.emptyMap();
        }

        if (parser.containsKey("abilities")) {
            this.abilities = new HashMap<Ability, Pointf>();

            SimpleJSONObject abilitiesIn = parser.getObject("abilities");

            for (String abilityID : abilitiesIn.keySet()) {
                Ability ability = Game.ruleset.getAbility(abilityID);
                if (ability == null) {
                    throw new IllegalArgumentException("Ability " + abilityID + " not found in selection list " + id);
                }

                SimpleJSONObject abilityIn = abilitiesIn.getObject(abilityID);

                abilities.put(ability, new Pointf(abilityIn.get("x", 0.0f), abilityIn.get("y", 0.0f)));
            }

        } else {
            this.abilities = Collections.emptyMap();
        }

        if (parser.containsKey("connectors")) {
            this.connectors = new ArrayList<Connector>();

            SimpleJSONArray connectorsIn = parser.getArray("connectors");
            for (SimpleJSONArrayEntry entryIn : connectorsIn) {
                SimpleJSONObject connectorIn = entryIn.getObject();

                ConnectorType type = ConnectorType.valueOf(connectorIn.get("type", null));
                this.connectors.add(new Connector(connectorIn.get("x", 0.0f), connectorIn.get("y", 0.0f), type));
            }
        } else {
            this.connectors = Collections.emptyList();
        }

        parser.warnOnUnusedKeys();
    }

    /**
     * Returns the ID String for this AbilitySelectionList
     *
     * @return the ID String for this AbilitySelectionList
     */

    public String getID()
    {
        return id;
    }

    /**
     * Returns the descriptive name for this AbilitySelectionList
     *
     * @return the descriptive name for this AbilitySelectionList
     */

    public String getName()
    {
        return name;
    }

    /**
     * Returns true if this AbilitySelectionList contains the specified ability,
     * false otherwise
     *
     * @param ability the Ability to check for
     * @return true if this List contains ability, false otherwise
     */

    public boolean contains(Ability ability)
    {
        return abilities.containsKey(ability);
    }

    /**
     * Returns all Abilities contained directly in this AbilitySelectionList.
     * The returned set does not contain Abilities contained in SubLists of this
     * AbilitySelectionList.  The returned set is unmodifiable
     *
     * @return the set of abilities contained in this AbilitySelectionList.
     */

    public Set<Ability> getAbilities()
    {
        return Collections.unmodifiableSet(abilities.keySet());
    }

    /**
     * Returns the grid layout position of the ability within this
     * specified AbilitySelectionList.  This is used in determining the
     * placement of the Ability on the screen when displaying this List
     *
     * @param ability the Ability to find
     * @return the position of the Ability in the grid
     */

    public Pointf getGridPosition(Ability ability)
    {
        return abilities.get(ability);
    }

    /**
     * Returns the Set of all SubLists contained in this AbilitySelectionList.
     * <p>
     * Note that the returned set is unmodifiable.
     *
     * @return the list of all Regions in this AbilitySelectionList
     */

    public Set<String> getSubListIDs()
    {
        return Collections.unmodifiableSet(subLists.keySet());
    }

    /**
     * Returns the List of all connectors contained within this AbilitySelectionList.
     * Note that the returned list is unmodifiable.
     *
     * @return the List of all connectors contained within this AbilitySelectionList
     */

    public List<Connector> getConnectors()
    {
        return Collections.unmodifiableList(connectors);
    }

    /**
     * Returns the grid position for the specified AbilitySelectionList referenced
     * as a sublist of this AbilitySelectionList.  If the specified list is not
     * as sublist of this List, will throw an IllegalArgumentException
     *
     * @param list the sublist of this list to get the position of
     */

    public Pointf getGridPosition(AbilitySelectionList list)
    {
        if (!subLists.containsKey(list.id)) {
            throw new IllegalArgumentException("SubList " + list.id + " not contained in this list.");
        }

        return subLists.get(list.id);
    }

    /**
     * The type for a connector
     *
     * @author Jared Stephen
     */

    public enum ConnectorType
    {
        /**
         * Connects two grid points, referenced from the bottom one
         */
        OneUp,

        /**
         * Connects two grid points, referenced from the top one
         */
        OneDown,

        /**
         * Connects one lower grid point to two upper ones
         */
        TwoUp,

        /**
         * Connects one upper grid point to two lower ones
         */
        TwoDown,

        /**
         * Connects one lower grid point to three upper ones
         */
        ThreeUp,

        /**
         * Connects one upper grid point to three lower ones
         */
        ThreeDown,

        /**
         * Connects one lower grid point to four upper ones
         */
        FourUp,

        /**
         * Connects one upper grid point to four lower ones
         */
        FourDown,

        /**
         * Connects vertically all the way through a grid position
         */
        Through
    }

    /**
     * A connector contains the data used to generate a widget connecting
     * 2 AbilitySelectorButtons when viewed in the CharacterBuilder.  This is used
     * to provide a visual cue to the ability trees.  Connectors are specified with
     * the "connect" key in the AbilitySelectionList data file.
     * <p>
     * Connectors are immutable.
     *
     * @author Jared Stephen
     */

    public class Connector
    {
        private final Pointf point;
        private final ConnectorType type;

        private Connector(float p1x, float p1y, ConnectorType type)
        {
            this.point = new Pointf(p1x, p1y);
            this.type = type;
        }

        /**
         * Returns the grid position that this connector references
         *
         * @return the grid position that this connector references
         */

        public Pointf getPoint()
        {
            return point;
        }

        /**
         * Returns the connector type for this Connector
         *
         * @return the connector type
         */

        public ConnectorType getType()
        {
            return type;
        }
    }
}
