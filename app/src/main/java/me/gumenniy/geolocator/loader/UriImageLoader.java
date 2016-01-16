package me.gumenniy.geolocator.loader;

import android.content.ContentResolver;
import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;

import me.gumenniy.geolocator.loader.AbstractImageLoaderTask;

/**
 * Created by Arkadiy on 15.01.2016.
 */
public class UriImageLoader extends AbstractImageLoaderTask {
    private final ContentResolver contentResolver;

    public UriImageLoader(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }

    @Override
    protected InputStream getInputStream(String urldisplay) throws IOException {
        return contentResolver.openInputStream(Uri.parse(urldisplay));
    }
}
