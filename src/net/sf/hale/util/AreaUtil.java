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
import java.util.Iterator;
import java.util.List;

import net.sf.hale.Game;
import net.sf.hale.area.Area;
import net.sf.hale.entity.Creature;
import net.sf.hale.entity.PC;
import net.sf.hale.entity.Path;
import net.sf.hale.rules.Faction;

/**
 * A class with utility helper functions for an Area, especially
 * dealing with line of sight and path finding
 *
 * @author Jared
 */

public class AreaUtil
{
    private final int width;
    private final int height;
    private boolean[][] transparent;

    private final Area area;

    private PathFinder.Data pathFindData;

    public enum Turn
    {
        LEFT, RIGHT, STRAIGHT
    }

    ;

    public AreaUtil( Area area )
    {
        this.area = area;
        boolean[][] trans = area.getTransparency( );

        width = trans.length;
        height = trans[ 0 ].length;

        transparent = new boolean[ width ][ height ];

        for ( int i = 0; i < width; i++ )
        {
            for ( int j = 0; j < height; j++ )
            {
                transparent[ i ][ j ] = trans[ i ][ j ];
            }
        }

        pathFindData = new PathFinder.Data( area );
    }

    public static List< Creature > getVisibleCreatures( Creature activeCreature, Faction.Relationship rel )
    {
        Faction activeFaction = activeCreature.getFaction( );

        List< Creature > creatures = activeCreature.getVisibleCreatures( );
        Iterator< Creature > iter = creatures.iterator( );
        while ( iter.hasNext( ) )
        {
            Creature c = iter.next( );

            Faction.Relationship curRel = activeFaction.getRelationship( c.getFaction( ) );
            if ( rel != null && curRel != rel )
            {
                iter.remove( );
            }
            else if ( c.stats.isHidden( ) )
            {
                iter.remove( );
            }
        }

        return creatures;
    }

    public static final void setMatrix( boolean[][] data, Point p, boolean value )
    {
        setMatrix( data, p.x, p.y, value );
    }

    public static final void setMatrix( boolean[][] data, int x, int y, boolean value )
    {
        if ( x < 0 || x >= data.length || y < 0 || y >= data[ 0 ].length ) return;

        data[ x ][ y ] = value;
    }

    private static final void orMatrix( boolean[][] source, boolean[][] dest )
    {
        if ( source.length != dest.length || source[ 0 ].length != dest[ 0 ].length ) return;

        for ( int i = 0; i < source.length; i++ )
        {
            for ( int j = 0; j < source[ i ].length; j++ )
            {
                dest[ i ][ j ] = source[ i ][ j ] || dest[ i ][ j ];
            }
        }
    }

    public void updateVisibility( )
    {
        updateTransparency( area.getTransparency( ) );
        for ( Creature c : Game.curCampaign.party )
        {
            c.computeVisibility( );
        }
        setPartyVisibility( );
    }

    public void setPartyVisibility( )
    {
        boolean[][] visible = area.getVisibility( );
        boolean[][] explored = area.getExplored( );

        setMatrix( visible, false );

        for ( Creature creature : Game.curCampaign.party )
        {
            if ( creature instanceof PC )
            { creature.addVisibilityToMatrix( visible ); }
        }

        Game.timer.getTemporaryVisibilityAreas( visible );

        orMatrix( visible, explored );
    }

    public void updateTransparency( boolean[][] transparent )
    {
        this.transparent = transparent;
    }

    public void setVisibilityWithRespectToPosition( boolean[][] visible, Point center )
    {
        setVisibilityWithRespectToPosition( visible, center.x, center.y );
    }

    public void setVisibilityWithRespectToPosition( boolean[][] visible, int centerX, int centerY )
    {
        setMatrix( visible, false );

        if ( visible.length != width || visible[ 0 ].length != height ) return;

        getVisibility( visible, centerX, centerY );
    }

    public static void setMatrix( boolean[][] data, boolean val )
    {
        for ( int i = 0; i < data.length; i++ )
        {
            for ( int j = 0; j < data[ 0 ].length; j++ )
            {
                data[ i ][ j ] = val;
            }
        }
    }

    private final boolean isValidCoordinates( Point p )
    {
        return p.x >= 0 && p.y >= 0 && p.x < width && p.y < height;
    }

