package com.example.bfrol.it_samsung_finals;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentProvider;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private MapView mapView;
    private GoogleMap gmap;
    private ArrayList<LatLng> routePoints;
    private FusedLocationProviderClient fusedLocationClient;
    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";
    public static final String ROUTE_POINTS_KEY = "routepointskey";
    public static final int LOCATION_PERMISSION = 0;
    public static final int MODE_DISPLAY = 1;
    public static final int MODE_EDIT = 2;
    public static final int MODE_ADD = 3;
    private int mode = MODE_DISPLAY;
    private String routeName = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Intent intent = getIntent();
        mode = intent.getIntExtra(UserProfileFragment.MODE_KEY, MODE_DISPLAY);
        routeName = intent.getStringExtra(UserProfileFragment.ROUTE_NAME_KEY);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        Toolbar tb = findViewById(R.id.toolbar);
        tb.setTitle("");
        setSupportActionBar(tb);
        routePoints = new ArrayList<>();
        if (mode != MODE_ADD && mode!=MODE_DISPLAY &&routeName != null) {
            for (GeoPoint point : MainActivity.currentUser.getRoutes().get(routeName)) {
                routePoints.add(new LatLng(point.getLatitude(), point.getLongitude()));
            }
        }
        else if (mode==MODE_DISPLAY)
        {
            ArrayList<LatLngSerializablePair> serRoutePoints = (ArrayList<LatLngSerializablePair>) intent.getSerializableExtra(ROUTE_POINTS_KEY);
            if(serRoutePoints!=null) {
                for (LatLngSerializablePair pair : serRoutePoints) {
                    routePoints.add(new LatLng(pair.getLatitude(), pair.getLongitude()));
                }
            }
        }
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }
        Button mapOkButton = findViewById(R.id.map_ok_button);
        EditText routeName = findViewById(R.id.route_name);
        if (this.routeName == null) {
            this.routeName = getResources().getString(R.string.new_route);
        }
        routeName.setText(this.routeName);
        if (mode == MODE_DISPLAY) {

            routeName.setKeyListener(null);//make not editable
            mapOkButton.setText(R.string.close);
            mapOkButton.setOnClickListener(caller -> {
                //send info that the user closed the map
            });
        } else if (mode == MODE_EDIT || mode == MODE_ADD) {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

            mapOkButton.setOnClickListener(caller -> {
                ArrayList<GeoPoint> routeGeoPoints = new ArrayList<>();
                for (LatLng point : routePoints) {
                    routeGeoPoints.add(new GeoPoint(point.latitude, point.longitude));
                }
                if (MainActivity.currentUser.getRoutes().containsKey(this.routeName) && mode == MODE_ADD) {
                    MainActivity.currentUser.getRoutes().put(this.routeName + "-" + getResources().getString(R.string.copy_noun), routeGeoPoints);
                } else {
                    MainActivity.currentUser.getRoutes().put(this.routeName, routeGeoPoints);
                }
                firebaseFirestore.collection("users").document(currentUser.getUid()).set(MainActivity.currentUser)
                        .addOnSuccessListener(aVoid -> {

                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        });
                finish();
            });
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
        UiSettings uiSettings = gmap.getUiSettings();
        uiSettings.setRotateGesturesEnabled(false);
        drawMarkersWithSigns();
        if (mode == MODE_EDIT || mode == MODE_ADD) {
            gmap.setOnMapClickListener(latLng -> {
                routePoints.add(latLng);
                drawMarkersWithSigns();
            });
            gmap.setOnMarkerClickListener(marker -> {
                LatLng markerPosition = marker.getPosition();
                int index = routePoints.indexOf(markerPosition);
                if (index != -1) {
                    routePoints.remove(index);
                    drawMarkersWithSigns();
                }
                return true;
            });
            if (checkForPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED) {
                requestRuntimePermission(Manifest.permission.ACCESS_COARSE_LOCATION, LOCATION_PERMISSION);
            } else {
                fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                    if (location == null) return;
                    LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    CameraPosition.Builder camBuilder = CameraPosition.builder();
                    camBuilder.zoom(10);
                    camBuilder.target(currentLatLng);
                    if (gmap != null)
                        gmap.moveCamera(CameraUpdateFactory.newCameraPosition(camBuilder.build()));
                });
            }
        } else if (mode == MODE_DISPLAY) {

        }
    }

    private void drawMarkers() {
        gmap.clear();
        for (LatLng point : routePoints) {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(point);
            gmap.addMarker(markerOptions);
        }
    }

    private void drawMarkersWithSigns() {
        gmap.clear();
        for (int i = 0; i < routePoints.size(); i++) {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(routePoints.get(i));
            if (i == 0)
                markerOptions.icon(bitmapDescriptorFromVector(this, R.drawable.ic_start_marker));
            else if (i == routePoints.size() - 1 && routePoints.size() != 1)
                markerOptions.icon(bitmapDescriptorFromVector(this, R.drawable.ic_finish_marker));

            gmap.addMarker(markerOptions);

        }
    }

    private int checkForPermission(String permission) {
        return ContextCompat.checkSelfPermission(this, permission);
    }

    private void requestRuntimePermission(String permission, int requestCode) {
        ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION: {
                if (grantResults.length == 0 || grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION))
                        Toast.makeText(getApplicationContext(), getString(R.string.location_permission_explanation), Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(getApplicationContext(), getString(R.string.location_permission_denied_explanation), Toast.LENGTH_LONG).show();
                } else {
                    fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                        if (location == null) return;
                        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        CameraPosition.Builder camBuilder = CameraPosition.builder();
                        camBuilder.zoom(10);
                        camBuilder.target(currentLatLng);
                        if (gmap != null)
                            gmap.moveCamera(CameraUpdateFactory.newCameraPosition(camBuilder.build()));
                    });
                }
            }
        }
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}


