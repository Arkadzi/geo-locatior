package me.gumenniy.geolocator.manage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageSize;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.Random;

import me.gumenniy.geolocator.db.SQLiteHelper;

/**
 * Created by Arkadiy on 15.01.2016.
 */
public class Utils {

    /**
     * size for converted thumbnail
     */
    private static ImageSize imageSize;

    /**
     * UIL instance
     */
    private static ImageLoader imageLoader;

    /**
     * obtains MediaStore image id by its URI
     * @param context used for ContentResolver instance
     * @param contentUri uri of appropriate image
     * @return MediaStore image id
     */
    public static long getImageIdFromURI(Context context, Uri contentUri) {
        long imageId = 0;
        String[] proj = {MediaStore.Images.Media._ID};
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndex(MediaStore.Images.Media._ID);
            cursor.moveToFirst();
            imageId = cursor.getLong(column_index);
            cursor.close();
        }
        Log.e("Utils", "imageId " + imageId);
        return imageId;
    }

    /**
     * caches bitmap into storage and adds record to local database
     * @param context used for getting ContentResolver instance
     * @param bitmap caching bitmap
     * @param location applying location
     * @return true if success, false otherwise
     */
    public static boolean cacheBitmap(final Context context, Bitmap bitmap, final Location location) {
        final String filePath = saveBitmap(context, bitmap);
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Images.Media.DATA, filePath);
        Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);
        long imageId = getImageIdFromURI(context, uri);
        if (imageId > 0) {
            SQLiteHelper helper = SQLiteHelper.getInstance(context);
            helper.write(imageId, location);
            return true;
        } else {
            return false;
        }
    }

    /**
     * saves bitmap into cache folder
      * @param context used for defining path to cache folder
     * @param bitmap bitmap for store
     * @return absolute path to saved bitmap
     */
    public static String saveBitmap(Context context, Bitmap bitmap) {
        File myDir = getDiskCacheDir(context, "GeoLocatorCache");
        myDir.mkdirs();
        Calendar c = Calendar.getInstance();
        long millis = c.getTimeInMillis();
        String fname = "Image-" + millis + ".jpg";
        File file = new File(myDir, fname);
        Log.e("Utils", "" + file);
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file.getAbsolutePath();
    }

    /**
     * defines file which represents cache directory
     * @param context used for defining path to cache directory
     * @param uniqueName name of cache directory
     * @return file representation of cache directory
     */
    public static File getDiskCacheDir(Context context, String uniqueName) {
        final String cachePath =
                Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                        !Environment.isExternalStorageRemovable() ? context.getExternalCacheDir().getPath() :
                        context.getCacheDir().getPath();

        return new File(cachePath + File.separator + uniqueName);
    }

    /**
     * returns bitmap for appropriate MediaStore image id, using UILoader
     * @param context used for instantiating UILoader
     * @param id MediaStore id
     * @return decoded bitmap
     */
    public static Bitmap getImageById(Context context, long id) {
        if (imageLoader == null) initImageLoader(context);
        Uri uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, String.valueOf(id));
        return imageLoader.loadImageSync(uri.toString(), imageSize);
    }

    /**
     * initiates UILoader
     * @param c used for creating of loading configuration
     */
    private static void initImageLoader(Context c) {
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(ImageLoaderConfiguration.createDefault(c));
        imageSize = new ImageSize(50, 50);
    }
}
