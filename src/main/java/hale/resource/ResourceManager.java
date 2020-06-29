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

package hale.resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;
import java.util.zip.ZipFile;

import hale.Game;
import hale.util.FileUtil;
import hale.util.Logger;

/**
 * The resource manager.  Keeps track of all system resources at a low level.
 * Resources are arranged in one or more packages; each package contains
 * a set of Strings referencing resource locations.
 * <p>
 * When searching for any resource, the ResourceManager searches in each
 * registered package in the ResourcePackages intrinsic order (based on their
 * PackageType) and returns the first resource found.
 *
 * @author Jared Stephen
 */

public class ResourceManager
{
    private static Map<String, String> cachedFiles = new HashMap<String, String>();

    private static List<ResourcePackage> packages = new ArrayList<ResourcePackage>(2);

    public static List<ResourcePackage> getPackages()
    {
        return packages;
    }

    /**
     * Removes the resource with the specified path from the Campaign Package, if
     * any is found.
     *
     * @param path the path of the resource to remove
     */

    public static void removeCampaignResource(String path)
    {
        for (ResourcePackage resourcePackage : packages) {
            if (resourcePackage.getType() == PackageType.Campaign) {
                resourcePackage.removeResource(path);
                break;
            }
        }
    }

    /**
     * Adds the resource with the specified path to the Campaign package, if any is found.
     * <p>
     * If the resource at this path doesn't actually exist within the Campaign package,
     * this will create problems.  Use with care.
     *
     * @param path the path of the resource to add
     */

    public static void addCampaignResource(String path)
    {
        for (ResourcePackage resourcePackage : packages) {
            if (resourcePackage.getType() == PackageType.Campaign) {
                resourcePackage.addResource(path);
                break;
            }
        }
    }

    /**
     * Gets the Resource ID of the resource of the specified resource and type.
     * This corresponds to the filename (with no extension) for a file based resource.
     *
     * @param resource the resource path (including extension) of the resource
     * @param type     the resoureType of the resource
     * @return the ID (without path or extension) of the resource
     */

    public static String getResourceIDNoPath(String resource, ResourceType type)
    {
        if (!resource.endsWith(type.getExtension())) return null;

        String id = new File(resource).getName();
        id = id.substring(0, id.length() - type.getExtension().length());
        return id;
    }

    /**
     * Gets the directory that contains the specified resource without the resource name and type
     *
     * @param resource
     * @return the directory ID
     */

    public static String getResourceDirectory(String resource)
    {
        String dir = new File(resource).getParent();

        return dir.replace('\\', '/');
    }

    /**
     * Gets the Resource ID of the resource of the specified resource and type.
     * This corresponds to the path relative to the root node (with no extension)
     * for a file based resource.
     *
     * @param resource the resource path (including extension) of the resource
     * @param type     the ResourceType of the resource
     * @return the resource ID (path from root note without extension) of the specified resource
     */

    public static String getResourceID(String resource, ResourceType type)
    {
        if (!resource.endsWith(type.getExtension())) return null;

        String id = resource.substring(0, resource.length() - type.getExtension().length());
        return id;
    }

    /**
     * Gets the Resource ID of the resource of the specified resource and type.
     * This corresponds to the path relative to the specified directory node (with no extension)
     * for a file based resource.
     *
     * @param resource  the resource path (including extension) of the resource
     * @param directory the directory the returned path with be relative to
     * @param type      the ResourceType of the resource
     * @return the resource ID (path from specified directory note without extension)
     * of the specified resource
     */

    public static String getResourceID(String resource, String directory, ResourceType type)
    {
        if (!resource.endsWith(type.getExtension())) return null;

        String id = FileUtil.getRelativePath(directory, resource);
        id = id.substring(0, id.length() - type.getExtension().length());
        return id;
    }

    /**
     * Searches for packages based on the ID of Game.curCampaign.
     * If a directory package is found, it is created and added to the Manager.
     * If no directory package is found and a zip package is found, it is
     * created and added to the manager.
     * <p>
     * If no package is found, an exception is thrown and no packages are created
     * <p>
     * Registering a package also clears any cached resources.
     */

    public static void registerCampaignPackage()
    {
        String directoryPath = "campaigns/" + Game.curCampaign.getID();
        String zipPath = directoryPath + ResourceType.Zip.getExtension();

        File directoryFile = new File(directoryPath);
        if (directoryFile.exists() && directoryFile.isDirectory()) {
            removePackageOfType(PackageType.Campaign);
            registerPackage(directoryFile, PackageType.Campaign);
        } else {
            File zipFile = new File(zipPath);
            if (zipFile.exists()) {
                removePackageOfType(PackageType.Campaign);
                registerPackage(zipFile, PackageType.Campaign);
            } else {
                throw new IllegalStateException("Package could not be found at " + directoryPath + " or " + zipPath);
            }
        }

        cachedFiles.clear();
    }

