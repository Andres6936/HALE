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

package main.java.hale;

import java.io.File;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import main.java.hale.loading.JSONOrderedObject;
import main.java.hale.loading.SaveWriter;
import main.java.hale.resource.ResourceType;
import main.java.hale.util.SimpleJSONArray;
import main.java.hale.util.SimpleJSONArrayEntry;
import main.java.hale.util.SimpleJSONParser;
import main.java.hale.util.Logger;

/**
 * A representation of a party that has been saved to a file on disk, for use in
 * building a party when starting a campaign
 *
 * @author Jared
 */

public class SavedParty
{
    private final List<String> characters;

    private final int currencyInCP;
    private final int maxLevel, minLevel;

    private final String id;
    private final String name;
    private final String resourceLocation;

    /**
     * creates a SavedParty object by reading in the resource at the location
     * specified by the ID
     *
     * @param resourceLocation
     * @param id               the ID string representing the resource location of the savedParty
     */

    public SavedParty(String resourceLocation, String id)
    {
        SimpleJSONParser parser = new SimpleJSONParser(new File(resourceLocation));

        this.resourceLocation = resourceLocation;
        this.id = id;
        this.name = parser.get("name", id);
        this.currencyInCP = parser.get("currency", 0);
        this.maxLevel = parser.get("maxLevel", 0);
        this.minLevel = parser.get("minLevel", 0);

        characters = new ArrayList<String>();

        SimpleJSONArray charsArray = parser.getArray("characters");
        for (SimpleJSONArrayEntry entry : charsArray) {
            characters.add(entry.getString());
        }

        parser.warnOnUnusedKeys();
    }

    /**
     * Creates a new SavedParty with the specified data
     *
     * @param characters   the characters in the party
     * @param name         the name of the party, which is also used to generate a unique ID string
     * @param minLevel     the minimum of the levels of the characters in the party
     * @param maxLevel     the maximum of the levels of the characters in the party
     * @param currencyInCP the amount of currency possessed by the party in CP
     */

    public SavedParty(List<String> characters, String name, int minLevel, int maxLevel, int currencyInCP)
    {
        this.name = name;
        this.currencyInCP = currencyInCP;
        this.maxLevel = maxLevel;
        this.minLevel = minLevel;
        this.characters = new ArrayList<String>(characters);

        // get unique ID String
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
        this.id = name + "-" + format.format(Calendar.getInstance().getTime());
        this.resourceLocation = Game.plataform.getPartiesDirectory() + this.id + ResourceType.JSON.getExtension();
    }

    /**
     * Returns the number of members in this party
     *
     * @return the number of members in this party
     */

    public int size()
    {
        return characters.size();
    }

    /**
     * Returns the list of the IDs of all characters in this party
     *
     * @return the list of character IDs
     */

    public List<String> getCharacterIDs()
    {
        return Collections.unmodifiableList(characters);
    }

    /**
     * Returns the unique ID string for this party
     *
     * @return the unique ID string
     */

    public String getID()
    {
        return id;
    }

    /**
     * Returns the user defined name for this party
     *
     * @return the user defined name
     */

    public String getName()
    {
        return name;
    }

    /**
     * Returns the amount of currency currently owned by this party, in units of CP over 100
     *
     * @return the amount of currency owned by the party
     */

    public int getCurrency()
    {
        return currencyInCP;
    }

    /**
     * Returns the level of the lowest level party member
     *
     * @return the lowest level party member level
     */

    public int getMinLevel()
    {
        return minLevel;
    }

    /**
     * Returns the level of the highest level party member
     *
     * @return the party member level
     */

    public int getMaxLevel()
    {
        return maxLevel;
    }

    /**
     * Writes the contents of this SavedParty to a file in the resource location
     */

    public void writeToFile()
    {
        File file = new File(resourceLocation);

        // build the JSON representation
        JSONOrderedObject obj = new JSONOrderedObject();
        obj.put("name", name);
        obj.put("currency", currencyInCP);
        obj.put("maxLevel", maxLevel);
        obj.put("minLevel", minLevel);

        List<Object> chars = new ArrayList<Object>();
        for (String character : characters) {
            chars.add(character);
        }
        obj.put("characters", chars.toArray());

        try {
            file.createNewFile();

            PrintWriter out = new PrintWriter(file);

            SaveWriter.writeJSON(obj, out);

            out.close();

        } catch (Exception e) {
            Logger.appendToErrorLog("Error writing party", e);
        }
    }

    /**
     * Deletes the file on disk corresponding to this party
     */

    public void deleteFile()
    {
        File file = new File(resourceLocation);

        file.delete();
    }
}
