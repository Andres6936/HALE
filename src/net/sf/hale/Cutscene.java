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

package net.sf.hale;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.hale.resource.ResourceManager;
import net.sf.hale.util.SimpleJSONArray;
import net.sf.hale.util.SimpleJSONArrayEntry;
import net.sf.hale.util.SimpleJSONObject;
import net.sf.hale.util.SimpleJSONParser;

/**
 * A cutscene consists of one or more frames, with each frame containing a background image
 * to display and some text
 *
 * @author Jared Stephen
 */

public class Cutscene
{
    private String id;
    private List< Frame > frames;
    private int textAreaWidth, textAreaHeight;

    private String callbackScript, callbackFunction;


    /**
     * Creates a new Cutscene with the specified ID using the data in the resource
     * at the specified path
     *
     * @param id   the ID for this Cutscene
     * @param path the resource path for this Cutscene
     */

    public Cutscene( String id, String path )
    {
        this.id = id;
        this.frames = Collections.emptyList( );

        SimpleJSONParser parser = new SimpleJSONParser( path );

        // warn for missing keys at the top level
        parser.setWarnOnMissingKeys( true );

        textAreaWidth = parser.get( "textAreaWidth", 0 );
        textAreaHeight = parser.get( "textAreaHeight", 0 );

        SimpleJSONArray array = parser.getArray( "frames" );
        this.frames = new ArrayList< Frame >( array.size( ) );

        // allow missing keys for the individual frames
        parser.setWarnOnMissingKeys( false );

        for ( SimpleJSONArrayEntry entry : array )
        {
            frames.add( new Frame( entry.getObject( ) ) );
        }

        callbackScript = parser.get( "callbackScript", null );
        callbackFunction = parser.get( "callbackFunction", null );

        parser.warnOnUnusedKeys( );
    }

    /**
     * Returns the ID of the script to be called after this cutscene is completed, or
     * null if nothing is called
     *
     * @return the ID of the script to call on completion
     */

    public String getCallbackScript( ) { return callbackScript; }

    /**
     * Returns the function of the script that should be called when this
     * cutscene completes, or null if nothing is called
     *
     * @return the callback function
     */

    public String getCallbackFunction( ) { return callbackFunction; }

    /**
     * Returns the unique ID for this Cutscene
     *
     * @return the ID for this Cutscene
     */

    public String getID( ) { return id; }

    /**
     * Returns the number of frames contained in this cutscene
     *
     * @return the number of frames contained in this cutscene
     */

    public int getNumFrames( ) { return frames.size( ); }

    /**
     * Returns the list of frames contained in this cutscene.  The returned
     * list is unmodifiable
     *
     * @return the list of frames contained in this cutscene
     */

    public List< Frame > getFrames( )
    {
        return Collections.unmodifiableList( frames );
    }

    /**
     * A single frame of a cutscene.  Frames are shown in order, one at a time
     *
     * @author Jared Stephen
     */

    public class Frame
    {
        private String bgImage;
        private String text;
        private int textAreaWidth, textAreaHeight;

        private Frame( SimpleJSONObject data )
        {
            bgImage = data.get( "background", null );
            textAreaWidth = data.get( "textAreaWidth", Cutscene.this.textAreaWidth );
            textAreaHeight = data.get( "textAreaHeight", Cutscene.this.textAreaHeight );

            text = data.get( "text", null );
            if ( text == null )
            {
                String textFile = data.get( "textFile", null );
                if ( textFile != null )
                { text = ResourceManager.getResourceAsString( "cutscenes/" + textFile ); }
            }
        }

        /**
         * Returns the width that this Frame's text area should be set to, or
         * 0 if it should use the default value
         *
         * @return the width for this Frame's text area
         */

        public int getTextAreaWidth( )
        {
            return textAreaWidth;
        }

        /**
         * Returns the height that this Frame's text area should be set to, or
         * 0 if it should use the default value
         *
         * @return the height for this Frame's text area
         */

        public int getTextAreaHeight( )
        {
            return textAreaHeight;
        }

        /**
         * Returns the resource location for the background image for this
         * Frame
         *
         * @return the background image
         */

        public String getBGImage( )
        {
            return bgImage;
        }

        /**
         * Returns the text for this Cutscene Frame
         *
         * @return the text
         */

        public String getText( )
        {
            return text;
        }
    }
}