    private boolean[][] getVisibility( boolean[][] visible, int centerX, int centerY )
    {
        if ( centerX < 0 || centerY < 0 || centerX >= visible.length || centerY >= visible[ 0 ].length ) return visible;

        byte centerElev = this.area.getElevationGrid( ).getElevation( centerX, centerY );

        visible[ centerX ][ centerY ] = true;

        LOSCone cone = new LOSCone( 0, new Point( Game.TILE_SIZE / 2, 0 ), 1, new Point( Game.TILE_SIZE, Game.TILE_SIZE / 2 ) );
        cone.next = new LOSCone( 2, new Point( Game.TILE_SIZE, Game.TILE_SIZE / 2 ), 3, new Point( Game.TILE_SIZE / 2, Game.TILE_SIZE ) );
        cone.next.next = new LOSCone( 3, new Point( Game.TILE_SIZE / 2, Game.TILE_SIZE ), 4, new Point( 0, Game.TILE_SIZE / 2 ) );
        cone.next.next.next = new LOSCone( 5, new Point( 0, Game.TILE_SIZE / 2 ), 6, new Point( Game.TILE_SIZE / 2, 0 ) );

        int radius = 1;
        while ( radius <= this.area.getVisibilityRadius( ) )
        {

            //System.out.println("At radius " + radius);

            LOSCone previousCone = cone;
            LOSCone currentCone = cone;
            boolean closeCurrentCone = false;

            while ( currentCone != null )
            {
                //Point s = new Point(currentCone.getStartArmX(), currentCone.getStartArmY());
                //Point e = new Point(currentCone.getEndArmX(), currentCone.getEndArmY());

                //System.out.println("  Cone with arms: " + s + "; " + e);
                //System.out.println("    With coords: " + currentCone.getStartI() + ", " + currentCone.getEndI());

                int i = 0;
                for ( i = currentCone.getStartI( ); i <= currentCone.getEndI( ); i++ )
                {
                    Point currentTile = AreaUtil.convertPolarToGrid( centerX, centerY, currentCone.getR( ), i );

                    byte curElev = 0;

                    if ( isValidCoordinates( currentTile ) )
                    {
                        visible[ currentTile.x ][ currentTile.y ] = true;

                        curElev = area.getElevationGrid( ).getElevation( currentTile.x, currentTile.y );
                    }

                    if ( ! isValidCoordinates( currentTile ) || ! transparent[ currentTile.x ][ currentTile.y ] || curElev > centerElev )
                    {
                        //System.out.println("    Found opaque tile at " + currentTile);
                        if ( currentCone.getStartI( ) == currentCone.getEndI( ) )
                        { // This cone has ended, close it out
                            //System.out.println("      Closing cone at " + i + ".");
                            closeCurrentCone = true;
                        }
                        else if ( currentCone.getStartI( ) == i )
                        {
                            //System.out.println("      Contracting start arm at " + i + ".");
                            currentCone.contractStartArm( i );
                            //System.out.println("      New coords: " + currentCone.getStartI() + ", " + currentCone.getEndI());
                            //System.out.println("      New start arm: " + currentCone.getStartArmX() + ", " + currentCone.getStartArmY());
                        }
                        else if ( currentCone.getEndI( ) == i )
                        {
                            //System.out.println("      Contracting end arm at " + i + ".");
                            currentCone.contractEndArm( i );
                            //System.out.println("      New coords: " + currentCone.getStartI() + ", " + currentCone.getEndI());
                            //System.out.println("      New end arm: " + currentCone.getEndArmX() + ", " + currentCone.getEndArmY());
                        }
                        else
                        {
                            //System.out.println("      Splitting cone at " + i + ".");
                            LOSCone nextCone = currentCone.split( i );
                            //System.out.println("      First cone coords: " + currentCone.getStartI() + ", " + currentCone.getEndI());
                            //System.out.println("      Second cone coords: " + nextCone.getStartI() + ", " + nextCone.getEndI());

                            nextCone.next = currentCone.next;
                            currentCone.next = nextCone;
                        }
                    }
                }

                if ( closeCurrentCone == true )
                {
                    if ( currentCone == cone )
                    {
                        cone = currentCone.next;
                        currentCone = currentCone.next;
                        if ( currentCone != null ) previousCone = currentCone.next;
                    }
                    else
                    {
                        previousCone.next = currentCone.next;
                        currentCone = currentCone.next;
                    }

                    closeCurrentCone = false;
                }
                else
                {
                    previousCone = currentCone;
                    currentCone = currentCone.next;
                }
            }

            //System.out.println("  Expanding.");

            currentCone = cone;
            while ( currentCone != null )
            {
                //System.out.println("    Cone");
                //System.out.println("      Old Coords: " + currentCone.getStartI() + ", " + currentCone.getEndI());
                //System.out.println("      Old Start arm: " + currentCone.getStartArmX() + ", " + currentCone.getStartArmY());
                //System.out.println("      Old End arm: " + currentCone.getEndArmX() + ", " + currentCone.getEndArmY());
                currentCone.expand( );
                //System.out.println("      New Coords: " + currentCone.getStartI() + ", " + currentCone.getEndI());
                //System.out.println("      New Start arm: " + currentCone.getStartArmX() + ", " + currentCone.getStartArmY());
                //System.out.println("      New End arm: " + currentCone.getEndArmX() + ", " + currentCone.getEndArmY());
                currentCone = currentCone.next;
            }

            radius++;
        }

        return visible;
    }

