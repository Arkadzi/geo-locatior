package me.gumenniy.geolocator.pojo;

import android.graphics.Bitmap;

import java.util.Date;

/**
 * Represents main entity of the project - image with location tags
 */
public class TagImage {

    /**
     * MediaStore image id
     */
    private long id;

    /**
     * location latitude
     */
    private double latitude;

    /**
     * location longitude
     */
    private double longitude;

    /**
     * date of applying location to image
     */
    private Date date;

    /**
     * decoded thumbnail
     */
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
