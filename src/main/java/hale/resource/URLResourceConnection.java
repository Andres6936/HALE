package hale.resource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * A URL connection for resources within resource packages
 *
 * @author Jared
 */

public class URLResourceConnection extends URLConnection
{
    private String resourcePath;

    protected URLResourceConnection(URL url, String resourcePath)
    {
        super(url);

        this.resourcePath = resourcePath;
    }

    /**
     * Warning.  This method is not implemented by URLResourceConnection
     */

    @Override
    public void connect() throws IOException
    {
        // not needed for our purposes, so do nothing
    }

    @Override
    public InputStream getInputStream() throws IOException
    {
        return ResourceManager.getStream(resourcePath);
    }

    @Override
    public String getContentType()
    {
        return "resource";
    }
}