    /**
     * Finds the shortest possible path from the position of the mover to the end point,
     * ignoring the positions of all creatures
     *
     * @param mover
     * @param end
     * @param distanceAway
     * @return the shortest available path or null if no path exists
     */

    public Path findShortestPathIgnoreCreatures( Creature mover, Point end )
    {
        boolean[][] entityPass = area.getEntities( ).getDoorPassabilities( mover );

        synchronized ( pathFindData )
        {
            pathFindData.setEntityPassabilities( entityPass );
            return PathFinder.findPathIgnoreCreatures( mover, end, pathFindData );
        }
    }

    /**
     * Finds the shortest possible path from the position of the moving creature to the specified
     * end position using the passability rules for the current area, ignoring the positions of all
     * party members
     *
     * @param mover the creature that is moving
     * @param end   the destination point
     * @return the shortest available path or null if no path exists
     */

    public Path findShortestPathIgnoreParty( Creature mover, Point end, boolean[][] entityPass )
    {
        synchronized ( pathFindData )
        {
            pathFindData.setEntityPassabilities( entityPass );

            return PathFinder.findPathIgnorePartyMembers( mover, end, pathFindData );
        }
    }

    /**
     * Finds the shortest possible path from the position of the moving creature to the specified
     * end position using the passability rules for the current area
     *
     * @param mover        the creature that is moving
     * @param end          the destination point or center of the destination points
     * @param distanceAway the distance away from the destination point that the mover must be
     *                     to satisfy a valid path.  This method will find the shortest path to any of the points
     *                     that are the specified distance away from the end point.  If distanceAway = 0, then
     *                     it just find the path to the end point
     * @return the shortest available path or null if no path exists
     */

    public Path findShortestPath( Creature mover, Point end, int distanceAway )
    {
        List< Point > goals = new ArrayList< Point >( );

        // add the appropriate set of goal points
        if ( distanceAway == 0 )
        {
            goals.add( end );
        }
        else
        {
            for ( int i = 0; i < distanceAway * 6; i++ )
            {
                goals.add( AreaUtil.convertPolarToGrid( end, distanceAway, i ) );
            }
        }

        boolean[][] entityPass = Game.curCampaign.curArea.getEntityPassabilities( mover );

        synchronized ( pathFindData )
        {
            pathFindData.setEntityPassabilities( entityPass );

            return PathFinder.findPath( mover, end, goals, pathFindData );
        }
    }

    public static Point[] getAdjacentTiles( Point grid )
    {
        return getAdjacentTiles( grid.x, grid.y );
    }

