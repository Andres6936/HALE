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

package main.java.hale.view;

/**
 * An interface for any widget that wants to recieve a drag from another widget
 *
 * @author Jared Stephen
 */

public interface DropTarget
{
    /**
     * Called when this widget is being hovered over by a drag target
     *
     * @param target the drag target
     */

    public void dragAndDropStartHover(DragTarget target);

    /**
     * Called when this widget has stopped being hovered over by a drag target
     *
     * @param target the drag target
     */

    public void dragAndDropStopHover(DragTarget target);

    /**
     * Called when a drag target has been dropped on this drop target
     *
     * @param target the target that has been dropped
     */

    public void dropDragTarget(DragTarget target);
}
