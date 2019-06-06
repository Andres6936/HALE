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

package net.sf.hale.entity;

/**
 * An Entity Listener is any widget or other class that displays some aspect of an Entity's
 * data.  The listener will be automatically updated by the Entity whenever
 * the entity is modified.
 *
 * @author Jared Stephen
 */

public interface EntityListener
{
    /**
     * Called by the Entity that this EntityViewer is referencing whenever that
     * entity is updated or modified.
     *
     * @param entity the entity that the listener is listening to
     */

    public void entityUpdated( Entity entity );

    /**
     * Called when any listener should close and remove itself from the Widget tree (if it
     * is a widget)
     */
    public void removeListener( );
}
