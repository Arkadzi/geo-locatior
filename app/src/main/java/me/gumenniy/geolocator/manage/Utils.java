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

    private static ImageSize imageSize;
    private static ImageLoader imageLoader;

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

    public static String saveBitmap(Context context, Bitmap bitmap) {
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = getDiskCacheDir(context, "GeoLocatorCache");
        myDir.mkdirs();
        Random generator = new Random();
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

    public static File getDiskCacheDir(Context context, String uniqueName) {
        // Check if media is mounted or storage is built-in, if so, try and use external cache dir
        // otherwise use internal cache dir
        final String cachePath =
                Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                        !Environment.isExternalStorageRemovable() ? context.getExternalCacheDir().getPath() :
                        context.getCacheDir().getPath();

        return new File(cachePath + File.separator + uniqueName);
    }

    public static Bitmap getImageById(Context context, long id) {
        if (imageLoader == null) initImageLoader(context);
        Uri uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, String.valueOf(id));
        return imageLoader.loadImageSync(uri.toString(), imageSize);
    }

    private static void initImageLoader(Context c) {
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(ImageLoaderConfiguration.createDefault(c));
        imageSize = new ImageSize(50, 50);
    }
}
