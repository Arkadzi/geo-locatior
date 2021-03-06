package me.gumenniy.geolocator;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import me.gumenniy.geolocator.loader.AbstractImageLoaderTask;
import me.gumenniy.geolocator.loader.UriImageLoader;
import me.gumenniy.geolocator.loader.UrlImageLoader;
import me.gumenniy.geolocator.manage.Utils;

/**
 * activity which applies location to loaded image
 */
public class TagActivity extends AppCompatActivity implements LocationListener, AbstractImageLoaderTask.ImageLoaderListener {

    /**
     * request code for implicit intent for picking image from gallery
     */
    private static final int REQUEST_CODE = 1010;

    /**
     * button for defining location
     */
    private Button locationButton;

    /**
     * image view with loaded bitmap
     */
    private ImageView imageView;

    /**
     * task for loading image. Could be UriImageLoader or UrlImageLoader
     */
    private AbstractImageLoaderTask imageTask;

    /**
     * loaded bitmap
     */
    private Bitmap mBitmap;

    /**
     * location service
     */
    private LocationManager locationManager;

    /**
     * defined location
     */
    private Location mLocation;

    /**
     * progress dialog
     */
    private Dialog mDialog;

    /**
     * listener which cancels search of location
     */
    private DialogInterface.OnCancelListener locationCancel = new DialogInterface.OnCancelListener() {
        @Override
        public void onCancel(DialogInterface dialog) {
            cancelLocationSearch();
        }
    };

    /**
     * listener which cancel image loading
     */
    private DialogInterface.OnCancelListener imageCancel = new DialogInterface.OnCancelListener() {
        @Override
        public void onCancel(DialogInterface dialog) {
            cancelImageTask();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag);

        imageView = (ImageView) findViewById(R.id.user_image);
        locationButton = (Button) findViewById(R.id.geo_button);
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findLocation();
            }
        });
        Button urlButton = (Button) findViewById(R.id.url_button);
        urlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isConn()) {
                    askUserEnableInternet();
                } else {
                    showURLAlert();
                }
            }
        });
        Button storageButton = (Button) findViewById(R.id.storage_button);
        storageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendImageIntent();
            }
        });
        Button saveButton = (Button) findViewById(R.id.save_location_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBitmap == null) {
                    Toast.makeText(TagActivity.this,
                            R.string.picture_not_chosen, Toast.LENGTH_SHORT).show();
                } else if (mLocation == null) {
                    Toast.makeText(TagActivity.this,
                            R.string.location_not_chosen, Toast.LENGTH_SHORT).show();
                } else {
                    showProgressDialog(null);
                    new CachingTask(mBitmap, mLocation).execute(TagActivity.this);
                    setResult(TotalActivity.RESULT_OK);
                }
            }
        });
    }

    private void askUserEnableInternet() {
        Toast.makeText(TagActivity.this, R.string.enable_internet, Toast.LENGTH_SHORT).show();
    }

    /**
     * sends implicit intent for picking image from gallery
     */
    private void sendImageIntent() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT, null);
        galleryIntent.setType("image/*");
        galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);

        Intent chooser = new Intent(Intent.ACTION_CHOOSER);
        chooser.putExtra(Intent.EXTRA_INTENT, galleryIntent);
        chooser.putExtra(Intent.EXTRA_TITLE, getString(R.string.choose_picture));

        startActivityForResult(chooser, REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
            Log.e("Utils", "onActivityResult OK" + data.getData());
            Uri uri = data.getData();
            launchImageLoader(uri.toString(), new UriImageLoader(getContentResolver()));
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * called when bitmap was loaded by imageTask
     *
     * @param bitmap loaded bitmap. could be null if loading was not successful
     */
    @Override
    public void onLoad(Bitmap bitmap) {
        dismissProgressDialog();
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
            mBitmap = bitmap;
        } else {
            Toast.makeText(this, R.string.error_message, Toast.LENGTH_SHORT).show();
            Log.e("TagActivity", "bitmap is null");
        }
    }

    /**
     * updates text on locationButton with defined location
     */
    private void updateUI() {
        locationButton.setText(mLocation.getLatitude() + " " + mLocation.getLongitude());
    }

    /**
     * tries to find location of device
     */
    private void findLocation() {
        Log.e("TagingActivity", "findLocation()");
        if (locationManager == null)
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            if (!isConn()) {
                askUserEnableInternet();
            } else {
                locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, TagActivity.this, null);
                showProgressDialog(locationCancel);
            }
        } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, TagActivity.this, null);
            showProgressDialog(locationCancel);
        } else {
            showSettingsAlert();
        }
    }

    /**
     * checks whether network connectivity is set
     *
     * @return true if connectivity is set, false otherwise
     */
    public boolean isConn() {
        ConnectivityManager connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity.getActiveNetworkInfo() != null) {
            if (connectivity.getActiveNetworkInfo().isConnected())
                return true;
        }
        return false;
    }

    public void dismissProgressDialog() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            cancelLocationSearch();
            cancelImageTask();
            dismissProgressDialog();
        }
    }

    /**
     * tries to cancel image loading task
     */
    private void cancelImageTask() {
        if (imageTask != null) {
            imageTask.cancel(false);
            imageTask.setListener(null);
        }
    }

    /**
     * cancels location search
     */
    private void cancelLocationSearch() {
        if (locationManager != null)
            locationManager.removeUpdates(this);
    }


    /**
     * shows alert dialog which asks user to enable network and gps navigation
     */
    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(R.string.settings_alert_title);
        alertDialog.setMessage(R.string.settings_alert_message);

        alertDialog.setPositiveButton(R.string.settings, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });

        alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialog.show();
    }

    /**
     * shows alert dialog with field for entering url for desired image
     */
    private void showURLAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Enter URL");
        View view = View.inflate(this, R.layout.dialog_url, null);
        alertDialog.setView(view);
        final EditText urlView = (EditText) view.findViewById(R.id.url_view);

        alertDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String url = urlView.getText().toString();
