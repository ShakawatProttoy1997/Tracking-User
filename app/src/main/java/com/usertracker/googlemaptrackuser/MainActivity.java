package com.usertracker.googlemaptrackuser;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.model.LocationType;
import com.usertracker.googlemaptrackuser.ModelClass.LocationType;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    private Boolean mLocationPermissionsGranted = false;

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 16f;
    private static final String TAG = "MapActivity";

    private FusedLocationProviderClient mFusedLocationProviderClient;

    private Location currentLocation;

    ImageButton gpsBtn;

    private String apiKey = "AIzaSyBvIH2sy8DGzhNfktZlhLJfmukQHIUiKRo";

    //private DatabaseReference firebaseRefInsert;

    LocationType locationType;

    private Handler mainHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        gpsBtn = findViewById(R.id.gpsBtn);

        Places.initialize(getApplicationContext(), apiKey);

        initMap();
        getLocationPermission();

        currentLocation = new Location("");

        getDeviceLocation();

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
        try {
            if (mLocationPermissionsGranted) {
                final Task loc = mFusedLocationProviderClient.getLastLocation();
                loc.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@afu.org.checkerframework.checker.nullness.qual.NonNull Task task) {
                        try {
                            if (task.isSuccessful()) {
                                getDeviceLocation();
                                Location tempCur = (Location) task.getResult();
                                Location location = new Location(LocationManager.GPS_PROVIDER);
                                location.setLongitude(tempCur.getLongitude());
                                location.setLatitude(tempCur.getLatitude());

                            }
                        } catch (Exception ex) {
                        }
                    }
                });
            }
        } catch (SecurityException e) {
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (mLocationPermissionsGranted) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        try{
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mMap.setTrafficEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);
            mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
            LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            CameraUpdate center = CameraUpdateFactory.newLatLng(latLng);
            CameraUpdate zoom = CameraUpdateFactory.zoomTo(DEFAULT_ZOOM);
            mMap.moveCamera(center);
            mMap.animateCamera(zoom);
        }
        catch (Exception e){ }

        gpsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDeviceLocation();
            }
        });

        mainHandler.post(liveMovement);

    }

    private void initMap() {
        //Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(MainActivity.this);

    }

    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionsGranted = true;
                initMap();
            } else {
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void getDeviceLocation() {

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
        try {
            if (mLocationPermissionsGranted) {

                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {

                        try {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "onComplete: found location!");
                                currentLocation = (Location) task.getResult();

                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom
                                        (new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM));

                            } else {
                                Toast.makeText(MainActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception ex) {

                        }
                    }
                });

            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }
    }

    private Runnable liveMovement = new Runnable() {
        @Override
        public void run() {
            try{
                final DatabaseReference firabaseRefCarMovement = FirebaseDatabase.getInstance().getReference("Location");

                firabaseRefCarMovement.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        for (DataSnapshot notify : dataSnapshot.getChildren()) {
                            LocationType value = notify.getValue(LocationType.class);

                            try{

                                String lat = value.getLatitude();
                                String lng = value.getLongitude();

                                LatLng deviceLoc = new LatLng(Double.parseDouble(lat),Double.parseDouble(lng));

                                mMap.clear();
                                mMap.addMarker(new MarkerOptions().position(deviceLoc)
                                        .icon(bitmapDescriptorFromVector(MainActivity.this, R.drawable.user_location)));

                            }
                            catch (Exception e){ }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }

                });

            }
            catch (Exception ex){
            }
        }
    };

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
