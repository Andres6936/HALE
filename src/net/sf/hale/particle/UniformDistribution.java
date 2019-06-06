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

public class UniformDistribution implements DistributionOneValue
{
    private float min, max;

    @Override
    public Object save( )
    {
        JSONOrderedObject data = new JSONOrderedObject( );

        data.put( "class", this.getClass( ).getName( ) );
        data.put( "min", min );
        data.put( "max", max );

        return data;
    }

    public static UniformDistribution load( SimpleJSONObject data )
    {
        float min = data.get( "min", 0.0f );
        float max = data.get( "max", 0.0f );

        return new UniformDistribution( min, max );
    }

    public UniformDistribution( float min, float max )
    {
        this.min = min;
        this.max = max;
    }

    public float generate( Particle particle ) { return Game.dice.rand( min, max ); }

    @Override
    public DistributionOneValue getCopyIfHasState( )
    {
        return this;
    }
}
