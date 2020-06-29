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

package hale.widgets;

import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.TextArea;
import de.matthiasmann.twl.textarea.TextAreaModel;

/**
 * A text area that does not grab any events
 *
 * @author Jared
 */

public class TextAreaNoInput extends TextArea
{
    /**
     * Creates a new TextArea
     *
     * @param model the text model to use
     */

    public TextAreaNoInput(TextAreaModel model)
    {
        super(model);
        setTheme("textarea");
    }

    @Override
    protected boolean handleEvent(Event evt)
    {
        return false;
    }
}