//                String url = "https://cdn.photographylife.com/wp-content/uploads/2014/06/Nikon-D810-Image-Sample-6.jpg";
                launchImageLoader(url, new UrlImageLoader());
            }
        });

        alertDialog.show();
    }

    /**
     * launches appropriate ImageLoader instance
     *
     * @param url  could be url or uri
     * @param task image loader
     */
    private void launchImageLoader(String url, AbstractImageLoaderTask task) {
        cancelImageTask();
        if (!url.isEmpty()) {
            imageTask = task;
            imageTask.setListener(TagActivity.this);
            imageTask.execute(url);
            showProgressDialog(imageCancel);
        }
    }

    /**
     * shows cancellable progress dialog
     *
     * @param onCancelListener appropriate cancel listener
     */
    public void showProgressDialog(DialogInterface.OnCancelListener onCancelListener) {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage(getString(R.string.please_wait));
        dialog.setOnCancelListener(onCancelListener);
        dialog.setButton(ProgressDialog.BUTTON_POSITIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        mDialog = dialog;
        mDialog.show();

    }

    @Override
    public void onLocationChanged(Location location) {
        dismissProgressDialog();
        mLocation = location;
        updateUI();
        Log.e("TagingActivity", "onLocationChanged() " + location.getLatitude() + " " + location.getLongitude());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.e("TagingActivity", "onStatusChanged()");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.e("TagingActivity", "onProviderEnabled() " + provider);
        findLocation();
    }

    @Override
    public void onProviderDisabled(String provider) {
        if (!(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))) {
            dismissProgressDialog();
            showSettingsAlert();
        }
    }

    /**
     * Task for applying defined location to loaded image
     */
    public class CachingTask extends AsyncTask<Context, Void, Boolean> {
        /**
         * loaded image
         */
        private final Bitmap bitmap;

        /**
         * defined location
         */
        private final Location location;


        public CachingTask(Bitmap bitmap, Location location) {
            this.bitmap = bitmap;
            this.location = location;
        }

        @Override
        protected Boolean doInBackground(Context... params) {
            return Utils.cacheBitmap(params[0], bitmap, location);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            dismissProgressDialog();
            if (aBoolean) {
                Toast.makeText(TagActivity.this,
                        R.string.image_saved, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(TagActivity.this,
                        R.string.wrong_caching, Toast.LENGTH_SHORT).show();
            }
        }
    }

}