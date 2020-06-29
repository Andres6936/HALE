package hale.resource;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * A URL stream handler for opening streams to resources within resource packages
 *
 * @author Jared
 */

public class URLResourceStreamHandler extends URLStreamHandler
{

    @Override
    protected URLConnection openConnection(URL url) throws IOException
    {
        if (!url.getProtocol().equals("resource")) {
            throw new IllegalArgumentException("May only use resource URLs");
        }

        String resourcePath = url.getPath();

        return new URLResourceConnection(url, resourcePath);
    }

}
