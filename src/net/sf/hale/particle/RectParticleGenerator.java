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
import net.sf.hale.loading.JSONOrderedObject;
import net.sf.hale.loading.LoadGameException;
import net.sf.hale.util.SimpleJSONObject;

public class RectParticleGenerator extends ParticleGenerator
{
    private float upperX, upperY;
    private float lowerX, lowerY;

    @Override
    public JSONOrderedObject save( )
    {
        JSONOrderedObject data = super.save( );

        data.put( "upperX", upperX - getX( ) );
        data.put( "upperY", upperY - getY( ) );
        data.put( "lowerX", lowerX - getX( ) );
        data.put( "lowerY", lowerY - getY( ) );

        return data;
    }

    public static ParticleGenerator load( SimpleJSONObject data ) throws LoadGameException
    {
        Mode mode = Mode.valueOf( data.get( "generatorMode", null ) );
        String particleSprite = data.get( "sprite", null );
        float numParticles = data.get( "numParticles", 0.0f );

        RectParticleGenerator generator = new RectParticleGenerator( mode, particleSprite, numParticles );

        generator.loadBase( data );

        generator.upperX = data.get( "upperX", 0.0f );
        generator.upperY = data.get( "upperY", 0.0f );
        generator.lowerX = data.get( "lowerX", 0.0f );
        generator.lowerY = data.get( "lowerY", 0.0f );

        return generator;
    }

    public RectParticleGenerator( Mode mode, String sprite, float numParticles )
    {
        super( mode, sprite, numParticles );
    }

    public RectParticleGenerator( RectParticleGenerator other )
    {
        super( other );

        this.upperX = other.upperX;
        this.upperY = other.upperY;
        this.lowerX = other.lowerX;
        this.lowerY = other.lowerY;
    }

    public void setRectBounds( float lowerX, float upperX, float lowerY, float upperY )
    {
        this.upperX = Math.max( upperX, lowerX );
        this.upperY = Math.max( upperY, lowerY );
        this.lowerX = Math.min( lowerX, upperX );
        this.lowerY = Math.min( lowerY, upperY );
    }

    @Override
    public void offsetPosition( float x, float y )
    {
        super.offsetPosition( x, y );

        upperX += x;
        lowerX += x;

        upperY += y;
        lowerY += y;
    }

    @Override
    public boolean initialize( )
    {
        if ( ! super.initialize( ) ) return false;

        this.upperX += getX( );
        this.upperY += getY( );
        this.lowerX += getX( );
        this.lowerY += getY( );

        return true;
    }

    @Override
    protected void setParticlePosition( Particle particle )
    {
        particle.setPosition( Game.dice.rand( lowerX, upperX ), Game.dice.rand( lowerY, upperY ) );
    }

    @Override
    public Animated getCopy( )
    {
        return new RectParticleGenerator( this );
    }
}
