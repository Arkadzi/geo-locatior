package me.gumenniy.geolocator.loader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import me.gumenniy.geolocator.loader.AbstractImageLoaderTask;

/**
 * Image loader by url (from internet)
 */
public class UrlImageLoader extends AbstractImageLoaderTask {
    @Override
    protected InputStream getInputStream(String urlString) throws IOException {
        URL url = new URL(urlString);
        return url.openStream();
    }
}
