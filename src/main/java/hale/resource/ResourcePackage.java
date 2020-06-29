package hale.resource;

import java.io.InputStream;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A set of Strings representing the locations of resources.  The resources themselves can
 * be contained in a directory structure or compressed archive.
 * <p>
 * Resources can be accessed via their unique ID string, which is also the location of
 * the resource's corresponding file in the directory structure or zip.
 * <p>
 * ResourcePackages are handled by the {@link ResourceManager}.
 *
 * @author Jared Stephen
 */

public abstract class ResourcePackage implements Comparable<ResourcePackage>
{
    protected final TreeSet<String> entries;
    private final PackageType type;

    /**
     * Create a new ResourcePackage with the specified package type.
     * The Package will initially have no entries.
     *
     * @param type the PackageType of this ResourcePackage
     */

    public ResourcePackage(PackageType type)
    {
        entries = new TreeSet<String>();
        this.type = type;
    }

    /**
     * Removes the resource with the specified path from this ResourcePackage
     *
     * @param path the path of the resource to remove
     */

    public void removeResource(String path)
    {
        entries.remove(path);
    }

    /**
     * Adds the specified path to the list of valid resources.
     *
     * @param path the path to add
     */

    public void addResource(String path)
    {
        entries.add(path);
    }

    /**
     * Finds all resources contained in the specified "directory".
     * For Directory based packages, this will be all files recursively
     * contained in the specified parent directory.  For archived packages,
     * it will be based on the archive's directory structure in the same manner.
     *
     * @param directory the parent directory
     * @return the set of resources contained in the specified directory
     */

    public SortedSet<String> getResourcesIn(String directory)
    {
        String first = entries.ceiling(directory + "/");
        String last = entries.ceiling(directory + Character.MAX_VALUE);

        if (first == null) return null;

        if (last == null) return entries.tailSet(first);

        return entries.subSet(first, last);
    }

    /**
     * Returns the InputStream representation of the resource contained at the
     * location specified by path.  The function allows you to read the contents
     * of the resource as if it were a file.
     *
     * @param path the path of the resource to read
     * @return an InputStream which when read yields the resource at the specified path.
     */

    public abstract InputStream getStream(String path);

    /**
     * Returns the PackageType of this ResourcePackage
     *
     * @return the PackageType of this ResourcePackage.
     */

    public PackageType getType()
    {
        return type;
    }

    /**
     * Returns true if the set of resources in this package contains a resource with the specified
     * path, false otherwise
     *
     * @param path the path of the resource to search for
     * @return true if this package contains the specified resource, false otherwise
     */

    public boolean hasResource(String path)
    {
        return entries.contains(path);
    }

    /**
     * Computes the path String of the resource based on the base resource String provided
     * and the resource type, then returns whether this package contains that resource
     *
     * @param resource the base resource (without any extension) for the resource to be located
     * @param type     the ResourceType of the resource to be located
     * @return true if this package contains the resource, false otherwise
     */

    public boolean hasResource(String resource, ResourceType type)
    {
        return entries.contains(resource + type.getExtension());
    }

    /**
     * Computes the path String of the resource based on the base resource provided and the
     * resource type, then returns the InputStream representation of the contents of that
     * resource.
     *
     * @param resource the base resource (without any extension) for the resource to be located
     * @param type     the ResourceType of the resource to be located
     * @return an InputStream representation of the contents of the resource
     */

    public InputStream getStream(String resource, ResourceType type)
    {
        return getStream(resource + type.getExtension());
    }

    /**
     * Compares this ResourcePackage with another ResourcePackage based on the intrinsic ordering
     * of this packageType versus the other's packageType.
     *
     * @param other The package to compare this package with
     * @return an integer comparing this package with other based on the PackageTypes
     */

    @Override
    public int compareTo(ResourcePackage other)
    {
        return this.type.compareTo(other.type);
    }
}
