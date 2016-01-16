package me.gumenniy.geolocator.pojo;

import android.graphics.Bitmap;

import java.util.Date;

/**
 * Created by Arkadiy on 15.01.2016.
 */
public class TagImage {
    private long id;
    private double latitude;
    private double longitude;
    private Date date;
    private Bitmap bitmap;

    public TagImage(long id, double latitude, double longitude, long date) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.date = new Date(date);
    }

    public long getId() {
        return id;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public Date getDate() {
        return date;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}
