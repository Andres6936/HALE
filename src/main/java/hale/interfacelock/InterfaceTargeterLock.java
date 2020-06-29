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
import main.java.hale.entity.Creature;

/**
 * An InterfaceLock that remains active as long as the game remains in Targeter mode.
 *
 * @author Jared Stephen
 */

public class InterfaceTargeterLock extends InterfaceLock
{
    private boolean finishing;

    /**
     * Creates a new InterfaceTargeter lock.  The lock will remain active until
     * there are no currently active Targeters in the TargeterManager.
     *
     * @param locker the parent Creature for this InterfaceLock
     */

    public InterfaceTargeterLock(Creature locker)
    {
        super(locker, Game.config.getCombatDelay() * 5);

        finishing = false;
    }

    @Override
    public void setCurrentTime(long curTime)
    {
        super.setCurrentTime(curTime);

        if (!finishing) {
            super.setUnlockTime(curTime + 800);

            if (!Game.areaListener.getTargeterManager().isInTargetMode()) {
                finishing = true;
            }
        }
    }

    @Override
    public int getPriority()
    {
        return 25;
    }
}
