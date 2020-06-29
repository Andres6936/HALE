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

package main.java.hale.util;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

/**
 * A wrapper for a JSON array entry.  Can potentially wrap an object
 *
 * @author Jared Stephen
 */

public class SimpleJSONArrayEntry
{
    private String id;
    private Object data;

    private boolean warnOnMissingKeys;

    private boolean entryRead;

    // non null if this is wrapping an object not a primitive type
    private SimpleJSONObject objData;
    private SimpleJSONArray arrayData;

    /**
     * Creates a new ArrayEntry with the specified data and id
     *
     * @param data the object data - can be a JSONObject, Boolean, Integer, or Double
     * @param id   the id for this array entry
     */

    public SimpleJSONArrayEntry(Object data, String id)
    {
        this.id = id;
        this.data = data;
        this.entryRead = false;
    }

    /**
     * Returns the ID of this entry
     *
     * @return the entry ID
     */

    public String getID()
    {
        return id;
    }

    /**
     * Sets whether this Object and all sub objects will warn when a key is requested but not
     * found.
     *
     * @param warn whether to warn on missing keys
     */

    protected void setWarnOnMissingKeys(boolean warn)
    {
        this.warnOnMissingKeys = warn;

        if (objData != null) objData.setWarnOnMissingKeys(warn);

        if (arrayData != null) arrayData.setWarnOnMissingKeys(warn);
    }

    /**
     * Checks all keys in this object and all sub objects and logs a warning
     * for any keys that have not been read
     */

    protected void warnOnUnusedKeys()
    {
        if (!entryRead) {
            Logger.appendToWarningLog(id + " is an unused key.");
        } else
            if (objData != null) {
                objData.warnOnUnusedKeys();
            } else
                if (arrayData != null) {
                    arrayData.warnOnUnusedKeys();
                }
    }

    /**
     * Returns true if and only if this ArrayEntry is wrapping a boolean
     *
     * @return true if and only if this ArrayEntry is wrapping a boolean
     */

    public boolean isBoolean()
    {
        return data instanceof Boolean;
    }

    /**
     * Returns true if and only if this ArrayEntry is wrapping an int
     *
     * @return true if and only if this ArrayEntry is wrapping an int
     */

    public boolean isInteger()
    {
        return data instanceof Integer;
    }

    /**
     * Returns true if and only if this ArrayEntry is wrapping a float or double
     *
     * @return true if and only if this ArrayEntry is wrapping a float or double
     */

    public boolean isFloat()
    {
        return data instanceof Double;
    }

    /**
     * Returns true if this ArrayEntry is an Object, false if it is wrapping a primitive
     * or an array
     *
     * @return true if this ArrayEntry is an Object
     */

    public boolean isObject()
    {
        return data instanceof JSONObject;
    }

    /**
     * Returns true if this ArrayEntry is an Array, false otherwise
     *
     * @return true if this ArrayEntry is an Array
     */

    public boolean isArray()
    {
        return data instanceof JSONArray;
    }

    /**
     * Returns true if this ArrayEntry is a String, false if it is wrapping otherwise
     *
     * @return true if this ArrayEntry is a String
     */

    public boolean isString()
    {
        return data instanceof String;
    }

    /**
     * Returns the String for this array entry, or null if the entry is not a String
     *
     * @return the String for this entry
     */

    public String getString()
    {
        if (data instanceof String) {
            entryRead = true;

            return (String)data;
        } else {
            Logger.appendToWarningLog("Array Entry \"" + id + "\" is not a string.");
            return null;
        }
    }

    /**
     * Gets the JSONArray wrapper for this Array entry, or null if the entry is
     * not a JSONArray (Object, Boolean, Integer, or Double)
     *
     * @return the JSONArray wrapper for this Array entry
     */

    public SimpleJSONArray getArray()
    {
        if (data instanceof JSONArray) {
            entryRead = true;

            arrayData = new SimpleJSONArray((JSONArray)data, id);
            if (warnOnMissingKeys) arrayData.setWarnOnMissingKeys(warnOnMissingKeys);
            return arrayData;
        } else {
            Logger.appendToWarningLog("Array Entry \"" + id + "\" is not an array.");
            return null;
        }
    }

    /**
     * Gets the JSONObject wrapper for this Array entry, or null if the entry is
     * not a JSONObject (Array, Boolean, Integer, or Double)
     *
     * @return the JSONObject wrapper for this Array entry
     */

    public SimpleJSONObject getObject()
    {
        if (data instanceof JSONObject) {
            entryRead = true;

            objData = new SimpleJSONObject((JSONObject)data, id);
            if (warnOnMissingKeys) objData.setWarnOnMissingKeys(warnOnMissingKeys);
            return objData;
        } else {
            Logger.appendToWarningLog("Array Entry \"" + id + "\" is not an object.");
            return null;
        }
    }

    /**
     * Gets the integer value for this Array entry, or the default value if this
     * entry is not wrapping an int
     *
     * @param defaultValue the default value
     * @return the int value for this Array entry
     */

    public int getInt(int defaultValue)
    {
        if (data instanceof Integer) {
            entryRead = true;

            return ((Integer)data).intValue();
        } else {
            Logger.appendToWarningLog("Array entry \"" + id + "\" is not an int.");
            return defaultValue;
        }
    }

    /**
     * Gets the boolean value for this Array entry, or the default value if this
     * entry is not wrapping a boolean
     *
     * @param defaultValue the default value
     * @return the boolean value for this Array entry
     */

    public boolean getBoolean(boolean defaultValue)
    {
        if (data instanceof Boolean) {
            entryRead = true;

            return ((Boolean)data).booleanValue();
        } else {
            Logger.appendToWarningLog("Array entry \"" + id + "\" is not a boolean.");
            return defaultValue;
        }
    }

    /**
     * Gets the float value for this Array entry, or the default value if this
     * entry is not wrapping a float
     *
     * @param defaultValue the default value
     * @return the float value for this Array entry
     */

    public float getFloat(float defaultValue)
    {
        if (data instanceof Double) {
            entryRead = true;

            return ((Double)data).floatValue();
        } else {
            Logger.appendToWarningLog("Array entry \"" + id + "\" is not a float.");
            return defaultValue;
        }
    }
}
