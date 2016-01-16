package me.gumenniy.geolocator.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import me.gumenniy.geolocator.pojo.TagImage;

/**
 * Created by Arkadiy on 15.01.2016.
 */
public class SQLiteHelper extends SQLiteOpenHelper {
    private final static String DB_NAME = "geo_locator_db";
    private static final int DB_VERSION = 3;
    private static SQLiteHelper mInstance;

    public SQLiteHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public static SQLiteHelper getInstance(Context c) {
        if (mInstance == null) {
            mInstance = new SQLiteHelper(c.getApplicationContext());
        }
        return mInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE "
                + LocationTable.NAME + "("
                + LocationTable.ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + LocationTable.IMAGE_ID + " INTEGER,"
                + LocationTable.LATITUDE + " REAL,"
                + LocationTable.LONGITUDE + " REAL,"
                + LocationTable.DATE + " INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (DB_VERSION < newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + LocationTable.NAME);
            onCreate(db);
        }
    }

    public void write(Context c, long imageId, Location location) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(LocationTable.IMAGE_ID, imageId);
        cv.put(LocationTable.LATITUDE, location.getLatitude());
        cv.put(LocationTable.LONGITUDE, location.getLongitude());
        cv.put(LocationTable.DATE, location.getTime());
        Log.e("SQLiteHelper", String.valueOf(location.getTime()));
        db.insert(LocationTable.NAME, null, cv);
        db.close();
    }

    public List<TagImage> getImages(Context c) {
        SQLiteDatabase db = getReadableDatabase();
        List<TagImage> images = new ArrayList<>();
        Cursor cursor = db.query(LocationTable.NAME,
                new String[]{
                        LocationTable.IMAGE_ID,
                        LocationTable.LATITUDE,
                        LocationTable.LONGITUDE,
                        LocationTable.DATE
                },
                null, null, null, null, LocationTable.DATE);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    long imageId = cursor.getLong(0);
                    double latitude = cursor.getDouble(1);
                    double longitude = cursor.getDouble(2);
                    long date = cursor.getLong(3);
                    images.add(new TagImage(imageId, latitude, longitude, date));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        db.close();

        return images;
    }

    private interface LocationTable {
        String NAME = "location_table";
        String ID = "_id";
        String IMAGE_ID = "image_id";
        String LATITUDE = "latitude";
        String LONGITUDE = "longitude";
        String DATE = "date";
    }
}