    /**
     * Searches for directory package "core" and zip package "core.zip" and creates
     * either or both if they are found.  Created packages are registered with the Manager.
     * <p>
     * Registering a package also clears any cached resources.
     */

    public static void registerCorePackage()
    {
        final String directoryPath = "core";
        final String zipPath = directoryPath + ResourceType.Zip.getExtension();

        final File directoryFile = new File(directoryPath);
        if (directoryFile.exists() && directoryFile.isDirectory()) {
            removePackageOfType(PackageType.CoreDirectory);
            registerPackage(directoryFile, PackageType.CoreDirectory);
        }

        File zipFile = new File(zipPath);
        if (zipFile.exists()) {
            removePackageOfType(PackageType.CoreZip);
            registerPackage(zipFile, PackageType.CoreZip);
        }

        cachedFiles.clear();
    }

    private static void removePackageOfType(PackageType type)
    {
        packages.removeIf(resourcePackage -> resourcePackage.getType() == type);
    }

    private static void registerPackage(final File file, final PackageType type)
    {
        if (file.isDirectory()) {
            packages.add(new DirectoryResourcePackage(file, type));
        } else
            if (file.isFile()) {
                try {
                    packages.add(new ZipResourcePackage(new ZipFile(file), type));
                } catch (Exception e) {
                    Logger.appendToErrorLog("Error reading package from " + file.getName(), e);
                }
            }

        Collections.sort(packages);
    }

    /**
     * Finds all resources contained in the specified "directory".
     * For Directory based packages, this will be all files recursively
     * contained in the specified parent directory.  For archived packages,
     * it will be based on the archive's directory structure in the same manner.
     *
     * @param directory the directory containing resources
     * @return the set of all resources contained in the directory
     */

    public static Set<String> getResourcesInDirectory(String directory)
    {
        Set<String> resources = new LinkedHashSet<>();

        for (ResourcePackage resourcePackage : packages) {
            Set<String> resourcesInPackage = resourcePackage.getResourcesIn(directory);
            if (resourcesInPackage != null) resources.addAll(resourcesInPackage);
        }

        return resources;
    }

    /**
     * Finds all resources contained in the specified "directory" in the core package(s).
     * For Directory based packages, this will be all files recursively
     * contained in the specified parent directory.  For archived packages,
     * it will be based on the archive's directory structure in the same manner.
     *
     * @param directory the directory containing resources
     * @return the set of all resources contained in the directory
     */

    public static Set<String> getCoreResourcesInDirectory(String directory)
    {
        Set<String> resources = new LinkedHashSet<>();

        for (ResourcePackage resourcePackage : packages) {
            // only use Core ZIP and Core directory packages
            switch (resourcePackage.getType()) {
                case CoreZip:
                case CoreDirectory:
                    break;
                default:
                    continue;
            }

            Set<String> resourcesInPackage = resourcePackage.getResourcesIn(directory);
            if (resourcesInPackage != null) resources.addAll(resourcesInPackage);
        }

        return resources;
    }

    /**
     * Returns true if one or more resource packages contain a resource with the specified
     * resource path, false otherwise
     *
     * @param path Resource path
     * @return whether a resource with the specified path exists
     */

