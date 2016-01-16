package me.gumenniy.geolocator;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import me.gumenniy.geolocator.db.SQLiteHelper;
import me.gumenniy.geolocator.manage.Utils;
import me.gumenniy.geolocator.pojo.TagImage;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Log.e("MapActivity", "omnMapReady()");
        if (getIntent() != null) {
            String date = getIntent().getStringExtra(TotalActivity.DATE);
            if (date != null) {
                new ImageLoaderTask(date).execute(this);
            }
        }
        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    class ImageLoaderTask extends AsyncTask<Context, Void, List<TagImage>> {
        String mDate;

        public ImageLoaderTask(String mDate) {
            this.mDate = mDate;
        }

        @Override
        protected List<TagImage> doInBackground(Context... params) {
            SQLiteHelper helper = SQLiteHelper.getInstance(params[0]);

            List<TagImage> images = helper.getImages(params[0]);
            List<TagImage> dayImages = new ArrayList<>();

            DateFormat ft = DateFormat.getDateInstance();

            for (TagImage image : images) {
                if (ft.format(image.getDate()).equals(mDate)) {
                    dayImages.add(image);
                }
            }
            for (TagImage image : dayImages) {
                image.setBitmap(Utils.getImageById(MapsActivity.this, image.getId()));
            }
            return dayImages;
        }

        @Override
        protected void onPostExecute(List<TagImage> tagImages) {
            updateMap(tagImages);
        }
    }

    private void updateMap(List<TagImage> tagImages) {
        if (tagImages.size() > 0) {
            LatLngBounds.Builder builder = LatLngBounds.builder();
            DateFormat ft = DateFormat.getTimeInstance();
            for (TagImage image : tagImages) {

                LatLng latLng = new LatLng(image.getLatitude(), image.getLongitude());
                BitmapDescriptor itemBitmap = BitmapDescriptorFactory.fromBitmap(image.getBitmap());
                builder.include(latLng);

                mMap.addMarker(new MarkerOptions().position(latLng).icon(itemBitmap).title(ft.format(image.getDate())));
            }
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 50));
        }
    }
}
