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

package hale.ability;

/**
 * The interface for any Object that wishes to be a target for an
 * Effect.  This will include Entities and Areas.
 *
 * @author Jared Stephen
 */

public interface EffectTarget
{
    /**
     * Removes the specified Effect from this EffectTarget.  This EffectTarget
     * will no longer be under the Effect or have the Effect's Bonuses.
     *
     * @param effect the Effect to remove
     */

    public void removeEffect(Effect effect);

    /**
     * Returns the number of points of spell resistance applied to this EffectTarget,
     * or 0 if the EffectTarget has no spell resistance.  Spell resistance shortens
     * the duration of effects created from Spells with {@link Spell#spellResistanceApplies()}
     * when the effect is applied.
     *
     * @return the number of points of spell resistance on this EffectTarget
     */

    public int getSpellResistance();


    /**
     * This method should return true if the effect target is still valid and capable of holding effects
     * It can return false if the effect should be removed due to the target no longer being valid
     *
     * @return whether the effect target is a valid target
     */

    public boolean isValidEffectTarget();
}
