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

import main.java.hale.loading.JSONOrderedObject;
import main.java.hale.util.SimpleJSONObject;

public class FixedDistribution implements DistributionOneValue
{
    public float value;

    @Override
    public Object save()
    {
        JSONOrderedObject data = new JSONOrderedObject();

        data.put("class", this.getClass().getName());
        data.put("value", value);

        return data;
    }

    public static DistributionOneValue load(SimpleJSONObject data)
    {
        return new FixedDistribution(data.get("value", 0.0f));
    }

    public FixedDistribution(float value)
    {
        this.value = value;
    }

    public float generate(Particle particle)
    {
        return value;
    }

    @Override
    public DistributionOneValue getCopyIfHasState()
    {
        return this;
    }
}