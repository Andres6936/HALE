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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Set;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import net.sf.hale.resource.ResourceManager;
import net.sf.hale.resource.ResourceType;

/**
 * A wrapper class for the JSONParser and associated classes that allows
 * for easy use of JSON files in the rest of the code base.  Also allows for
 * data validation that is very useful for debugging data files
 *
 * @author Jared Stephen
 */

public class SimpleJSONParser
{
    private SimpleJSONObject result;

    /**
     * Creates a new JSONParser and parses the resource at the specified resource
     * location with the specified extension
     *
     * @param resource the resource to parse
     * @param type     the resource type of the resource to parse
     */

    public SimpleJSONParser( String resource, ResourceType type )
    {
        initialize( ResourceManager.getReader( resource + type.getExtension( ) ), resource );
    }

    /**
     * Creates a new JSONParser and parses the resource at the specified resource
     * location
     *
     * @param resource the resource to parse
     */

    public SimpleJSONParser( String resource )
    {
        initialize( ResourceManager.getReader( resource ), resource );
    }

    /**
     * Creates a JSONParser parsing the specified input stream
     *
     * @param id    the ID used for informational purposes if an error occurs
     * @param input
     */

    public SimpleJSONParser( String id, InputStream input )
    {
        initialize( new InputStreamReader( input ), id );
    }

    /**
     * Creates a JSONParser parsing the resource at the specified file
     *
     * @param file the file to parse
     */

    public SimpleJSONParser( File file )
    {
        try
        {
            initialize( new FileReader( file ), file.getPath( ) );
        }
        catch ( FileNotFoundException e )
        {
            Logger.appendToErrorLog( "Unable to find file " + file.getPath( ) + " to parse." );
        }
    }

    /**
     * Creates a new JSONParser parsing the resource from the specified reader
     *
     * @param reader the reader to parse
     * @param id     the ID of the resource being parsed
     */

    public SimpleJSONParser( Reader reader, String id )
    {
        initialize( reader, id );
    }

    private void initialize( Reader reader, String id )
    {
        JSONParser parser = new JSONParser( JSONParser.MODE_PERMISSIVE );

        try
        {
            result = new SimpleJSONObject( ( JSONObject ) parser.parse( reader ), id );
            reader.close( );
        }
        catch ( ParseException e )
        {
            Logger.appendToErrorLog( "JSON Parsing error in " + id, e );
            return;
        }
        catch ( IOException e )
        {
            Logger.appendToErrorLog( "I/O Error in " + id, e );
        }
    }

    /**
     * Sets whether this Parser and all sub object will warn when a key is requested but not
     * found.  This defaults to false.
     *
     * @param warn whether to warn on missing keys
     */

    public void setWarnOnMissingKeys( boolean warn )
    {
        result.setWarnOnMissingKeys( warn );
    }

    /**
     * Generates warning messages in the standard warning log for any unused keys
     * in the JSON Parsing tree.  This method should be called after finishing use
     * of a JSON tree to warn the user of typos and other errors that might otherwise
     * be hard to spot.
     */

    public void warnOnUnusedKeys( )
    {
        result.warnOnUnusedKeys( );
    }

    /**
     * Returns true if and only if the specified key exists and points to a String
     *
     * @param key the key to check
     * @return true if the key's value is a string
     */

    public boolean isString( String key )
    {
        return result.isString( key );
    }

    /**
     * Returns true if and only if the specified key exists and points to an int
     *
     * @param key the key to check
     * @return true if the key's value is an int
     */

    public boolean isInteger( String key )
    {
        return result.isInteger( key );
    }

    /**
     * Returns true if and only if the specified key exists and points to a float or double
     *
     * @param key the key to check
     * @return true if the key's value is a float
     */

    public boolean isFloat( String key )
    {
        return result.isFloat( key );
    }

    /**
     * Returns true if and only if the specified key exists and points to a boolean
     *
     * @param key the key to check
     * @return true if the key's value is a boolean
     */

    public boolean isBoolean( String key )
    {
        return result.isBoolean( key );
    }

    /**
     * Returns the value specified by the key or the defaultValue if the resource that
     * was parsed does not contain the specified key or the key does not point to an integer
     *
     * @param key          the key for the value
     * @param defaultValue the default value to return if the key is not found
     * @return the value for the specified key
     */

    public int get( String key, int defaultValue )
    {
        return result.get( key, defaultValue );
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
        return result.get( key, defaultValue );
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
        return result.get( key, defaultValue );
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
        return result.get( key, defaultValue );
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
        return result.getArray( key );
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
        return result.getObject( key );
    }

    /**
     * Returns true if and only if this object contains a value with the specified key
     *
     * @param key the key
     * @return true if the key is contained in this object
     */

    public boolean containsKey( String key )
    {
        return result.containsKey( key );
    }

    /**
     * Returns the set of all keys contained in this JSONObject
     *
     * @return the set of all keys contained in this JSONObject
     */

    public Set< String > keySet( )
    {
        return result.keySet( );
    }

    /**
     * Returns the object containing the data parsed by this Parser
     *
     * @return the object containing the data parsed by this Parser
     */

    public SimpleJSONObject getObject( )
    {
        return result;
    }
}
