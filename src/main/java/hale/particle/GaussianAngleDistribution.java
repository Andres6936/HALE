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

package main.java.hale.particle;

import main.java.hale.Game;
import main.java.hale.loading.JSONOrderedObject;
import main.java.hale.util.SimpleJSONObject;

public class GaussianAngleDistribution implements DistributionTwoValue
{
    private static final float twoPi = 2.0f * (float)Math.PI;

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

    public static GaussianAngleDistribution load(SimpleJSONObject data)
    {
        float mean = data.get("mean", 0.0f);
        float stddev = data.get("stddev", 0.0f);

        return new GaussianAngleDistribution(mean, stddev);
    }

    public GaussianAngleDistribution(float mean, float stddev)
    {
        this.mean = mean;
        this.stddev = stddev;
    }

    @Override
    public float[] generate(Particle particle)
    {
        final float angle = Game.dice.rand(0.0f, twoPi);

        final float magnitude = Game.dice.gaussian(mean, stddev);

        final float[] vector = new float[4];

        vector[0] = (float)Math.cos(angle) * magnitude;
        vector[1] = (float)Math.sin(angle) * magnitude;
        vector[2] = magnitude;
        vector[3] = angle;

        return vector;
    }

    @Override
    public DistributionTwoValue getCopyIfHasState()
    {
        return this;
    }
}
