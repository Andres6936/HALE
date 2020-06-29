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

package main.java.hale.widgets;

import main.java.hale.Game;
import main.java.hale.Keybindings;
import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.ThemeInfo;

/**
 * A button with an associated hot key binding callback
 *
 * @author Jared
 */

public class HotKeyButton extends Button
{
    private Keybindings.Binding binding;

    /**
     * Sets the hot key binding that is used for this button's tooltip and
     * click action
     *
     * @param binding
     */

    public void setHotKeyBinding(Keybindings.Binding binding)
    {
        this.binding = binding;
        super.addCallback(binding);
    }

    @Override
    protected void applyTheme(ThemeInfo themeInfo)
    {
        super.applyTheme(themeInfo);

        String tooltip = themeInfo.getParameter("tooltip", (String)null);

        String actionName = binding.getActionName();
        int key = Game.config.getKeyForAction(actionName);
        if (key != -1) {
            String keyChar = Event.getKeyNameForCode(key);

            setTooltipContent("[" + keyChar + "] " + tooltip);
        }
    }
}
