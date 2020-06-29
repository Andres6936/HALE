/*
 * Hale is highly moddable tactical RPG.
 * Copyright (C) 2012 Jared Stephen
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

package hale.entity;

import hale.area.Area;
import hale.loading.JSONOrderedObject;
import hale.loading.LoadGameException;
import hale.loading.ReferenceHandler;
import hale.util.SimpleJSONObject;

/**
 * An item that can be equipped as armor, gloves, boots, a helmet, or a shield
 *
 * @author Jared
 */

public class Armor extends EquippableItem
{

    private final ArmorTemplate template;

    // armor stats modified by quality
    private float qualityArmorPenalty, qualityArmorClass, qualityMovementPenalty;

    @Override
    public void load(SimpleJSONObject data, Area area, ReferenceHandler refHandler) throws LoadGameException
    {
        super.load(data, area, refHandler);
    }

    @Override
    public JSONOrderedObject save()
    {
        return super.save();
    }

    /**
     * Creates a new Armor item
     *
     * @param template
     */

    protected Armor(ArmorTemplate template)
    {
        super(template);

        this.template = template;
    }

    @Override
    public ArmorTemplate getTemplate()
    {
        return template;
    }

    /**
     * Returns the armor penalty of this armor, modified by the quality level of the item.
     * <p>
     * See @link {@link ArmorTemplate#getArmorPenalty()}
     *
     * @return the armor penalty of this armor, modified by the quality level of the item
     */

    public float getQualityModifiedArmorPenalty()
    {
        return qualityArmorPenalty;
    }

    /**
     * Returns the armor class of this armor, modified by the quality level of the item.
     * <p>
     * See @link {@link ArmorTemplate#getArmorClass()}
     *
     * @return the armor class of this armor, modified by the quality level of the item
     */

    public float getQualityModifiedArmorClass()
    {
        return qualityArmorClass;
    }

    /**
     * Returns the movement penalty of this armor, modified by the quality level of the item.
     * <p>
     * See @link {@link ArmorTemplate#getMovementPenalty()}
     *
     * @return the movement penalty of this armor, modified by the quality level of the item
     */

    public float getQualityModifiedMovementPenalty()
    {
        return qualityMovementPenalty;
    }

    @Override
    protected void setQuality(String quality)
    {
        super.setQuality(quality);

        if (template.hasQuality()) {
            this.qualityArmorPenalty = template.getArmorPenalty() *
                    (1.0f - ((float)getQuality().getArmorPenaltyBonus()) / 100.0f);
            this.qualityArmorClass = template.getArmorClass() *
                    (1.0f + ((float)getQuality().getArmorClassBonus()) / 100.0f);
            this.qualityMovementPenalty = template.getMovementPenalty() *
                    (1.0f - ((float)getQuality().getMovementPenaltyBonus()) / 100.0f);
        } else {
            this.qualityArmorPenalty = template.getArmorPenalty();
            this.qualityArmorClass = template.getArmorClass();
            this.qualityMovementPenalty = template.getMovementPenalty();
        }
    }
}
