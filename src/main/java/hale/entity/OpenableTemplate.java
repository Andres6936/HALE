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

package main.java.hale.entity;

import main.java.hale.icon.Icon;
import main.java.hale.icon.IconFactory;
import main.java.hale.util.SimpleJSONObject;

/**
 * A template for any openable object that can be locked, such as doors
 * and chests
 *
 * @author Jared
 */

public abstract class OpenableTemplate extends EntityTemplate
{

    private final Icon openIcon;
    private final Icon closedIcon;

    private final boolean isDefaultLocked;
    private final int lockDifficulty;

    private final String keyID;
    private final boolean isKeyRequiredToUnlock;
    private final boolean isRemoveKeyOnUnlock;

    /**
     * Creates an OpenableTemplate
     *
     * @param id   the entity ID
     * @param data the JSON data to parse
     */

    public OpenableTemplate(String id, SimpleJSONObject data)
    {
        super(id, data);

        if (data.containsKey("openIcon")) {
            this.openIcon = IconFactory.createIcon(data.getObject("openIcon"));
        } else {
            this.openIcon = IconFactory.emptyIcon;
        }

        if (data.containsKey("closedIcon")) {
            this.closedIcon = IconFactory.createIcon(data.getObject("closedIcon"));
        } else {
            this.closedIcon = IconFactory.emptyIcon;
        }

        this.isDefaultLocked = data.get("isLocked", false);
        this.lockDifficulty = data.get("lockDifficulty", 0);

        this.keyID = data.get("keyID", null);

        this.isKeyRequiredToUnlock = data.get("keyRequired", false);
        this.isRemoveKeyOnUnlock = data.get("removeKeyOnUnlock", false);
    }

    /**
     * Returns the icon used to display this openable when it is open
     *
     * @return the open icon
     */

    public Icon getOpenIcon()
    {
        return openIcon;
    }

    /**
     * Returns the icon used to display this openable when it is closed
     *
     * @return the closed icon
     */

    public Icon getClosedIcon()
    {
        return closedIcon;
    }

    /**
     * Returns true if this openable will be locked by default (until it is unlocked, generally
     * by a player), false otherwise
     *
     * @return whether this openable will be locked by default
     */

    public boolean isDefaultLocked()
    {
        return isDefaultLocked;
    }

    /**
     * Returns the Locks skill difficulty of opening the lock associated with this openable,
     * or 0 if there is no lock
     *
     * @return the lock difficulty
     */

    public int getLockDifficulty()
    {
        return lockDifficulty;
    }

    /**
     * Returns true if this Openable has a key, false otherwise
     *
     * @return whether this openable has a key
     */

    public boolean hasKey()
    {
        return keyID != null;
    }

    /**
     * Returns the Entity ID of the key for this openable, or null if there is no key
     *
     * @return the ID of the key
     */

    public String getKeyID()
    {
        return keyID;
    }

    /**
     * Returns true if the specific key from {@link #getKeyID()} is required to unlock this
     * door, false if the openable is not default locked or the lock can be picked.  Note that
     * with the keyID set to null and a key required to unlock, the lock will not be openable
     * by the player
     *
     * @return whether a specific key is required to open this openable
     */

    public boolean isKeyRequiredToUnlock()
    {
        return isKeyRequiredToUnlock;
    }

    /**
     * Returns true if the key to unlock this openable is removed from the player's inventory
     * when used, false if it not
     *
     * @return whether the key is removed from the player's inventory when used
     */

    public boolean isRemoveKeyOnUnlock()
    {
        return isRemoveKeyOnUnlock;
    }

}
