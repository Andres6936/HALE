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

public class CircleParticleGenerator extends ParticleGenerator
{
    private float minBound, maxBound;

    private static final float twoPi = 2.0f * ( float ) Math.PI;

    @Override
    public JSONOrderedObject save( )
    {
        JSONOrderedObject data = super.save( );

        data.put( "minCircleBound", minBound );
        data.put( "maxCircleBound", maxBound );

        return data;
    }

    public static ParticleGenerator load( SimpleJSONObject data ) throws LoadGameException
    {
        Mode mode = Mode.valueOf( data.get( "generatorMode", null ) );
        String particleSprite = data.get( "sprite", null );
        float numParticles = data.get( "numParticles", 0.0f );

        CircleParticleGenerator generator = new CircleParticleGenerator( mode, particleSprite, numParticles );

        generator.loadBase( data );

        generator.minBound = data.get( "minCircleBound", 0.0f );
        generator.maxBound = data.get( "maxCircleBound", 0.0f );

        return generator;
    }

    public CircleParticleGenerator( Mode mode, String sprite, float numParticles )
    {
        super( mode, sprite, numParticles );
    }

    public CircleParticleGenerator( CircleParticleGenerator other )
    {
        super( other );

        this.minBound = other.minBound;
        this.maxBound = other.maxBound;
    }

    public void setCircleBounds( float min, float max )
    {
        this.minBound = min;
        this.maxBound = max;
    }

    @Override
    public void offsetPosition( float x, float y )
    {
        super.offsetPosition( x, y );
    }

    @Override
    public boolean initialize( )
    {
        if ( ! super.initialize( ) ) return false;

        return true;
    }

    @Override
    protected void setParticlePosition( Particle particle )
    {
        float angle = Game.dice.rand( 0.0f, twoPi );
        float magnitude = Game.dice.rand( minBound, maxBound );

        particle.setPosition( ( float ) Math.cos( angle ) * magnitude + getX( ), ( float ) Math.sin( angle ) * magnitude + getY( ) );
    }

    @Override
    public Animated getCopy( )
    {
        return new CircleParticleGenerator( this );
    }
}
