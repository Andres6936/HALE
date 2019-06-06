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

import net.sf.hale.loading.Saveable;
import net.sf.hale.util.SimpleJSONObject;

/**
 * A DistributionBase allows a Distribution to have its generated values be based
 * on a property of the given particle in question.  For example, when calculating
 * the color for a given particle, you can have the color be based on the speed of
 * the particle.  So, you might have faster particles be more red while slower
 * particles are more white.
 *
 * @author Jared Stephen
 */

public interface DistributionBase extends Saveable
{

    /**
     * Returns the float value for the property of the specified particle
     * for this DistributionBase.
     *
     * @param particle the Particle to get the property from
     * @return the value for the property of the specified particle for this
     * DistributionBase
     */

    public float getBase( Particle particle );

    /**
     * Loads the distribution base from the specified JSON data
     *
     * @param data the JSON data to load from
     * @return the distribution base
     */

    public DistributionBase load( SimpleJSONObject data );
}
