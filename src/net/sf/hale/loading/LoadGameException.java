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

package net.sf.hale.loading;

/**
 * An exception indicating that an error occurred in the process of loading a saved game file
 *
 * @author Jared
 */

public class LoadGameException extends Exception
{
    private static final long serialVersionUID = 7504635062021201430L;

    /**
     * Creates a new LoadGameException with the specified description
     *
     * @param description the description
     */

    public LoadGameException( String description )
    {
        super( description );
    }
}
