package me.gumenniy.geolocator.loader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Arkadiy on 15.01.2016.
 */
public abstract class AbstractImageLoaderTask extends AsyncTask<String, Void, Bitmap> {
    ImageLoaderListener listener;
    private Bitmap mBitmap;
    private boolean isLoaded;
    private int mode;

    protected Bitmap doInBackground(String... urls) {
        String urldisplay = urls[0];
        Bitmap bitmap = null;
        try {
            final int IMAGE_MAX_SIZE = 1000000;

            InputStream in = getInputStream(urldisplay);

            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, o);
            in.close();
            if (!isCancelled()) {

                int scale = 1;
                while ((o.outWidth * o.outHeight) * (1 / Math.pow(scale, 2)) >
                        IMAGE_MAX_SIZE) {
                    scale++;
                }
                in = getInputStream(urldisplay);
                if (scale > 1) {
                    scale--;
                    o = new BitmapFactory.Options();
                    o.inSampleSize = scale;
                    bitmap = BitmapFactory.decodeStream(in, null, o);

                } else {
                    bitmap = BitmapFactory.decodeStream(in);
                }
                in.close();
                Log.e("AbstractImageLoaderTask", "bitmap size - width: " + bitmap.getWidth() + ", height: " +
                        bitmap.getHeight());
            }
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return bitmap;
    }

    protected abstract InputStream getInputStream(String urldisplay) throws IOException;

    protected void onPostExecute(Bitmap result) {
        Log.e("AbstractImageLoaderTask", "onPostExecute() " + isCancelled());
        isLoaded = true;
        mBitmap = result;
        if (listener != null) {
            listener.onLoad(mBitmap);
        }
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    public void setListener(ImageLoaderListener listener) {
        this.listener = listener;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public interface ImageLoaderListener {
        void onLoad(Bitmap bitmap);
    }

}
