package net.sf.hale.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import net.sf.hale.util.FileUtil;
import net.sf.hale.util.Logger;

/**
 * A ResourcePackage where all resources are contained in a folder hierarchy
 * with a single root node.  Resource paths are the paths to each file
 * in the hierarchy relative to the root node.
 *
 * @author Jared Stephen
 */

public class DirectoryResourcePackage extends ResourcePackage
{
    private final File root;

    /**
     * Create a new ResourcePackage with the specified root directory and
     * packageType.  All files recursively contained in the directory
     * will be added as resources.
     *
     * @param root the root directory of this package
     * @param type the PackageType of this package
     */

    public DirectoryResourcePackage( File root, PackageType type )
    {
        super( type );
        this.root = root;

        for ( File file : FileUtil.getFiles( root ) )
        {
            if ( file.isDirectory( ) ) continue;

            if ( file.getPath( ).contains( ".svn" ) ) continue;

            entries.add( FileUtil.getRelativePath( root, file ) );
        }
    }

    @Override
    public InputStream getStream( String path )
    {
        File file = new File( root.getPath( ) + "/" + path );
        try
        {
            return new FileInputStream( file );
        }
        catch ( Exception e )
        {
            Logger.appendToErrorLog( "Error getting resource stream from " + path, e );
        }

        return null;
    }
}
