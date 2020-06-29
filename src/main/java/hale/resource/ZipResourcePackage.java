package hale.resource;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import hale.util.Logger;

/**
 * A ResourcePackage where all resources are contained in a single ZIP file.
 * Resource paths are simply the paths for each ZipEntry
 *
 * @author Jared Stephen
 */

public class ZipResourcePackage extends ResourcePackage
{
    private final ZipFile file;

    /**
     * Create a new ZipResourcePackage with the specified zipFile and PackageType.
     * All ZipEntries in the zipFile are read in and registered as package resources.
     *
     * @param file the zipFile containing all resources
     * @param type the PackageType of this ResourcePackage
     */

    public ZipResourcePackage(ZipFile file, PackageType type)
    {
        super(type);
        this.file = file;

        Enumeration<? extends ZipEntry> e = file.entries();
        while (e.hasMoreElements()) {
            ZipEntry entry = e.nextElement();

            if (entry.isDirectory()) continue;

            entries.add(entry.getName());
        }
    }

    @Override
    public InputStream getStream(String path)
    {
        try {
            return file.getInputStream(file.getEntry(path));
        } catch (Exception e) {
            Logger.appendToErrorLog("Error getting stream from " + path, e);
        }

        return null;
    }
}