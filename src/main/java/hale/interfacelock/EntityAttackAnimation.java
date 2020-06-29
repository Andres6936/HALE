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

package main.java.hale.interfacelock;

import main.java.hale.Game;
import main.java.hale.entity.Entity;
import main.java.hale.util.AreaUtil;
import main.java.hale.util.Point;

/**
 * An EntityOffsetAnimation that moves a Creature half way to an adjacent
 * tile and then quickly back to its original position
 *
 * @author Jared Stephen
 */

public class EntityAttackAnimation extends EntityOffsetAnimation
{
    private float halfDuration;
    private float elapsed;
    private float xPerSecond, yPerSecond;

    /**
     * Creates a new EntityAttackAnimation
     *
     * @param attacker the creature that will be moving
     * @param target   the entity at the target position
     */

    public EntityAttackAnimation(Entity attacker, Entity target)
    {
        super();

        halfDuration = Game.config.getCombatDelay() / 1000.0f;
        elapsed = 0.0f;

        Point curScreen = attacker.getLocation().getScreenPoint();
        Point destScreen = target.getLocation().getScreenPoint();

        double distance = AreaUtil.euclideanDistance2(curScreen.x, curScreen.y, destScreen.x, destScreen.y);
        distance = Math.sqrt(distance);

        double factor = 0.75 * Game.TILE_SIZE / distance;

        xPerSecond = (float)factor * (destScreen.x - curScreen.x) / halfDuration;
        yPerSecond = (float)factor * (destScreen.y - curScreen.y) / halfDuration;
    }

    @Override
    protected boolean runAnimation(float seconds)
    {
        elapsed += seconds;

        if (elapsed > 2.0f * halfDuration) {
            resetOffset();
            return true;
        } else
            if (elapsed > halfDuration) {
                float time = 2.0f * halfDuration - elapsed;
                setOffset(xPerSecond * time, yPerSecond * time);
                return false;
            } else {
                setOffset(xPerSecond * elapsed, yPerSecond * elapsed);
                return false;
            }
    }
}
