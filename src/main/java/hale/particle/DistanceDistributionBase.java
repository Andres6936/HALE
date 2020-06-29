package main.java.hale.particle;

import main.java.hale.loading.JSONOrderedObject;
import main.java.hale.util.Point;
import main.java.hale.util.SimpleJSONObject;

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

/**
 * A DistributionBase for returning the distance between a particle
 * and a given screenPoint
 */

public class DistanceDistributionBase implements DistributionBase
{
    private int x;
    private int y;

    @Override
    public Object save()
    {
        JSONOrderedObject data = new JSONOrderedObject();

        data.put("class", getClass().getName());
        data.put("x", x);
        data.put("y", y);

        return data;
    }

    /**
     * Creates a new DistanceDistributionBase
     *
     * @param screenPoint the screenPoint that the distance for each particle
     *                    will be computed from
     */

    public DistanceDistributionBase(Point screenPoint)
    {
        this.x = screenPoint.x;
        this.y = screenPoint.y;
    }

    @Override
    public float getBase(Particle particle)
    {
        return (float)Math.sqrt((particle.getX() - x) * (particle.getX() - x) +
                (particle.getY() - y) * (particle.getY() - y));
    }

    @Override
    public DistributionBase load(SimpleJSONObject data)
    {
        int x = data.get("x", 0);
        int y = data.get("y", 0);

        return new DistanceDistributionBase(new Point(x, y));
    }

}
