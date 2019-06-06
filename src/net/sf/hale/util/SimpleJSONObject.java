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

package net.sf.hale.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

/**
 * A simple wrapper around a JSONObject allowing easy access to the different types
 * of data that can be stored in the JSONObject
 *
 * @author Jared Stephen
 */

public class SimpleJSONObject
{
    private static SimpleJSONArray emptyArray = new SimpleJSONArray( );

    private boolean warnOnMissingKeys;

    private String objectID;
    private JSONObject object;

    private Set< String > usedKeys;

    // used to validate unused keys to report warnings
    private List< SimpleJSONObject > subObjects;
    private List< SimpleJSONArray > subArrays;

    /**
     * Creates a new SimpleJSONObject wrapping the specified JSONObject
     *
     * @param object   the object to wrap
     * @param objectID the key based on the object this maps to at the parent level
     */

    public SimpleJSONObject( JSONObject object, String objectID )
    {
        this.object = object;
        this.objectID = objectID;
        this.warnOnMissingKeys = false;

        this.subObjects = new ArrayList< SimpleJSONObject >( 1 );
        this.subArrays = new ArrayList< SimpleJSONArray >( 1 );
        this.usedKeys = new HashSet< String >( );
    }

    /**
     * Returns an ID string which describes the location of this object
     *
     * @return the ID string
     */

    public String getObjectID( )
    {
        return objectID;
    }

    /**
     * Sets whether this Object and all sub objects will warn when a key is requested but not
     * found.
     *
     * @param warn whether to warn on missing keys
     */

    public void setWarnOnMissingKeys( boolean warn )
    {
        this.warnOnMissingKeys = warn;

        for ( SimpleJSONObject subObject : subObjects )
        {
            subObject.setWarnOnMissingKeys( warn );
        }

        for ( SimpleJSONArray subArray : subArrays )
        {
            subArray.setWarnOnMissingKeys( warn );
        }
    }

    /**
     * Checks all keys in this object and all sub objects and logs a warning
     * for any keys that have not been read
     */

    protected void warnOnUnusedKeys( )
    {
        for ( String key : object.keySet( ) )
        {
            if ( usedKeys.contains( key ) ) continue;

            // ignore comment keys
            if ( key.equals( "comment" ) ) continue;

            Logger.appendToWarningLog( "In " + objectID + " unused key \"" + key + "\"" );
        }

        for ( SimpleJSONObject subObject : subObjects )
        {
            subObject.warnOnUnusedKeys( );
        }

        for ( SimpleJSONArray subArray : subArrays )
        {
            subArray.warnOnUnusedKeys( );
        }
    }

    /**
     * Returns true if and only if the specified key exists and points to a String
     *
     * @param key the key to check
     * @return true if the key's value is a string
     */

    public boolean isString( String key )
    {
        Object obj = object.get( key );

        if ( obj == null )
        {
            if ( warnOnMissingKeys )
            { Logger.appendToWarningLog( "In resource " + objectID + ": \"" + key + "\" is missing" ); }
            return false;
        }
        else
        {
            return obj instanceof String;
        }
    }

    /**
     * Returns true if and only if the specified key exists and points to an int
     *
     * @param key the key to check
     * @return true if the key's value is an int
     */

    public boolean isInteger( String key )
    {
        Object obj = object.get( key );

        if ( obj == null )
        {
            if ( warnOnMissingKeys )
            { Logger.appendToWarningLog( "In resource " + objectID + ": \"" + key + "\" is missing" ); }
            return false;
        }
        else
        {
            return obj instanceof Integer;
        }
    }

    /**
     * Returns true if and only if the specified key exists and points to a float or double
     *
     * @param key the key to check
     * @return true if the key's value is a float
     */

    public boolean isFloat( String key )
    {
        Object obj = object.get( key );

        if ( obj == null )
        {
            if ( warnOnMissingKeys )
            { Logger.appendToWarningLog( "In resource " + objectID + ": \"" + key + "\" is missing" ); }
            return false;
        }
        else
        {
            return obj instanceof Double;
        }
    }

    /**
     * Returns true if and only if the specified key exists and points to a boolean
     *
     * @param key the key to check
     * @return true if the key's value is a boolean
     */

    public boolean isBoolean( String key )
    {
        Object obj = object.get( key );

        if ( obj == null )
        {
            if ( warnOnMissingKeys )
            { Logger.appendToWarningLog( "In resource " + objectID + ": \"" + key + "\" is missing" ); }
            return false;
        }
        else
        {
            return obj instanceof Boolean;
        }
    }

    /**
     * Returns the string value specified by the key or the defaultValue if the resource that
     * was parsed does not contain the specified key or the key does not point to a String
     *
     * @param key          the key for the value
     * @param defaultValue the default value to return if the key is not found
     * @return the value for the specified key
     */

    public String get( String key, String defaultValue )
    {
        Object obj = object.get( key );

        if ( obj == null )
        {
            if ( warnOnMissingKeys )
            { Logger.appendToWarningLog( "In resource " + objectID + ": \"" + key + "\" is missing" ); }
            return defaultValue;
        }
        else if ( obj instanceof String )
        {
            usedKeys.add( key );
            return ( String ) object.get( key );
        }
        else
        {
            Logger.appendToWarningLog( "In resource " + objectID + ": \"" + key + "\" is not a String." );
            return defaultValue;
        }
    }