    public static boolean hasResource(final String path)
    {
        for (ResourcePackage resourcePackage : packages) {
            if (resourcePackage.hasResource(path)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns a scanner for the first resource found in any registered package with the
     * specified path
     *
     * @param path the path of the resource to find
     * @return a scanner for the contents of the resource
     */

    public static Scanner getScanner(final String path)
    {
        return new Scanner(Objects.requireNonNull(getStream(path)));
    }

    /**
     * Returns a scanner for the first resource found in any registered package
     * at the location based on the resource base String (without extension) and the resource type.
     *
     * @param resource the base resource String (without extension)
     * @param type     the ResourceType of the resource
     * @return a scanner for the contents of the resource
     */

    public static Scanner getScanner(final String resource, final ResourceType type)
    {
        return new Scanner(Objects.requireNonNull(getStream(resource + type.getExtension())));
    }

    /**
     * Returns an InputStream for the first resource found in any registered package
     * at the location based on the resource base String (without extension) and the resource type.
     *
     * @param resource the base resource String (without extension)
     * @param type     the ResourceType of the resource
     * @return an InputStream for the contents of the resource
     */

    public static InputStream getStream(String resource, ResourceType type)
    {
        return getStream(resource + type.getExtension());
    }

    /**
     * Returns an InputStream for the first resource found in any registered package with the
     * specified path
     *
     * @param path the path of the resource to find
     * @return an InputStream for the contents of the resource
     */

    public static InputStream getStream(String path)
    {
        for (ResourcePackage resourcePackage : packages) {
            if (resourcePackage.hasResource(path)) {
                return resourcePackage.getStream(path);
            }
        }

        return null;
    }

    /**
     * Convenience method for getting a Reader for the resource at the specified path
     *
     * @param path the path of the resource
     * @return a Reader for the contents of the resource
     */

    public static Reader getReader(final String path)
    {
        return new InputStreamReader(Objects.requireNonNull(getStream(path)));
    }

    /**
     * Convenience method for getting a Reader for the resource with the specified ID
     * and ResourceType
     *
     * @param resource the resource ID of the resource
     * @param type     the ResourceType of the resource
     * @return a Reader for the contents of the resource
     */

    public static Reader getReader(String resource, ResourceType type)
    {
        return new InputStreamReader(getStream(resource, type));
    }

    /**
     * Returns a String representation of the package containing the resource at the specified
     * path.  The package ID returned is for the package that will be used whenever the
     * ResourceManager retrieves the resource with the specified path.
     *
     * @param path the ID String of the resource
     * @return the package ID of the ResourcePackage containing the specified resource
     */

    public static String getPackageIDOfResource(String path)
    {
        for (ResourcePackage resourcePackage : packages) {
            if (resourcePackage.hasResource(path)) {
                return resourcePackage.getType().toString();
            }
        }

        return null;
    }

    /**
     * Returns the package type containing the resource at the specified
     * path.  The package returned is for the package that will be used whenever the
     * ResourceManager retrieves the resource with the specified path.
     *
     * @param path the ID String of the resource
     * @return the package of the ResourcePackage containing the specified resource
     */

    public static PackageType getPackageTypeOfResource(String path)
    {
        for (ResourcePackage resourcePackage : packages) {
            if (resourcePackage.hasResource(path)) return resourcePackage.getType();
        }

        return null;
    }

    /**
     * A wrapper provided for convienience around the method
     * {@link #getResourceAsString(String, ResourceType)}.  This will return the
     * String contents of the specified Script resource.  That is, the Resource
     * located at "scripts/" + path + ScriptExtension (.js)
     *
     * @param scriptID the script path to the requested resource without extension or
     *                 directory prefix
     * @return the contents of the requested script Resource
     */

    public static String getScriptResourceAsString(String scriptID)
    {
        return getResourceAsString("scripts/" + scriptID + ResourceType.JavaScript.getExtension());
    }

    /**
     * Returns the contents of the resource found at the path specified
     * by the given base resource (without extension) and ResourceType
     * as a String.  Results are cached when possible for faster lookup.
     *
     * @param path the base path (without extension) of the resource
     * @param type the ResourceType of the resource
     * @return a String representing the contents of the specified resource
     */

    public static String getResourceAsString(String path, ResourceType type)
    {
        return getResourceAsString(path + type.getExtension());
    }

    /**
     * Returns the contents of the resource at the specified path
     * as a String.  Results are cached when possible for faster lookup.
     *
     * @param path the path of the resource
     * @return a String representing the contents of the specified resource.
     */

    public static String getResourceAsString(String path)
    {
        if (cachedFiles.containsKey(path)) {
            return cachedFiles.get(path);
        }

        InputStream in = getStream(path);
        if (in == null) {
            cachedFiles.put(path, null);
            return null;
        }

        String resource = getResourceAsString(in);
        cachedFiles.put(path, resource);

        return resource;
    }

    /**
     * Returns the contents of the core resource at the specified path
     * as a String.
     *
     * @param path the path of the resource
     * @return a String representing the contents of the specified resource.
     */

    public static String getCoreResourceAsString(String path)
    {
        InputStream in = null;
        for (ResourcePackage resourcePackage : packages) {
            // only use Core ZIP and Core directory packages
            switch (resourcePackage.getType()) {
                case CoreZip:
                case CoreDirectory:
                    break;
                default:
                    continue;
            }

            if (resourcePackage.hasResource(path)) {
                in = resourcePackage.getStream(path);
                break;
            }
        }

        if (in != null) {
            return getResourceAsString(in);
        } else {
            return null;
        }
    }

    /**
     * Reads the complete contents of the given InputStream until it has
     * no more output, and returns a String representation of those contents.
     * <p>
     * Note that this method can work on any InputStream, not just input
     * streams from Resources.
     *
     * @param in the InputStream to read in
     * @return a String representing the contents of the specified InputStream.
     */

    public static String getResourceAsString(InputStream in)
    {
        final char[] buffer = new char[0x10000];

        StringBuilder out = new StringBuilder();
        Reader reader = new InputStreamReader(in);

        try {
            int read;

            do {
                read = reader.read(buffer, 0, buffer.length);
                if (read > 0) {
                    out.append(buffer, 0, read);
                }
            } while (read >= 0);

        } catch (IOException e) {
            Logger.appendToErrorLog("Error reading resource as string from stream.", e);
        }

        return out.toString();
    }
}
