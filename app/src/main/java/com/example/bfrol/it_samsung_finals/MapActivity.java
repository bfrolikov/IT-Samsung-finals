package com.example.bfrol.it_samsung_finals;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private MapView mapView;
    private GoogleMap gmap;
    private ArrayList<LatLng> routePoints;
    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";
    public static final int LOCATION_PERMISSION = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Toolbar tb = findViewById(R.id.toolbar);
        tb.setTitle(getResources().getString(R.string.create_route));
        setSupportActionBar(tb);
        routePoints = new ArrayList<>();
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }
        mapView = findViewById(R.id.map_view);
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }

        mapView.onSaveInstanceState(mapViewBundle);
    }
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }
    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }
    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        gmap = googleMap;
        gmap.setMinZoomPreference(1);
        gmap.setMaxZoomPreference(30);
        gmap.setOnMapClickListener(latLng -> {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            gmap.addMarker(markerOptions);
            routePoints.add(latLng);
        });
        gmap.setOnMarkerClickListener(marker -> {
            LatLng markerPosition = marker.getPosition();
            int index = routePoints.indexOf(markerPosition);
            if(index!=-1)
            {
                routePoints.remove(index);
                drawMarkers();
            }
            return true;
        });
        UiSettings uiSettings = gmap.getUiSettings();
        uiSettings.setRotateGesturesEnabled(false);
        Location currentLocation = getLastKnownLocation();
        if (currentLocation!=null)
        {
            LatLng currentLatLng = new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());
            CameraPosition.Builder camBuilder = CameraPosition.builder();
            camBuilder.zoom(10);
            camBuilder.target(currentLatLng);
            gmap.moveCamera(CameraUpdateFactory.newCameraPosition(camBuilder.build()));
        }
    }
    private void drawMarkers()
    {
        gmap.clear();
        for(LatLng point:routePoints)
        {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(point);
            gmap.addMarker(markerOptions);
        }
    }
    private Location getLastKnownLocation() {
        if (checkForPermission(Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_DENIED)
        {
            requestRuntimePermission(Manifest.permission.ACCESS_COARSE_LOCATION,LOCATION_PERMISSION);
            return null;
        }
        LocationManager mLocationManager = (LocationManager)getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            Location l = mLocationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }
    private int checkForPermission(String permission) {
        return ContextCompat.checkSelfPermission(this, permission);
    }

    private void requestRuntimePermission(String permission, int requestCode) {
        ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION:
            {
                if (grantResults.length == 0 || grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION))
                        Toast.makeText(this, getString(R.string.location_permission_denied_explanation), Toast.LENGTH_LONG).show();
                }
                else {
                    Location currentLocation = getLastKnownLocation();
                    if (currentLocation!=null)
                    {
                        LatLng currentLatLng = new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());
                        CameraPosition.Builder camBuilder = CameraPosition.builder();
                        camBuilder.zoom(10);
                        camBuilder.target(currentLatLng);
                        if (gmap!=null)
                            gmap.moveCamera(CameraUpdateFactory.newCameraPosition(camBuilder.build()));
                    }
                }
            }
        }
    }
}

//
