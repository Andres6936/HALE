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
import net.sf.hale.util.SimpleJSONObject;

public class EquallySpacedAngleDistribution implements DistributionTwoValue
{
    private final float angleStepSize;
    private final float magnitudeStepSize;
    private final float particlesPerRadius;
    private final float jitter;

    private int count;
    private float curMagnitude;

    @Override
    public Object save( )
    {
        JSONOrderedObject data = new JSONOrderedObject( );

        data.put( "class", this.getClass( ).getName( ) );
        data.put( "angleStepSize", angleStepSize );
        data.put( "magnitudeStepSize", magnitudeStepSize );
        data.put( "particlesPerRadius", particlesPerRadius );
        data.put( "jitter", jitter );

        data.put( "count", count );
        data.put( "curMagnitude", curMagnitude );

        return data;
    }

    public static DistributionTwoValue load( SimpleJSONObject data )
    {
        float angleStepSize = data.get( "angleStepSize", 0.0f );
        float magStep = data.get( "magnitudeStepSize", 0.0f );
        float particlesPerRadius = data.get( "particlesPerRadius", 0.0f );
        float jitter = data.get( "jitter", 0.0f );

        EquallySpacedAngleDistribution dist = new EquallySpacedAngleDistribution( angleStepSize, magStep, particlesPerRadius, jitter );
        dist.count = data.get( "count", 0 );
        dist.curMagnitude = data.get( "curMagnitude", 0.0f );

        return dist;
    }

    private EquallySpacedAngleDistribution( float angleStepSize, float magStepSize, float particlesPerRadius, float jitter )
    {
        this.angleStepSize = angleStepSize;
        this.magnitudeStepSize = magStepSize;
        this.particlesPerRadius = particlesPerRadius;
        this.jitter = jitter;
    }

    public EquallySpacedAngleDistribution( float magnitudeMin, float magnitudeMax, float magnitudeStepSize, float numParticlesTotal, float jitter )
    {
        this.magnitudeStepSize = magnitudeStepSize;
        this.jitter = jitter;

        if ( magnitudeMax == magnitudeMin )
        {
            particlesPerRadius = numParticlesTotal;
        }
        else
        {
            particlesPerRadius = numParticlesTotal / ( ( magnitudeMax - magnitudeMin ) / magnitudeStepSize );
        }

        angleStepSize = 2.0f * ( float ) Math.PI / particlesPerRadius;

        count = 0;
        curMagnitude = magnitudeMin;
    }

    public EquallySpacedAngleDistribution( EquallySpacedAngleDistribution other )
    {
        this.angleStepSize = other.angleStepSize;
        this.magnitudeStepSize = other.magnitudeStepSize;
        this.particlesPerRadius = other.particlesPerRadius;
        this.jitter = other.jitter;

        this.count = other.count;
        this.curMagnitude = other.curMagnitude;
    }

    @Override
    public float[] generate( Particle particle )
    {
        float angle = angleStepSize * count;
        float magnitude = curMagnitude + Game.dice.rand( 0.0f, jitter );

        final float[] vector = new float[ 4 ];

        vector[ 0 ] = ( float ) Math.cos( angle ) * magnitude;
        vector[ 1 ] = ( float ) Math.sin( angle ) * magnitude;
        vector[ 2 ] = magnitude;
        vector[ 3 ] = angle;

        count++;

        if ( count >= particlesPerRadius )
        {
            count = 0;
            curMagnitude += magnitudeStepSize;
        }

        return vector;
    }

    @Override
    public DistributionTwoValue getCopyIfHasState( )
    {
        return new EquallySpacedAngleDistribution( this );
    }
}
