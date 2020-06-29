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

package hale.particle;

import hale.Game;
import hale.loading.JSONOrderedObject;
import hale.util.SimpleJSONObject;

public class GaussianDistribution implements DistributionOneValue
{
    private final float mean, stddev;

    @Override
    public Object save()
    {
        JSONOrderedObject data = new JSONOrderedObject();

        data.put("class", this.getClass().getName());
        data.put("mean", mean);
        data.put("stddev", stddev);

        return data;
    }

    public static GaussianDistribution load(SimpleJSONObject data)
    {
        float mean = data.get("mean", 0.0f);
        float stddev = data.get("stddev", 0.0f);

        return new GaussianDistribution(mean, stddev);
    }

    public GaussianDistribution(float mean, float stddev)
    {
        this.mean = mean;
        this.stddev = stddev;
    }

    public float generate(Particle particle)
    {
        return Game.dice.gaussian(mean, stddev);
    }

    @Override
    public DistributionOneValue getCopyIfHasState()
    {
        return this;
    }
}
