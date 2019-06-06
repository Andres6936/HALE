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

import net.sf.hale.Game;

public class LOSCone
{
    private int startI;
    private int endI;
    private int r;
    private Point startArm;
    private Point endArm;

    public LOSCone next = null;

    public int getStartArmX( ) { return startArm.x; }

    public int getStartArmY( ) { return startArm.y; }

    public int getEndArmX( ) { return endArm.x; }

    public int getEndArmY( ) { return endArm.y; }

    public int getStartI( ) { return startI; }

    public int getEndI( ) { return endI; }

    public int getR( ) { return r; }

    public LOSCone( int startI, Point startArm, int endI, Point endArm )
    {
        this( startI, startArm, endI, endArm, 1 );
    }

    public LOSCone( int startI, Point startArm, int endI, Point endArm, int r )
    {
        this.startI = startI;
        this.endI = endI;
        this.startArm = startArm;
        this.endArm = endArm;
        this.r = r;
    }

    public LOSCone( LOSCone cone )
    {
        this( cone.startI, new Point( cone.startArm.x, cone.startArm.y ), cone.endI,
              new Point( cone.endArm.x, cone.endArm.y ), cone.r );
    }

    public LOSCone split( int splitI )
    {
        LOSCone secondPart = new LOSCone( this );

        contractEndArm( splitI );
        secondPart.contractStartArm( splitI );

        return secondPart;
    }

    public void contractStartArm( int startI )
    {
        Point gridI = AreaUtil.convertPolarToGridCenter0( r, startI );
        Point screenI = AreaUtil.convertGridToScreen( gridI );

        //System.out.println("          Polar: (" + (r) + ", " + endI + ") Grid: " + gridI + " Screen: " + screenI);

        int side = ( ( startI + 1 ) % ( r * 6 ) ) / r;

        //System.out.println("          Choosing side: " + side);

        if ( side == 0 )
        {
            startArm.x = screenI.x + Game.TILE_SIZE;
            startArm.y = screenI.y + Game.TILE_SIZE / 2;
        }
        else if ( side == 1 )
        {
            startArm.x = screenI.x + Game.TILE_WIDTH;
            startArm.y = screenI.y + Game.TILE_SIZE;
        }
        else if ( side == 2 )
        {
            startArm.x = screenI.x + Game.TILE_SIZE / 4;
            startArm.y = screenI.y + Game.TILE_SIZE;
        }
        else if ( side == 3 )
        {
            startArm.x = screenI.x;
            startArm.y = screenI.y + Game.TILE_SIZE / 2;
        }
        else if ( side == 4 )
        {
            startArm.x = screenI.x + Game.TILE_SIZE / 4;
            startArm.y = screenI.y;
        }
        else if ( side == 5 )
        {
            startArm.x = screenI.x + Game.TILE_WIDTH;
            startArm.y = screenI.y;
        }

        this.startI = startI + 1;
    }

    public void contractEndArm( int endI )
    {
        Point gridI = AreaUtil.convertPolarToGridCenter0( r, endI );
        Point screenI = AreaUtil.convertGridToScreen( gridI );

        //System.out.println("          Polar: (" + (r) + ", " + endI + ") Grid: " + gridI + " Screen: " + screenI);

        int side = 0;
        if ( endI != 6 * r ) side = endI / r; // if endI = 6 * r, then we leave side at 0

        //System.out.println("          Choosing side: " + side);

        if ( side == 0 )
        {
            endArm.x = screenI.x;
            endArm.y = screenI.y + Game.TILE_SIZE / 2;
        }
        else if ( side == 1 )
        {
            endArm.x = screenI.x + Game.TILE_SIZE / 4;
            endArm.y = screenI.y;
        }
        else if ( side == 2 )
        {
            endArm.x = screenI.x + Game.TILE_WIDTH;
            endArm.y = screenI.y;
        }
        else if ( side == 3 )
        {
            endArm.x = screenI.x + Game.TILE_SIZE;
            endArm.y = screenI.y + Game.TILE_SIZE / 2;
        }
        else if ( side == 4 )
        {
            endArm.x = screenI.x + Game.TILE_WIDTH;
            endArm.y = screenI.y + Game.TILE_SIZE;
        }
        else if ( side == 5 )
        {
            endArm.x = screenI.x + Game.TILE_SIZE / 4;
            endArm.y = screenI.y + Game.TILE_SIZE;
        }

        this.endI = endI - 1;
    }

    public void expand( )
    {
        int d = startI / r;
        int e = startI % r;

        int newStartI = d * ( r + 1 ) + e - 1;
        if ( newStartI < 0 ) newStartI = 0;

        Point gridI, screenI;
        boolean intersects = false;

        //System.out.println("        Computing New Start");

        while ( newStartI < ( r + 1 ) * 6 )
        {
            gridI = AreaUtil.convertPolarToGridCenter0( r + 1, newStartI );
            screenI = AreaUtil.convertGridToScreen( gridI );

            //System.out.println("          Polar: (" + (r+1) + ", " + newStartI + ") Grid: " + gridI + " Screen: " + screenI);

            intersects = AreaUtil.lineIntersectsHex( screenI.x, screenI.y, Game.TILE_SIZE / 2, Game.TILE_SIZE / 2, startArm.x, startArm.y );

            if ( intersects ) break;
            newStartI++;
        }

        d = endI / r;
        e = endI % r;

        int newEndI = d * ( r + 1 ) + e + 1;
        if ( newEndI > ( r + 1 ) * 6 ) newEndI = ( r + 1 ) * 6;

        //System.out.println("        Computing New End");

        while ( newEndI > 0 )
        {
            gridI = AreaUtil.convertPolarToGridCenter0( r + 1, newEndI );
            screenI = AreaUtil.convertGridToScreen( gridI );

            //System.out.println("          Polar: (" + (r+1) + ", " + newEndI + ") Grid: " + gridI + " Screen: " + screenI);

            intersects = AreaUtil.lineIntersectsHex( screenI.x, screenI.y, Game.TILE_SIZE / 2, Game.TILE_SIZE / 2, endArm.x, endArm.y );

            if ( intersects ) break;
            newEndI--;
        }

        startI = newStartI;
        endI = newEndI;
        r++;
    }
}
