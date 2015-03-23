package com.mxmariner.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mxmariner.andxtidelib.remote.RemoteStationData;
import com.mxmariner.tides.R;
import com.mxmariner.util.HarmonicsServiceConnection;
import com.mxmariner.viewcomponent.CircleCutoutLayout;
import com.mxmariner.viewcomponent.TextViewList;

import java.util.Calendar;

public class StationActivity extends Activity {

    //region CLASS VARIABLES ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private static final String TAG = StationActivity.class.getSimpleName();
    private static final String STATION_ID = "STATION_ID";

    //endregion ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


    //region CLASS METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public static void startWithStationId(Context packageContext, Long stationId) {
        packageContext.startActivity(getStartIntent(packageContext, stationId));
    }

    public static Intent getStartIntent(Context packageContext, Long stationId) {
        Intent intent = new Intent(packageContext, StationActivity.class);
        intent.putExtra(STATION_ID, stationId);
        return intent;
    }

    //endregion ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


    //region FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    private Long stationId;

    private TextView nameTv;
    private TextView dateTv;
    private ImageView graphIv;
    private TextView predictionTv;
    private TextViewList detailsLayout;
    private GoogleMap googleMap;
    private HarmonicsServiceConnection serviceConnection = new HarmonicsServiceConnection();

    //endregion ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


    //region CONSTRUCTOR ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    //endregion ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


    //region ACCESSORS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    //endregion ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


    //region PRIVATE METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private void updateMapView(RemoteStationData remoteStationData) {
        if (remoteStationData != null && googleMap != null) {
            LatLng position = new LatLng(remoteStationData.getLatitude(), remoteStationData.getLongitude());
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 7));
            googleMap.addMarker(new MarkerOptions()
                    .position(position));
        }
    }

    private void showErrorAlert() {
        new AlertDialog.Builder(this)
                .setTitle("Whoops!")
                .setMessage("Error getting station data :(")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .show();
    }

    private void setupViewAsync() {
        new AsyncTask<Void, Void, RemoteStationData>() {
            @Override
            protected RemoteStationData doInBackground(Void... params) {
                if (serviceConnection.getHarmonicsDatabaseService() != null) {
                    long epoch = Calendar.getInstance().getTime().getTime() / 1000;
                    try {
                        int options = RemoteStationData.REQUEST_OPTION_PLAIN_DATA |
                                RemoteStationData.REQUEST_OPTION_PREDICTION |
                                RemoteStationData.REQUEST_OPTION_GRAPH_SVG;
                        return serviceConnection.getHarmonicsDatabaseService().getDataForTime(stationId, epoch, options);
                    } catch (RemoteException e) {
                        Log.e(TAG, "onServiceLoaded()", e);
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(RemoteStationData remoteStationData) {
                super.onPostExecute(remoteStationData);
                if (remoteStationData != null) {
                    nameTv.setText(remoteStationData.getName());
                    dateTv.setText(remoteStationData.getDataTimeStamp());
                    predictionTv.setText(remoteStationData.getOptionalPrediction());
                    detailsLayout.addTextViewsWithStrings(remoteStationData.getOptionalPlainData());
                    updateMapView(remoteStationData);
                    loadGraphAsync(remoteStationData);
                }
            }
        }.execute();
    }

    public void loadGraphAsync(RemoteStationData remoteStationData) {
        final String svgString = remoteStationData.getOptionalGraphSvg();
        if (svgString != null) {
            new AsyncTask<Void, Void, Bitmap>() {
                @Override
                protected Bitmap doInBackground(Void... params) {
                    try {
                        SVG svg = SVG.getFromString(svgString);
                        if (svg.getDocumentWidth() != -1) {
                            int width = (int) (svg.getDocumentWidth() * getResources().getDisplayMetrics().scaledDensity);
                            int height = (int) (svg.getDocumentHeight() * getResources().getDisplayMetrics().scaledDensity);
                            svg.setDocumentHeight(height);
                            svg.setDocumentWidth(width);

                            Bitmap newBM = Bitmap.createBitmap(width, height,
                                    Bitmap.Config.ARGB_8888);
                            Canvas bmcanvas = new Canvas(newBM);
                            // Clear background to white
                            bmcanvas.drawRGB(255, 255, 255);
                            // Render our document onto our canvas
                            svg.renderToCanvas(bmcanvas);
                            return newBM;
                        }
                    } catch (SVGParseException e) {
                        Log.e(TAG, "", e);
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Bitmap bitmap) {
                    super.onPostExecute(bitmap);
                    if (bitmap != null) {
                        graphIv.setImageBitmap(bitmap);
                    }
                }
            }.execute();
        }
    }

    //endregion ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


    //region PUBLIC METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    //endregion ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


    //region INNER CLASSES ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    //endregion ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


    //region EVENTS  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    //endregion ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


    /*~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^
                                               ANDROID
    ~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^*/


    //region LIFE CYCLE ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_station);

        MapView mapView = (MapView) findViewById(R.id.map_view);

        if (MapsInitializer.initialize(this) == 0) {
            mapView.onCreate(savedInstanceState);
            googleMap = mapView.getMap();
            googleMap.getUiSettings().setMapToolbarEnabled(false);
        } else {
            Log.w(TAG, "no google play services");
            mapView.setVisibility(View.GONE);
        }
        nameTv = (TextView) findViewById(R.id.activity_station_name);
        dateTv = (TextView) findViewById(R.id.activity_station_datetime);
        graphIv = (ImageView) findViewById(R.id.activity_station_graph_iv);
        predictionTv = (TextView) findViewById(R.id.activity_station_prediction);
        detailsLayout = (TextViewList) findViewById(R.id.activity_station_details_container);
        stationId = getIntent().getLongExtra(STATION_ID, 0l);
        if (stationId.equals(0l)) {
            showErrorAlert();
        }

        serviceConnection.startService(this, new ServiceConnectionListener());
    }

    @Override
    protected void onDestroy() {
        unbindService(serviceConnection);
        super.onDestroy();
    }

    //endregion ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


    //region IMPLEMENTATION  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    //endregion ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


    //region LISTENERS  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private class ServiceConnectionListener implements HarmonicsServiceConnection.ConnectionListener {
        @Override
        public void onServiceLoaded() {
            setupViewAsync();
        }

        @Override
        public void onServiceLoadError() {
            Log.e(TAG, "onServiceLoadError()");
            showErrorAlert();
        }

        @Override
        public void onServiceDisconnected() {
            Log.d(TAG, "onServiceDisconnected()");
        }
    }

    //endregion ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
}