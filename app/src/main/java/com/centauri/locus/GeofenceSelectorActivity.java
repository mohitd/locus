/**
 * 
 */
package com.centauri.locus;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.centauri.locus.adapter.PlacesAutoCompleteAdapter;
import com.centauri.locus.provider.Locus;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * @author mohitd2000
 * 
 */
public class GeofenceSelectorActivity extends ActionBarActivity implements OnMapClickListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, AdapterView.OnItemClickListener {

    private static final String TAG = GeofenceSelectorActivity.class.getSimpleName();

    private GoogleMap map;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private LatLng markerLoc;
    private Marker marker;
    private Circle geofenceCircle;
    private boolean hasPlacedMarker = false;
    private boolean hasPlacedGeofence = false;

    /**
     * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geofence_selector);

        googleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();

        setupIfNeeded();
        map.setOnMapClickListener(this);
        map.setMyLocationEnabled(true);

        AutoCompleteTextView placesACTextView = (AutoCompleteTextView) findViewById(R.id.placesACTextView);
        placesACTextView.setAdapter(new PlacesAutoCompleteAdapter(this, R.layout.list_item_autocomplete_place));
        placesACTextView.setOnItemClickListener(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Create Task");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * @see android.support.v4.app.FragmentActivity#onResume()
     */
    @Override
    protected void onResume() {
        super.onResume();
        setupIfNeeded();
    }

    /**
     * @see com.google.android.gms.maps.GoogleMap.OnMapClickListener#onMapClick(com.google.android.gms.maps.model.LatLng)
     */
    @Override
    public void onMapClick(LatLng location) {
        if (!hasPlacedMarker) {
            this.markerLoc = location;
            marker = map.addMarker(new MarkerOptions().position(markerLoc));
            Log.v("TAG", "markerLoc:" + markerLoc.latitude + "," + markerLoc.longitude);
            hasPlacedMarker = true;
        } else if (hasPlacedMarker && !hasPlacedGeofence) {
            CircleOptions circle = new CircleOptions();
            float[] result = new float[3];
            Location.distanceBetween(location.latitude, location.longitude, markerLoc.latitude,
                    markerLoc.longitude, result);

            if (markerLoc != null) {
                circle.center(markerLoc);
                circle.radius(result[0]);
                circle.fillColor(Color.argb(50, 51, 181, 229));
                circle.strokeColor(Color.argb(100, 0, 153, 204));
                circle.strokeWidth(5.0f);
            }

            geofenceCircle = map.addCircle(circle);
            hasPlacedGeofence = true;
            newTaskDialog();
        } else if (hasPlacedMarker && hasPlacedGeofence) {
            marker.remove();
            geofenceCircle.remove();
            hasPlacedMarker = false;
            hasPlacedGeofence = false;
        }
    }

    private void setupIfNeeded() {
        if (map == null) {
            map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        }
    }

    /**
     * @see com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener#onConnectionFailed(com.google.android.gms.common.ConnectionResult)
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    /**
     * @see com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks#onConnected(android.os.Bundle)
     */
    @Override
    public void onConnected(Bundle dataBundle) {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng latlon = new LatLng(location.getLatitude(), location.getLongitude());
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latlon, 16.0f));
    }

    private void newTaskDialog() {
        final LatLng loc = markerLoc;
        LayoutInflater inflater = getLayoutInflater();
        final View viewLayout = inflater.inflate(R.layout.new_task_dialog, null);
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setView(viewLayout);
        dialogBuilder.setTitle("Task Title");
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialogBuilder.setNeutralButton("Details", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                addGeofence((LinearLayout) viewLayout, loc, true);
                dialog.dismiss();
            }
        });
        dialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                addGeofence((LinearLayout) viewLayout, loc, false);
                dialog.dismiss();
            }
        });

        AlertDialog dialog = dialogBuilder.create();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                clearMarkers();
            }
        });
        dialog.show();
    }

    private void clearMarkers() {
        marker.remove();
        geofenceCircle.remove();
        hasPlacedMarker = false;
        hasPlacedGeofence = false;
    }

    private void addGeofence(LinearLayout viewLayout, LatLng loc, boolean details) {
        ContentValues values = new ContentValues();
        values.put(Locus.Task.COLUMN_TITLE, ((EditText) viewLayout
                .findViewById(R.id.newTaskEditText)).getText().toString());
        values.put(Locus.Task.COLUMN_DESCRIPTION, "");
        values.put(Locus.Task.COLUMN_LATITUDE, loc.latitude);
        values.put(Locus.Task.COLUMN_LONGITUDE, loc.longitude);
        values.put(Locus.Task.COLUMN_RADIUS, geofenceCircle.getRadius());
        values.put(Locus.Task.COLUMN_DUE, System.currentTimeMillis());
        values.put(Locus.Task.COLUMN_COMPLETED, 0);
        Uri uri = getContentResolver().insert(Locus.Task.CONTENT_URI, values);
        Log.i(TAG, "Added geofence: " + uri.getLastPathSegment());

        if (details) {
            Intent intent = new Intent(GeofenceSelectorActivity.this, TaskEditActivity.class);
            intent.putExtra(MainActivity.KEY_TASK_ID, ContentUris.parseId(uri));
            startActivity(intent);
        } else {
            NavUtils.navigateUpFromSameTask(this);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        String str = (String) adapterView.getItemAtPosition(position);
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocationName(str, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        double lat = addresses.get(0).getLatitude();
        double lon = addresses.get(0).getLongitude();
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 16.0f));
        onMapClick(new LatLng(lat, lon));
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }
}