    public static Point[] getAdjacentTiles( int gridX, int gridY )
    {
        Point[] adjacent = new Point[ 6 ];
        int i;
        for ( i = 0; i < 6; i++ )
        {
            adjacent[ i ] = new Point( );
        }

        // North
        adjacent[ 0 ].x = gridX;
        adjacent[ 0 ].y = gridY - 1;

        // North East
        adjacent[ 1 ].x = gridX + 1;
        adjacent[ 1 ].y = gridY - ( ( gridX + 1 ) % 2 ); // If x is even, subtract one, if x is odd, no change

        // South East
        adjacent[ 2 ].x = gridX + 1;
        adjacent[ 2 ].y = gridY + ( gridX % 2 ); // If x is even, no change, if x is odd, add one

        // South
        adjacent[ 3 ].x = gridX;
        adjacent[ 3 ].y = gridY + 1;

        // South West
        adjacent[ 4 ].x = gridX - 1;
        adjacent[ 4 ].y = gridY + ( gridX % 2 ); // If x is even, no change, if x is odd, add one

        // North West
        adjacent[ 5 ].x = gridX - 1;
        adjacent[ 5 ].y = gridY - ( ( gridX + 1 ) % 2 ); // If x is even, subtract one, if x is odd, no change

        return adjacent;
    }

    public static final Point convertGridToScreenAndCenter( int gridX, int gridY )
    {
        Point p = convertGridToScreen( gridX, gridY );
        p.x += Game.TILE_SIZE / 2;
        p.y += Game.TILE_SIZE / 2;

        return p;
    }

    public static final Point convertGridToScreenAndCenter( Point grid )
    {
        return convertGridToScreenAndCenter( grid.x, grid.y );
    }

    public static final Point convertGridToScreen( Point grid )
    {
        return convertGridToScreen( grid.x, grid.y );
    }

    public static final Point convertGridToScreen( int gridX, int gridY )
    {
        int screenX = gridX * Game.TILE_WIDTH;
        int screenY = gridY * Game.TILE_SIZE;

        if ( gridX % 2 == 1 || gridX % 2 == - 1 )
        {
            screenY += Game.TILE_SIZE / 2;
        }

        return new Point( screenX, screenY );
    }

    public static final Point convertScreenToGrid( Point screen )
    {
        return convertScreenToGrid( screen.x, screen.y );
    }

    public static final Point convertScreenToGrid( int screenX, int screenY )
    {
        // this function does not work for negative results

        int xBase = ( screenX / ( Game.TILE_WIDTH * 2 ) ) * 2;
        int xMod = screenX % ( Game.TILE_WIDTH * 2 );

        int yBase = screenY / Game.TILE_SIZE;
        int yMod = screenY % Game.TILE_SIZE;

        int xOffset = 0;
        int yOffset = 0;

        if ( yMod < Game.TILE_SIZE / 2 )
        {
            if ( xMod * 2 + yMod < Game.TILE_SIZE / 2 )
            {
                xOffset = - 1;
                yOffset = - 1;
            }
            else if ( xMod * 2 - yMod < Game.TILE_SIZE * 3 / 2 )
            {
                xOffset = 0;
                yOffset = 0;
            }
            else
            {
                xOffset = 1;
                yOffset = - 1;
            }
        }
        else
        {
            if ( xMod * 2 - ( yMod - Game.TILE_SIZE / 2 ) < 0 )
            {
                xOffset = - 1;
                yOffset = 0;
            }
            else if ( xMod * 2 + ( yMod - Game.TILE_SIZE / 2 ) < Game.TILE_SIZE * 2 )
            {
                xOffset = 0;
                yOffset = 0;
            }
            else
            {
                xOffset = 1;
                yOffset = 0;
            }
        }

        return new Point( xBase + xOffset, yBase + yOffset );
    }

    public static final Point convertPolarToGridCenter0( int r, int i )
    {
        if ( r == 0 ) return new Point( 0, 0 );

        if ( i >= 6 * r ) i -= 6 * r;

        int d = ( i / r );
        int e = i % r;

        int px = 0;
        int py = 0;

        if ( d == 0 )
        {
            px = 0;
            py = 0 - r;

            py = py + ( e + Math.abs( px % 2 ) ) / 2;
            px = px + e;
        }
        else if ( d == 1 )
        {
            px = 0 + r;
            py = 0 - ( r + 1 ) / 2;

            py = py + e;
            px = px + 0;
        }
        else if ( d == 2 )
        {
            px = 0 + r;
            py = 0 + ( r ) / 2;

            py = py + ( e + Math.abs( px % 2 ) ) / 2;
            px = px - e;
        }
        else if ( d == 3 )
        {
            px = 0;
            py = 0 + r;

            py = py - ( e + 1 - Math.abs( px % 2 ) ) / 2;
            px = px - e;
        }
        else if ( d == 4 )
        {
            px = 0 - r;
            py = 0 + ( r ) / 2;

            py = py - e;
            px = px + 0;
        }
        else if ( d == 5 )
        {
            px = 0 - r;
            py = 0 - ( r + 1 ) / 2;

            py = py - ( e + 1 - Math.abs( px % 2 ) ) / 2;
            px = px + e;
        }

        return new Point( px, py );
    }

