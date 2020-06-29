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

package main.java.hale.resource;

/**
 * All possible package types for ResourcePackage and its subclasses.
 * The intrinsic ordering of this enum is also the ordering for
 * ResourcePackages stored in the ResourceManager
 *
 * @author Jared Stephen
 */

public enum PackageType
{
    Campaign, // a campaign package, can be either zip or directory
    CoreDirectory, // core package contained in a directory hierarchy
    CoreZip; // core package contained in a zipFile
}