    /**
     * Returns the value specified by the key or the defaultValue if the resource that
     * was parsed does not contain the specified key or the key does not point to an Integer
     *
     * @param key          the key for the value
     * @param defaultValue the default value to return if the key is not found
     * @return the value for the specified key
     */

    public int get( String key, int defaultValue )
    {
        Object obj = object.get( key );

        if ( obj == null )
        {
            if ( warnOnMissingKeys )
            { Logger.appendToWarningLog( "In resource " + objectID + ": \"" + key + "\" is missing" ); }
            return defaultValue;
        }
        else if ( obj instanceof Integer )
        {
            usedKeys.add( key );
            return ( ( Integer ) obj ).intValue( );
        }
        else
        {
            Logger.appendToWarningLog( "In resource " + objectID + ": \"" + key + "\" is not an int." );
            return defaultValue;
        }
    }

    /**
     * Returns the boolean value specified by the key or the default value if the key does
     * not exist or points to a non boolean value.
     *
     * @param key          the key for the value
     * @param defaultValue the default value to return if the key is not found
     * @return the value for the specified key
     */

    public boolean get( String key, boolean defaultValue )
    {
        Object obj = object.get( key );

        if ( obj == null )
        {
            if ( warnOnMissingKeys )
            { Logger.appendToWarningLog( "In resource " + objectID + ": \"" + key + "\" is missing" ); }
            return defaultValue;
        }
        else if ( obj instanceof Boolean )
        {
            usedKeys.add( key );
            return ( ( Boolean ) obj ).booleanValue( );
        }
        else
        {
            Logger.appendToWarningLog( "In resource " + objectID + ": \"" + key + "\" is not a boolean." );
            return defaultValue;
        }
    }

    /**
     * Returns the float value specified by the key or the default value if the key does
     * not exist or points to a non floating point value.
     *
     * @param key          the key for the value
     * @param defaultValue the default value to return if the key is not found
     * @return the value for the specified key
     */

    public float get( String key, float defaultValue )
    {
        Object obj = object.get( key );

        if ( obj == null )
        {
            if ( warnOnMissingKeys )
            { Logger.appendToWarningLog( "In resource " + objectID + ": \"" + key + "\" is missing" ); }
            return defaultValue;
        }
        else if ( obj instanceof Double )
        {
            usedKeys.add( key );
            return ( ( Double ) obj ).floatValue( );
        }
        else if ( obj instanceof BigDecimal )
        {
            usedKeys.add( key );
            return ( ( BigDecimal ) obj ).floatValue( );
        }
        else
        {
            Logger.appendToWarningLog( "In resource " + objectID + ": \"" + key + "\" is not a float, is " +
                                               obj.getClass( ).getCanonicalName( ) );
            return defaultValue;
        }
    }

    /**
     * Returns the array corresponding to the specified key.  If there is no
     * such key in the parsed data, or the key points to a non-array object,
     * returns an empty array
     *
     * @param key the key for the array
     * @return the array corresponding to the specified key
     */

    public SimpleJSONArray getArray( String key )
    {
        Object obj = object.get( key );

        if ( obj == null )
        {
            if ( warnOnMissingKeys )
            { Logger.appendToWarningLog( "In resource " + objectID + ": \"" + key + "\" is missing" ); }
            return emptyArray;
        }
        else if ( obj instanceof JSONArray )
        {
            usedKeys.add( key );

            StringBuilder id = new StringBuilder( );
            id.append( objectID );
            id.append( " -> " );
            id.append( key );

            SimpleJSONArray returnVal = new SimpleJSONArray( ( JSONArray ) obj, id.toString( ) );

            if ( warnOnMissingKeys )
            { returnVal.setWarnOnMissingKeys( warnOnMissingKeys ); }

            subArrays.add( returnVal );
            return returnVal;
        }
        else
        {
            Logger.appendToWarningLog( "In resource " + objectID + ": \"" + key + "\" is not an array." );
            return emptyArray;
        }
    }

    /**
     * Returns the JSON object corresponding to the specified key.  If there is no
     * such key in the data, returns null
     *
     * @param key the key for the object to return
     * @return the wrapped JSONObject
     */

    public SimpleJSONObject getObject( String key )
    {
        Object obj = object.get( key );

        if ( obj == null )
        {
            if ( warnOnMissingKeys )
            { Logger.appendToWarningLog( "In resource " + objectID + ": \"" + key + "\" is missing" ); }
            return null;
        }
        else if ( obj instanceof JSONObject )
        {
            usedKeys.add( key );

            StringBuilder id = new StringBuilder( );
            id.append( objectID );
            id.append( " -> " );
            id.append( key );
            SimpleJSONObject returnVal = new SimpleJSONObject( ( JSONObject ) obj, id.toString( ) );

            if ( warnOnMissingKeys )
            { returnVal.setWarnOnMissingKeys( warnOnMissingKeys ); }

            subObjects.add( returnVal );
            return returnVal;
        }
        else
        {
            Logger.appendToWarningLog( "In resource " + objectID + ": \"" + key + "\" is not an object." );
            return null;
        }
    }

    /**
     * Returns true if and only if this object contains a value with the specified key
     *
     * @param key the key
     * @return true if the key is contained in this object
     */

    public boolean containsKey( String key )
    {
        return object.containsKey( key );
    }

    /**
     * Returns the set of all keys contained in this JSONObject
     *
     * @return the set of all keys contained in this JSONObject
     */

    public Set< String > keySet( )
    {
        Set< String > keys = object.keySet( );

        // don't return comments
        keys.remove( "comment" );

        return keys;
    }
}
