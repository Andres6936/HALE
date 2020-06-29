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

import hale.loading.JSONOrderedObject;
import hale.util.AreaUtil;
import hale.util.Point;
import hale.util.SimpleJSONObject;

/**
 * A Two valued distribution for setting the velocity of a particle to go towards
 * a specified destination point in a specified amount of time
 *
 * @author Jared Stephen
 */

public class VelocityTowardsPointDistribution implements DistributionTwoValue
{
    private Point dest;
    private float time;

    @Override
    public Object save()
    {
        JSONOrderedObject data = new JSONOrderedObject();

        data.put("class", this.getClass().getName());
        data.put("destX", dest.x);
        data.put("destY", dest.y);
        data.put("time", time);

        return data;
    }

    public static VelocityTowardsPointDistribution load(SimpleJSONObject data)
    {
        int x = data.get("destX", 0);
        int y = data.get("destY", 0);
        float time = data.get("time", 0.0f);

        return new VelocityTowardsPointDistribution(new Point(x, y), time);
    }

    /**
     * Creates a new Distribution
     *
     * @param dest the destination point that this velocity distribution will send
     *             points towards (screen coordinates)
     * @param time the amount of time in seconds that the trip should take
     */

    public VelocityTowardsPointDistribution(Point dest, float time)
    {
        this.dest = new Point(dest);
        this.time = time;
    }

    @Override
    public float[] generate(Particle particle)
    {
        int distanceSquared = AreaUtil.euclideanDistance2((int)particle.getX(), (int)particle.getY(), dest.x, dest.y);
        float distance = (float)Math.sqrt(distanceSquared);

        float distX = dest.x - particle.getX();
        float distY = dest.y - particle.getY();

        float magnitude = distance / time;

        float angle = (float)Math.acos(distX / distance);

        if (distY < 0.0f) angle = -angle;

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