    public static final Point convertGridToPolar( Point center, Point grid )
    {
        return convertGridToPolar( center.x, center.y, grid.x, grid.y );
    }

    public static final Point convertGridToPolar( int centerX, int centerY, int gridX, int gridY )
    {
        int r = distance( centerX, centerY, gridX, gridY );
        int i = 0;

        if ( gridY > centerY ) { i = 3 * r; }
        else { i = 0; }

        Point start = convertPolarToGrid( centerX, centerY, r, i );

        int dist = distance( start.x, start.y, gridX, gridY );

        if ( gridX > centerX )
        {
            if ( gridY > centerY ) { i -= dist; }
            else { i += dist; }
        }
        else
        {
            if ( gridY > centerY ) { i += dist; }
            else
            {
                i += ( 6 * r - dist );
            }
        }

        return new Point( r, i );

    }

    public static final Point convertPolarToGrid( Point center, int r, int i )
    {
        return convertPolarToGrid( center.x, center.y, r, i );
    }

    public static final Point convertPolarToGrid( int centerX, int centerY, int r, int i )
    {
        if ( r == 0 ) return new Point( centerX, centerY );

        if ( i >= 6 * r ) i -= 6 * r;

        int d = ( i / r );
        int e = i % r;

        int px = 0;
        int py = 0;

        if ( d == 0 )
        {
            px = centerX;
            py = centerY - r;

            py = py + ( e + Math.abs( px % 2 ) ) / 2;
            px = px + e;
        }
        else if ( d == 1 )
        {
            px = centerX + r;
            py = centerY - ( r + 1 - ( centerX % 2 ) ) / 2;

            py = py + e;
            px = px + 0;
        }
        else if ( d == 2 )
        {
            px = centerX + r;
            py = centerY + ( r + ( centerX % 2 ) ) / 2;

            py = py + ( e + Math.abs( px % 2 ) ) / 2;
            px = px - e;
        }
        else if ( d == 3 )
        {
            px = centerX;
            py = centerY + r;

            py = py - ( e + 1 - Math.abs( px % 2 ) ) / 2;
            px = px - e;
        }
        else if ( d == 4 )
        {
            px = centerX - r;
            py = centerY + ( r + ( centerX % 2 ) ) / 2;

            py = py - e;
            px = px + 0;
        }
        else if ( d == 5 )
        {
            px = centerX - r;
            py = centerY - ( r + 1 - ( centerX % 2 ) ) / 2;

            py = py - ( e + 1 - Math.abs( px % 2 ) ) / 2;
            px = px + e;
        }

        return new Point( px, py );
    }

    public static ArrayList< Point > findIntersectingHexes( int x0, int y0, int x1, int y1 )
    {
        ArrayList< Point > hexes = new ArrayList< Point >( );

        Point previous = AreaUtil.convertScreenToGrid( x0, y0 );
        Point end = AreaUtil.convertScreenToGrid( x1, y1 );
        Point[] adjacentToEnd = AreaUtil.getAdjacentTiles( end );

        Point current = new Point( - 1, - 1 );
        int previousSide = - 1;

        int i = 0;

//		System.out.println("Finding...");

        while ( i < 24 && ! current.equals( end ) )
        {
            Point previousScreen = AreaUtil.convertGridToScreen( previous );

            previousSide = getLineIntersectSide( previousSide, previousScreen.x, previousScreen.y, x0, y0, x1, y1 );

//			System.out.println("   previous is " + previous + " side is " + previousSide + " end is " + end);

            if ( previousSide == 0 )
            {
                // North
                current.x = previous.x;
                current.y = previous.y - 1;

            }
            else if ( previousSide == 1 )
            {
                // North East
                current.x = previous.x + 1;
                current.y = previous.y - ( ( previous.x + 1 ) % 2 ); // If x is even, subtract one, if x is odd, no change

            }
            else if ( previousSide == 2 )
            {
                // South East
                current.x = previous.x + 1;
                current.y = previous.y + ( previous.x % 2 ); // If x is even, no change, if x is odd, add one

            }
            else if ( previousSide == 3 )
            {
                // South
                current.x = previous.x;
                current.y = previous.y + 1;

            }
            else if ( previousSide == 4 )
            {
                // South West
                current.x = previous.x - 1;
                current.y = previous.y + ( previous.x % 2 ); // If x is even, no change, if x is odd, add one

            }
            else if ( previousSide == 5 )
            {
                // North West
                current.x = previous.x - 1;
                current.y = previous.y - ( ( previous.x + 1 ) % 2 ); // If x is even, subtract one, if x is odd, no change
            }

            hexes.add( new Point( current ) );

            // check to see if we have reached a tile adjacent to the end
            if ( contains( adjacentToEnd, current ) )
            {
                hexes.add( end );
                break;
            }

            previous.x = current.x;
            previous.y = current.y;

            i++;
        }

        return hexes;
    }

