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

package net.sf.hale.particle;

import net.sf.hale.Game;
import net.sf.hale.area.Area;
import net.sf.hale.resource.Sprite;
import net.sf.hale.util.AreaUtil;
import net.sf.hale.util.Point;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

public class Particle extends AnimationBase
{
    private final Sprite sprite;
    private boolean stopAtOpaque, drawInOpaque;

    public Particle( Particle other )
    {
        super( other );

        this.sprite = other.sprite;

        this.stopAtOpaque = other.stopAtOpaque;
        this.drawInOpaque = other.drawInOpaque;
    }

    public Particle( Sprite sprite )
    {
        super( sprite.getWidth( ) / 2, sprite.getHeight( ) / 2 );
        this.sprite = sprite;

        this.stopAtOpaque = false;
        this.drawInOpaque = false;
    }

    public final void setDrawInOpaque( boolean drawInOpaque )
    {
        this.drawInOpaque = drawInOpaque;
    }

    public final void setStopAtOpaque( boolean stopAtOpaque )
    {
        this.stopAtOpaque = stopAtOpaque;
    }

    public final void draw( )
    {
        GL11.glColor4f( getR( ), getG( ), getB( ), getA( ) );
        GL14.glSecondaryColor3f( getR2( ), getG2( ), getB2( ) );

        int posX = ( int ) getX( );
        int posY = ( int ) getY( );

        Point pos1 = AreaUtil.convertScreenToGrid( posX + getHalfWidth( ), posY + getHalfHeight( ) );
        Point pos2 = AreaUtil.convertScreenToGrid( posX + getHalfWidth( ), posY - getHalfHeight( ) );
        Point pos3 = AreaUtil.convertScreenToGrid( posX - getHalfWidth( ), posY + getHalfHeight( ) );
        Point pos4 = AreaUtil.convertScreenToGrid( posX - getHalfWidth( ), posY - getHalfHeight( ) );

        Area area = Game.curCampaign.curArea;

        if ( ! drawInOpaque )
        {
            // don't draw effects in opaque tiles regardless of whether the effect stops or not
            if ( ( ! area.isTransparent( pos1 ) || ! area.isTransparent( pos2 ) ||
                    ! area.isTransparent( pos3 ) || ! area.isTransparent( pos4 ) ) )
            {
                Point pos = AreaUtil.convertScreenToGrid( posX + getHalfWidth( ), posY + getHalfHeight( ) );
                if ( ! area.isTransparent( pos ) && stopAtOpaque ) finish( );

                return;
            }
        }

        if ( getRotation( ) != 0.0f )
        {
            GL11.glPushMatrix( );

            GL11.glTranslatef( getX( ), getY( ), 0.0f );

            GL11.glRotatef( getRotation( ), 0.0f, 0.0f, 1.0f );

            if ( area.isVisible( pos1 ) || area.isVisible( pos2 ) || area.isVisible( pos3 ) || area.isVisible( pos4 ) )
            {
                sprite.drawNoTextureBind( - getHalfWidth( ), - getHalfHeight( ) );
            }

            GL11.glPopMatrix( );
        }
        else
        {
            if ( area.isVisible( pos1 ) || area.isVisible( pos2 ) || area.isVisible( pos3 ) || area.isVisible( pos4 ) )
            {
                sprite.drawNoTextureBind( ( int ) getX( ) - getHalfWidth( ), ( int ) getY( ) - getHalfHeight( ) );
            }
        }
    }
}
