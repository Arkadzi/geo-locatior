package me.gumenniy.geolocator.pojo;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Arkadiy on 16.01.2016.
 */
public class DaySet {
    List<TagImage> images;
    String date;
    double distance;

    public DaySet() {
        images = new ArrayList<>();
    }

    public static List<DaySet> imagesToSet(List<TagImage> images) {
        List<DaySet> sets = new ArrayList<>();
        Log.e("DaySet", "images size " + images.size());

        DateFormat ft = SimpleDateFormat.getDateInstance();
//                new SimpleDateFormat("yyyy.MM.dd");
        for (TagImage image : images) {
            DaySet currentSet = null;
            String date = ft.format(image.getDate());
            Log.e("DaySet", "date " + date);
            for (DaySet set : sets) {
                if (date.equals(set.getDate())) {
                    currentSet = set;
                }
            }
            if (currentSet == null) {
                currentSet = new DaySet();
                currentSet.setDate(date);
                sets.add(currentSet);
            }
            currentSet.addImage(image);
            Log.e("DaySet", "current " + currentSet.getDate() + " " + currentSet.getImages().size());
        }

        Comparator<TagImage> comparator = new Comparator<TagImage>() {
            @Override
            public int compare(TagImage lhs, TagImage rhs) {
                return (lhs.getDate().before(rhs.getDate())) ? 1 : -1;
            }
        };

        for (DaySet set : sets) {
            List<TagImage> setImages = set.getImages();
            Collections.sort(setImages, comparator);
            double distance = 0;
            for (int i = 0; i < setImages.size() - 1; i++) {

                Location point1 = new Location("1");
                point1.setLatitude(setImages.get(i).getLatitude());
                point1.setLongitude(setImages.get(i).getLongitude());

                Location point2 = new Location("2");
                point2.setLatitude(setImages.get(i + 1).getLatitude());
                point2.setLongitude(setImages.get(i + 1).getLongitude());

                distance += point1.distanceTo(point2);
            }
            set.setDistance(distance);
        }

        return sets;
    }

    public void addImage(TagImage image) {
        this.images.add(image);
    }

    public List<TagImage> getImages() {
        return images;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }
}
