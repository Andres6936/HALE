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

package main.java.hale.characterbuilder;

import java.util.ArrayList;
import java.util.List;

import main.java.hale.Game;
import main.java.hale.icon.Icon;
import de.matthiasmann.twl.AnimationState;
import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.GUI;

/**
 * A button that displays a single icon
 *
 * @author Jared Stephen
 */

public class IconButton extends Button
{
    private List<Callback> callbacks;

    private Icon icon;

    /**
     * Creates an icon button displaying the specified icon
     *
     * @param icon the icon to display
     */

    public IconButton(Icon icon)
    {
        if (icon != null) {
            this.icon = icon;
            this.setSize(icon.getWidth(), icon.getHeight());
        } else {
            this.setSize(Game.ICON_SIZE, Game.ICON_SIZE);
        }

        callbacks = new ArrayList<Callback>(1);

        // the left click callback
        super.addCallback(new Runnable()
        {
            @Override
            public void run()
            {
                for (Callback callback : callbacks) {
                    callback.leftClicked();
                }
            }
        });
    }

    /**
     * Sets the icon displayed by this IconButton to the specified Icon
     *
     * @param icon the icon to display
     */

    public void setIcon(Icon icon)
    {
        this.icon = icon;
    }

    @Override
    public void addCallback(Runnable runnable)
    {
        throw new UnsupportedOperationException("Only IconButton.Callback callbacks can be used " +
                "as callbacks for IconButtons.");
    }

    /**
     * Adds the specified Callback to the list of callbacks that will
     * be called whenever this button fires an event such as a click
     *
     * @param callback the Callback to add
     */

    public void addCallback(Callback callback)
    {
        callbacks.add(callback);
    }

    /**
     * Removes the specified Callback from the list of callbacks that
     * are called when this button fires an event
     *
     * @param callback the Callback to remove
     */

    public void removeCallback(Callback callback)
    {
        callbacks.remove(callback);
    }

    @Override
    public int getMinWidth()
    {
        return icon != null ? icon.getWidth() + getBorderHorizontal() : Game.ICON_SIZE + getBorderHorizontal();
    }

    @Override
    public int getMinHeight()
    {
        return icon != null ? icon.getHeight() + getBorderVertical() : Game.ICON_SIZE + getBorderVertical();
    }

    @Override
    protected boolean handleEvent(Event evt)
    {
        AnimationState animationState = getAnimationState();

        if (evt.isMouseEvent()) {
            boolean hover = (evt.getType() != Event.Type.MOUSE_EXITED) && isMouseInside(evt);

            if (hover && !animationState.getAnimationState(Button.STATE_HOVER)) {
                for (Callback callback : callbacks) {
                    callback.startHover();
                }
            } else
                if (!hover && animationState.getAnimationState(Button.STATE_HOVER)) {
                    for (Callback callback : callbacks) {
                        callback.endHover();
                    }
                }
        }

        switch (evt.getType()) {
            case MOUSE_BTNUP:
                if (evt.getMouseButton() == Event.MOUSE_RBUTTON) {
                    for (Callback callback : callbacks) {
                        callback.rightClicked();
                    }
                }
            default:
        }

        return super.handleEvent(evt);
    }

    @Override
    protected void paintWidget(GUI gui)
    {
        super.paintWidget(gui);

        if (icon != null) {
            icon.draw(getInnerX(), getInnerY());
        }
    }

    /**
     * The interface for callbacks for this Button.  Callbacks are used by
     * adding them via {@link IconButton#addCallback(Callback)}.
     *
     * @author Jared Stephen
     */

    public interface Callback
    {
        /**
         * Called whenever this IconButton is left clicked
         */
        public void leftClicked();

        /**
         * Called whenever this IconButton is right clicked
         */
        public void rightClicked();

        /**
         * Called whenever the mouse enters the area of this IconButton
         */
        public void startHover();

        /**
         * Called whenever the mouse exits the area of this IconButton
         */
        public void endHover();
    }
}
