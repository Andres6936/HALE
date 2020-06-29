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
import hale.loading.LoadGameException;
import hale.util.SaveGameUtil;
import hale.util.SimpleJSONObject;

public class FixedDistributionWithBase implements DistributionOneValue
{
    private final DistributionBase base;
    private final float multiplier;
    private final float offset;

    @Override
    public Object save()
    {
        JSONOrderedObject data = new JSONOrderedObject();

        data.put("class", this.getClass().getName());
        data.put("multiplier", multiplier);
        data.put("offset", offset);
        data.put("base", base.save());

        return data;
    }

    public static DistributionOneValue load(SimpleJSONObject data) throws LoadGameException
    {
        float multiplier = data.get("multiplier", 0.0f);
        float offset = data.get("offset", 0.0f);

        DistributionBase base = (DistributionBase)SaveGameUtil.loadObject(data.getObject("base"));

        return new FixedDistributionWithBase(base, multiplier, offset);
    }

    public FixedDistributionWithBase(DistributionBase base, float multiplier, float offset)
    {
        this.base = base;
        this.multiplier = multiplier;
        this.offset = offset;
    }

    public float generate(Particle particle)
    {
        return base.getBase(particle) * multiplier + offset;
    }

    @Override
    public DistributionOneValue getCopyIfHasState()
    {
        return this;
    }
}
