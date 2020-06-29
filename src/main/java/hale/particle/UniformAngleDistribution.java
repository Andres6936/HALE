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

public class UniformAngleDistribution implements DistributionTwoValue
{
    private static final float twoPi = 2.0f * (float)Math.PI;

    private final float magnitudeMin, magnitudeMax;

    @Override
    public Object save()
    {
        JSONOrderedObject data = new JSONOrderedObject();

        data.put("class", this.getClass().getName());
        data.put("magnitudeMin", magnitudeMin);
        data.put("magnitudeMax", magnitudeMax);

        return data;
    }

    public static UniformAngleDistribution load(SimpleJSONObject data)
    {
        float magMin = data.get("magnitudeMin", 0.0f);
        float magMax = data.get("magnitudeMax", 0.0f);

        return new UniformAngleDistribution(magMin, magMax);
    }

    public UniformAngleDistribution(float magnitudeMin, float magnitudeMax)
    {
        this.magnitudeMin = magnitudeMin;
        this.magnitudeMax = magnitudeMax;
    }

    @Override
    public float[] generate(Particle particle)
    {
        final float angle = Game.dice.rand(0.0f, twoPi);

        final float magnitude = Game.dice.rand(magnitudeMin, magnitudeMax);

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
