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

import main.java.hale.util.Point;

/**
 * An animation that offsets an entity for a short time, to show movement
 * or combat
 *
 * @author Jared Stephen
 */

public abstract class EntityOffsetAnimation
{
    private Point entityOffset;

    private boolean canceled;

    /**
     * Create a new EntityOffsetAnimation.
     */

    public EntityOffsetAnimation()
    {
        this.canceled = false;
    }

    /**
     * Sets the point that will be controlled with this animation.
     * This method must be called prior to any other methods.
     *
     * @param offset
     */

    public void setAnimatingPoint(Point offset)
    {
        this.entityOffset = offset;
        resetOffset();
    }

    /**
     * Sets the entity Offset to the specified x and y values.  The values
     * are converted to ints
     *
     * @param x the x coordinate
     * @param y the y coordinate
     */

    protected void setOffset(float x, float y)
    {
        entityOffset.x = (int)x;
        entityOffset.y = (int)y;
    }

    /**
     * Sets the entityOffset back to its default state of 0
     */

    protected void resetOffset()
    {
        entityOffset.x = 0;
        entityOffset.y = 0;
    }

    /**
     * Ends this animation.  The animation may take some time to perform some "cleanup"
     */

    public void cancel()
    {
        this.canceled = true;
    }

    /**
     * Elapses the specified amount of time in seconds for this animation,
     * updating the entity offset
     *
     * @param seconds the amount of time to elapse
     * @return true if this animation is finished and can be removed, false otherwise
     */

    public final boolean elapseTime(float seconds)
    {
        if (canceled) return true;

        return runAnimation(seconds);
    }

    /**
     * Overriden by subclasses to run the specified animation
     *
     * @param seconds the amount of time that has been elapsed
     * @return true if the animation is completed, false otherwise
     */

    protected abstract boolean runAnimation(float seconds);
}