    private static final boolean contains( Point[] points, Point pointToCheck )
    {
        for ( Point point : points )
        {
            if ( point.x == pointToCheck.x && point.y == pointToCheck.y ) return true;
        }

        return false;
    }

    public static final Turn turns( int x0, int y0, int x1, int y1, int x2, int y2 )
    {
        int cross = ( x1 - x0 ) * ( y2 - y0 ) - ( x2 - x0 ) * ( y1 - y0 );

        return ( ( cross > 0 ) ? Turn.LEFT : ( ( cross == 0 ) ? Turn.STRAIGHT : Turn.RIGHT ) );
    }

    private static final int getLineIntersectSide( int previousSide, int hexX, int hexY, int x3, int y3, int x4, int y4 )
    {
        int[] x = { hexX + Game.TILE_SIZE / 4, hexX + Game.TILE_WIDTH, hexX + Game.TILE_SIZE,
                hexX + Game.TILE_WIDTH, hexX + Game.TILE_SIZE / 4, hexX, hexX + Game.TILE_SIZE / 4 };
        int[] y = { hexY, hexY, hexY + Game.TILE_SIZE / 2, hexY + Game.TILE_SIZE,
                hexY + Game.TILE_SIZE, hexY + Game.TILE_SIZE / 2, hexY };

        int noCheckSide = ( previousSide + 3 ) % 6;
        if ( previousSide == - 1 ) noCheckSide = - 1;

//		System.out.println("     no check side is " + noCheckSide + ", hex = (" + hexX + ", " + hexY + ") p3 = (" + x3 + ", " + y3 + ") p4 = (" + x4 + ", " + y4 + ")");

        int bestChoice = - 1;
        double bestVal = - 100.0;

        for ( int i = 0; i < 6; i++ )
        {

            if ( i == noCheckSide ) continue;

            double uanum = ( x4 - x3 ) * ( y[ i ] - y3 ) - ( y4 - y3 ) * ( x[ i ] - x3 );
            double ubnum = ( x[ i + 1 ] - x[ i ] ) * ( y[ i ] - y3 ) - ( y[ i + 1 ] - y[ i ] ) * ( x[ i ] - x3 );
            double den = ( y4 - y3 ) * ( x[ i + 1 ] - x[ i ] ) - ( x4 - x3 ) * ( y[ i + 1 ] - y[ i ] );

            double ua = uanum / den;
            double ub = ubnum / den;

//			System.out.println("      " + i + ": " + "ua = " + ua + ", ub = " + ub);

            if ( ua >= 0.0 && ua < 1.0 && ub >= 0.0 && ub < 1.0 )
            {
                double curVal = ua + ub;
                if ( curVal > bestVal )
                {
                    bestChoice = i;
                    bestVal = curVal;
                }
            }

        }

        return bestChoice;
    }

