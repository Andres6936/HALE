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

/**
 * An EntityOffsetAnimation that moves an entity from one tile to an
 * adjacent tile smoothly over a specified time
 *
 * @author Jared Stephen
 */

public class EntityMovementAnimation extends EntityOffsetAnimation
{
    private Entity mover;
    private int initialGridX, initialGridY;

    private float duration;
    private float elapsed;
    private float xPerSecond, yPerSecond;

    private int xOffset, yOffset;

    /**
     * Create a new Animation moving the specified entity to the destination point
     * over Game.config.getCombatDelay() milliseconds
     *
     * @param mover   the entity being moved
     * @param deltaX  the change in the amount of x screen coordinates
     * @param deltaY  the change in the amount of y screen coordinates
     * @param xOffset the base x value
     * @param yOffset the base y value
     */

    public EntityMovementAnimation(Entity mover, int deltaX, int deltaY, int xOffset, int yOffset)
    {
        super();

        this.mover = mover;
        this.initialGridX = mover.getLocation().getX();
        this.initialGridY = mover.getLocation().getY();

        this.xOffset = xOffset;
        this.yOffset = yOffset;

        duration = Game.config.getCombatDelay() / 1000.0f;
        elapsed = 0.0f;

        xPerSecond = deltaX / duration;
        yPerSecond = deltaY / duration;

        duration += 0.025f;
    }

    @Override
    protected boolean runAnimation(float seconds)
    {
        elapsed += seconds;

        if (elapsed > duration || elapsed < 0.0 || initialGridX != mover.getLocation().getX() ||
                initialGridY != mover.getLocation().getY()) {

            resetOffset();
            return true;
        } else {
            setOffset(xOffset + xPerSecond * elapsed, yOffset + yPerSecond * elapsed);
            return false;
        }
    }
}
