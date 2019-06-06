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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import net.sf.hale.Game;
import net.sf.hale.entity.Creature;
import net.sf.hale.rules.Campaign;

/**
 * The header data that is written at the beginning of save files to allow
 * quick access to data that should be displayed when showing the list of
 * available saves
 *
 * @author Jared Stephen
 */

public class SaveFileHeader
{
    private List< String > characterNames;
    private String areaName;
    private int level;
    private String versionID;

    private SaveFileHeader( ) { }

    /**
     * Creates a new SaveFileHeader for the specified campaign, using data
     * from the campaign's party
     *
     * @param campaign the campaign to create this SaveFileHeader for
     */

    public SaveFileHeader( Campaign campaign )
    {
        int maxLevel = 0;
        this.characterNames = new ArrayList< String >( campaign.party.size( ) );
        for ( Creature pc : campaign.party )
        {
            // don't write out summoned creature names
            if ( pc.isSummoned( ) ) continue;

            characterNames.add( pc.getTemplate( ).getName( ) );

            maxLevel = Math.max( maxLevel, pc.stats.getCreatureLevel( ) );
        }

        this.level = maxLevel;

        this.areaName = campaign.curArea.getName( );

        this.versionID = Game.config.getVersionID( );
    }

    /**
     * Returns the version ID of the version that was used to create this save file header
     *
     * @return the version ID
     */

    public String getVersionID( )
    {
        return versionID;
    }

    /**
     * Returns the name of the current area that the party is within
     *
     * @return the name of the current area
     */

    public String getAreaName( )
    {
        return areaName;
    }

    /**
     * Returns the maximum level of all party members
     *
     * @return the maximum level of all party members
     */

    public int getPartyLevel( )
    {
        return level;
    }

    /**
     * Returns a List of the names of all party members.  The returned list is
     * unmodifiable.
     *
     * @return a List of the names of all party members.
     */

    public List< String > getPartyNames( )
    {
        return Collections.unmodifiableList( characterNames );
    }

    /**
     * Returns a string containing all of the names of party members in order,
     * separated by commas.
     *
     * @return a string containing all of the names of party members
     */

    public String getPartyNamesString( )
    {
        StringBuilder sb = new StringBuilder( );

        for ( int i = 0; i < characterNames.size( ) - 1; i++ )
        {
            sb.append( characterNames.get( i ) );
            sb.append( ", " );
        }

        sb.append( characterNames.get( characterNames.size( ) - 1 ) );

        return sb.toString( );
    }

    /**
     * Writes the specified header file to the specified file output stream as plain text
     *
     * @param header the header to write out
     * @param fout   the file output stream
     * @throws IOException
     */

    public static void write( SaveFileHeader header, GZIPOutputStream fout ) throws IOException
    {
        PrintStream pout = new PrintStream( fout );

        pout.print( "HEADER\n" );

        pout.print( "BuildID " );
        pout.print( header.versionID );
        pout.print( "\n" );

        pout.print( header.areaName );
        pout.print( "\n" );

        pout.print( "Level " );
        pout.print( header.level );
        pout.print( "\n" );

        for ( String characterName : header.characterNames )
        {
            pout.print( characterName );
            pout.print( "\n" );
        }

        pout.print( "END HEADER\n" );
    }

    /*
     * Helper method since we can't use Buffered input streams, see read() below
     */

    private static String nextLine( InputStream in ) throws IOException
    {
        StringBuilder sb = new StringBuilder( );

        int c;
        while ( ( c = in.read( ) ) != - 1 )
        {
            switch ( c )
            {
                case '\n':
                    return sb.toString( );
                default:
                    sb.append( ( char ) c );
            }
        }

        return sb.toString( );
    }

    /*
     * Helper method since we can't use Buffered input streams, see read() below
     */

    private static String nextToken( InputStream in ) throws IOException
    {
        StringBuilder sb = new StringBuilder( );

        int c;
        while ( ( c = in.read( ) ) != - 1 )
        {
            switch ( c )
            {
                case ' ':
                case '\n':
                    return sb.toString( );
                default:
                    sb.append( ( char ) c );
            }
        }

        return sb.toString( );
    }

    /**
     * Reads a save file header from the specified file input stream.  This method does
     * not make use of any buffered methods, allowing the caller to immediately create
     * a gzip reader on the rest of the stream after calling this method
     *
     * @param in the file input stream to read from
     * @return a new save file header
     * @throws IOException
     */

    public static SaveFileHeader read( GZIPInputStream in ) throws IOException
    {
        SaveFileHeader header = new SaveFileHeader( );

        // read HEADER line
        nextToken( in );

        // read the BuildID token
        nextToken( in );
        header.versionID = nextToken( in );

        header.areaName = nextLine( in );

        // read the Level token
        nextToken( in );
        header.level = Integer.parseInt( nextToken( in ) );

        header.characterNames = new ArrayList< String >( );
        while ( true )
        {
            String line = nextLine( in );

            if ( "END HEADER".equals( line ) )
            {
                break;
            }

            header.characterNames.add( line );
        }

        return header;
    }
}