    public static final boolean lineSegmentIntersectsHex( int hexX, int hexY, int x0, int y0, int x1, int y1 )
    {
        if ( turns( hexX, hexY + Game.TILE_SIZE / 2, hexX + Game.TILE_SIZE / 4, hexY, x0, y0 ) == Turn.RIGHT &&
                turns( hexX, hexY + Game.TILE_SIZE / 2, hexX + Game.TILE_SIZE / 4, hexY, x1, y1 ) == Turn.RIGHT )
        { return false; }

        if ( turns( hexX + Game.TILE_WIDTH, hexY + Game.TILE_SIZE, hexX + Game.TILE_SIZE, hexY + Game.TILE_SIZE / 2, x0, y0 ) == Turn.LEFT &&
                turns( hexX + Game.TILE_WIDTH, hexY + Game.TILE_SIZE, hexX + Game.TILE_SIZE, hexY + Game.TILE_SIZE / 2, x1, y1 ) == Turn.LEFT )
        { return false; }


        if ( hexY > y0 && hexY > y1 ) return false;
        if ( hexY + Game.TILE_SIZE < y0 && hexY + Game.TILE_SIZE < y1 ) return false;

        if ( turns( hexX + Game.TILE_SIZE / 4, hexY + Game.TILE_SIZE, hexX, hexY + Game.TILE_SIZE / 2, x0, y0 ) == Turn.RIGHT &&
                turns( hexX + Game.TILE_SIZE / 4, hexY + Game.TILE_SIZE, hexX, hexY + Game.TILE_SIZE / 2, x1, y1 ) == Turn.RIGHT )
        { return false; }

        if ( turns( hexX + Game.TILE_SIZE, hexY + Game.TILE_SIZE / 2, hexX + Game.TILE_WIDTH, hexY, x0, y0 ) == Turn.LEFT &&
                turns( hexX + Game.TILE_SIZE, hexY + Game.TILE_SIZE / 2, hexX + Game.TILE_WIDTH, hexY, x1, y1 ) == Turn.LEFT )
        { return false; }

        return lineIntersectsHex( hexX, hexY, x0, y0, x1, y1 );
    }

    public static final boolean lineIntersectsHex( int hexX, int hexY, int x0, int y0, int x1, int y1 )
    {
        int[] x = { hexX + Game.TILE_SIZE / 4, hexX + Game.TILE_WIDTH, hexX + Game.TILE_SIZE,
                hexX + Game.TILE_WIDTH, hexX + Game.TILE_SIZE / 4, hexX };
        int[] y = { hexY, hexY, hexY + Game.TILE_SIZE / 2, hexY + Game.TILE_SIZE,
                hexY + Game.TILE_SIZE, hexY + Game.TILE_SIZE / 2 };

        int i;
        Turn side1, j;
        side1 = turns( x0, y0, x1, y1, x[ 0 ], y[ 0 ] );

        if ( side1 == Turn.STRAIGHT ) return true;

        for ( i = 1; i < 6; i++ )
        {
            j = turns( x0, y0, x1, y1, x[ i ], y[ i ] );
            if ( j == Turn.STRAIGHT || j != side1 ) return true;
        }

        return false;
    }

    public static final int distance( Point a, Point b )
    {
        return distance( a.x, a.y, b.x, b.y );
    }

    public static final int distance( int ax, int ay, int bx, int by )
    {
        int axout = ay - Floor2( - ax );
        int ayout = ay + Ceil2( - ax );

        int bxout = by - Floor2( - bx );
        int byout = by + Ceil2( - bx );

        int dx = bxout - axout;
        int dy = byout - ayout;

        return ( Math.abs( dx ) + Math.abs( dy ) + Math.abs( dx - dy ) ) / 2;
    }

    public static final int euclideanDistance2( int x1, int y1, int x2, int y2 )
    {
        return ( ( x2 - x1 ) * ( x2 - x1 ) + ( y2 - y1 ) * ( y2 - y1 ) );
    }

    public static final int Floor2( int x )
    {
        return ( ( x >= 0 ) ? ( x >> 1 ) : ( x - 1 ) / 2 );
    }

    public static final int Ceil2( int x )
    {
        return ( ( x >= 0 ) ? ( x + 1 ) >> 1 : x / 2 );
    }

    public static final double angle( int x1, int y1, int x2, int y2 )
    {
        double dx = x2 - x1;
        double dy = y2 - y1;
        if ( dy > 0 )
        {
            return Math.atan( dx / dy );
        }
        else
        {
            if ( dy == 0.0 )
            {
                if ( dx > 0 ) { return Math.PI / 2; }
                else { return - Math.PI / 2; }
            }
            else { return Math.PI + Math.atan( dx / dy ); }
        }
    }
}
