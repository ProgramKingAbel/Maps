package com.example.grace.maps;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResolvingResultCallbacks;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMarkerClickListener, LocationListener {

    private GoogleMap mMap;
    private GoogleApiClient mGooghleApiClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private Location mLastLocation;

    private LocationRequest mlocationRequest;
    private boolean mlocationUpdateStatus;

    private static final int REQUEST_CHECK_SETTINGS = 2;
    private static int placePickerRequest = 3;
    private static int placePickerRequest2 = 4;


    private void createLocationRequuest()

    {

        mlocationRequest = new LocationRequest();
        mlocationRequest.setInterval(10000);
        mlocationRequest.setFastestInterval(1000);
        mlocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mlocationRequest);

        final PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGooghleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();

                switch (status.getStatusCode()) {

                    case LocationSettingsStatusCodes.SUCCESS:
                        mlocationUpdateStatus = true;
                        startLocationUpdates();
                        break;

                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            status.startResolutionForResult(MapsActivity.this, REQUEST_CHECK_SETTINGS);

                        } catch (IntentSender.SendIntentException in) {

                        }


                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:

                        break;


                }

            }


        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS)


        {

            if (requestCode == RESULT_OK) {
                mlocationUpdateStatus = true;
                startLocationUpdates();
            }
        }
        if (requestCode == placePickerRequest) {
            if (resultCode == RESULT_OK) {
                Place myplace = PlacePicker.getPlace(this, data);
                placeMarkerOnMap(myplace.getLatLng());
            }
        }
        if (requestCode == placePickerRequest2)

        {

            if (resultCode == RESULT_OK) {
                Place thisPlace = PlacePicker.getPlace(this, data);
                LatLng latLngTo = thisPlace.getLatLng();

                Intent mapsIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?sddr=" + mLastLocation.getLatitude() + "," + mLastLocation.getLongitude() + "&daddr=" + latLngTo.latitude + "," + latLngTo.longitude));
                startActivity(mapsIntent);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        LocationServices.FusedLocationApi.removeLocationUpdates(mGooghleApiClient, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGooghleApiClient.isConnected() && !mlocationUpdateStatus) {
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;

        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGooghleApiClient,
                mlocationRequest, this);


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (mGooghleApiClient == null) {

            mGooghleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).addApi(LocationServices.API)
                    .build();
        }
        createLocationRequuest();
    }


    @Override
    protected void onStart() {
        super.onStart();

        mGooghleApiClient.connect();


    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mGooghleApiClient != null && mGooghleApiClient.isConnected()) {
            mGooghleApiClient.disconnect();
        }

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


    private void setmap() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;

        }
        mMap.setMyLocationEnabled(true);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        LocationAvailability locationAvailability = LocationServices.FusedLocationApi.getLocationAvailability(mGooghleApiClient);
        if (locationAvailability != null && locationAvailability.isLocationAvailable()) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGooghleApiClient);

            if (mLastLocation != null) {
                LatLng currentLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                placeMarkerOnMap(currentLocation);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 12));

            }
        }


    }

    private void placeMarkerOnMap(LatLng placeMarker)

    {
        MarkerOptions markerOptions = new MarkerOptions().position(placeMarker);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        //markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher)));
        String placeName = getAddress(placeMarker);
        markerOptions.title(placeName);
        mMap.addMarker(markerOptions);


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setOnMarkerClickListener(this);


        // Add a marker in Sydney and move the camera
        LatLng zalego = new LatLng(-6.2623413, 69.798756754);
        mMap.addMarker(new MarkerOptions().position(zalego).title("Marker in zalego"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(zalego, 12));
    }

    private String getAddress(LatLng mLatLng) {
        Geocoder geocoder = new Geocoder(this);
        String addressText = "";
        List<Address> addresses = null;

        Address mAddress = null;

        try

        {
            addresses = geocoder.getFromLocation(mLatLng.latitude, mLatLng.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) ;
            {
                mAddress = addresses.get(0);

                for (int counter = 0; counter < mAddress.getMaxAddressLineIndex(); counter++)

                {

                    addressText += (counter == 0) ? mAddress.getAddressLine(counter) : ("/n") + mAddress.getAddressLine(counter);
                }


            }


        } catch (IOException exception) {

        }
        return addressText;
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

        setmap();
        if (mlocationUpdateStatus) {
            startLocationUpdates();
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if (mLastLocation != null) {
            placeMarkerOnMap(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
        }

    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    public void search(View view) {
        loadPlacePicker();

    }

    public void loadPlacePicker() {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(builder.build(MapsActivity.this), placePickerRequest);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException exception) {

            exception.printStackTrace();
        }
    }


    public void moveto(View view) {

        loadPlacePickerTo();


    }

    public void loadPlacePickerTo() {
        PlacePicker.IntentBuilder build = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(build.build(MapsActivity.this), placePickerRequest2);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException exception) {
            exception.printStackTrace();
        }

    }

}

