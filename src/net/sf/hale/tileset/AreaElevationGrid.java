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

package net.sf.hale.tileset;

import java.io.BufferedWriter;
import java.io.IOException;

import net.sf.hale.util.AreaUtil;
import net.sf.hale.util.Point;

/**
 * A grid with specified elevation for each tile of a given area
 *
 * @author Jared
 */

public class AreaElevationGrid
{
    private static byte MaxElevation = 9;
    private static byte MinElevation = - 9;

    private byte[][] elevation;

    /**
     * returns an array representing this elevation grid
     *
     * @return the elevation array
     */

    public byte[][] writeToJSON( )
    {
        byte[][] out = new byte[ elevation[ 0 ].length ][ elevation.length ];
        for ( int x = 0; x < elevation.length; x++ )
        {
            for ( int y = 0; y < elevation[ x ].length; y++ )
            {
                out[ y ][ x ] = elevation[ x ][ y ];
            }
        }

        return out;
    }

    /**
     * Creates a new AreaElevationGrid for an area with the specified dimensions
     *
     * @param width  the width of the grid
     * @param height the height of the grid
     */

    public AreaElevationGrid( int width, int height )
    {
        elevation = new byte[ width ][ height ];
    }

    /**
     * Modifies the size of this elevation grid to the new size
     *
     * @param width  the new width
     * @param height the new height
     */

    public void resize( int width, int height )
    {
        byte[][] newElevation = new byte[ width ][ height ];

        int copyWidth = Math.min( width, elevation.length );
        int copyHeight = Math.min( height, elevation[ 0 ].length );

        for ( int x = 0; x < copyWidth; x++ )
        {
            for ( int y = 0; y < copyHeight; y++ )
            {
                newElevation[ x ][ y ] = this.elevation[ x ][ y ];
            }
        }

        this.elevation = newElevation;
    }

    /**
     * Returns the width of this grid
     *
     * @return the width of this grid
     */

    public int getWidth( )
    {
        return elevation.length;
    }

    /**
     * Returns the height of this grid
     *
     * @return the height of this grid
     */

    public int getHeight( )
    {
        return elevation[ 0 ].length;
    }

    /**
     * Adds the specified value to the elevation at the specified coordinates.  Note
     * that the elevation will be constrained to be within {@link #MinElevation},
     * {@link #MaxElevation} inclusive, even if the resulting value is outside of those bounds.
     * <p>
     * Also, the elevation will be constrained such that no point has an elevation that is more
     * than 1 different from any neighboring point
     *
     * @param x     the x coordinate
     * @param y     the y coordinate
     * @param delta the amount to change the elevation at the coordinates by
     */

    public void modifyElevation( int x, int y, byte delta )
    {
        if ( delta == 0 ) return;

        this.elevation[ x ][ y ] = ( byte ) ( this.elevation[ x ][ y ] + delta );

        byte minValue = MinElevation;
        byte maxValue = MaxElevation;

        for ( Point p : AreaUtil.getAdjacentTiles( x, y ) )
        {
            if ( p.x < 0 || p.y < 0 || p.x >= elevation.length || p.y >= elevation[ 0 ].length )
            { continue; }

            byte pElevation = this.elevation[ p.x ][ p.y ];

            minValue = ( byte ) Math.max( minValue, pElevation - 1 );
            maxValue = ( byte ) Math.min( maxValue, pElevation + 1 );
        }

        if ( elevation[ x ][ y ] > maxValue ) { elevation[ x ][ y ] = maxValue; }
        else if ( elevation[ x ][ y ] < minValue ) elevation[ x ][ y ] = minValue;
    }

    /**
     * Sets the elevation at the specified coordinates to the specified value.  Note
     * that the elevation will be constrained to be within {@link #MinElevation},
     * {@link #MaxElevation} inclusive, even if the specified value is outside of those bounds
     *
     * @param x    the x coordinate
     * @param y    the y coordinate
     * @param elev the elevation at the specified coordinates
     */

    public void setElevation( int x, int y, byte elev )
    {
        this.elevation[ x ][ y ] = elev;

        if ( elevation[ x ][ y ] > MaxElevation ) { elevation[ x ][ y ] = MaxElevation; }
        else if ( elevation[ x ][ y ] < MinElevation ) elevation[ x ][ y ] = MinElevation;
    }

    /**
     * Returns the elevation at the specified coordinates
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the elevation at the specified coordinates
     */

    public byte getElevation( int x, int y )
    {
        return elevation[ x ][ y ];
    }

    /**
     * Writes the elevation data contained in this List out to the specified Writer
     *
     * @param out the Writer to write with
     * @throws IOException
     */

    public void write( BufferedWriter out ) throws IOException
    {
        for ( int y = 0; y < elevation[ 0 ].length; y++ )
        {
            out.write( "elevation " );
            out.write( Integer.toString( y ) );
            out.write( " " );

            for ( int x = 0; x < elevation.length; x++ )
            {
                out.write( Byte.toString( elevation[ x ][ y ] ) );
                out.write( " " );
            }

            out.newLine( );
        }
    }
}
