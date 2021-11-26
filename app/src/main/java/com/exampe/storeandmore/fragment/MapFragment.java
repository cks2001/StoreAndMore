package com.exampe.storeandmore.fragment;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static com.exampe.storeandmore.helper.ApiConfig.getAddress;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;

import com.exampe.storeandmore.R;
import com.exampe.storeandmore.activity.MainActivity;
import com.exampe.storeandmore.helper.Constant;
import com.exampe.storeandmore.helper.GPSTracker;

public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnMarkerDragListener, GoogleMap.OnMapLongClickListener {
    View root;
    TextView tvLocation;
    FloatingActionButton fabSatellite, fabStreet, fabCurrent;
    int mapType = GoogleMap.MAP_TYPE_NORMAL;
    SupportMapFragment mapFragment;
    Button btnUpdateLocation;
    OnMapReadyCallback mapReadyCallback;
    String from;
    Activity activity;
    private GoogleApiClient googleApiClient;
    private double longitude = 0.00, latitude = 0.00;
    private GoogleMap mMap;
    GPSTracker gpsTracker;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_map, container, false);

        activity = getActivity();
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        Objects.requireNonNull(mapFragment).getMapAsync(this);
        btnUpdateLocation = root.findViewById(R.id.btnUpdateLocation);
        tvLocation = root.findViewById(R.id.tvLocation);
        fabSatellite = root.findViewById(R.id.fabSatellite);
        fabCurrent = root.findViewById(R.id.fabCurrent);
        fabStreet = root.findViewById(R.id.fabStreet);
        setHasOptionsMenu(true);

        from = requireArguments().getString(Constant.FROM);

        if (from.equalsIgnoreCase("update")) {
            assert getArguments() != null;
            latitude = getArguments().getDouble("latitude");
            longitude = getArguments().getDouble("longitude");
        }
        if (latitude == 0.00 || longitude == 0.00) {
            gpsTracker = new GPSTracker(activity);
            latitude = gpsTracker.getLatitude();
            longitude = gpsTracker.getLongitude();
        }

        btnUpdateLocation.setOnClickListener(view -> {
            AddressAddUpdateFragment.address1.setLongitude("" + longitude);
            AddressAddUpdateFragment.address1.setLatitude("" + latitude);
            AddressAddUpdateFragment.tvCurrent.setText(getAddress(latitude, longitude, activity));
            AddressAddUpdateFragment.mapFragment.getMapAsync(AddressAddUpdateFragment.mapReadyCallback);
            MainActivity.fm.popBackStack();
        });

        mapReadyCallback = googleMap -> {
            googleMap.clear();
            LatLng latLng = new LatLng(latitude, longitude);
            googleMap.setMapType(mapType);
            googleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .draggable(true)
                    .title(getString(R.string.current_location)));
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(18));
        };

        googleApiClient = new GoogleApiClient.Builder(activity)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        fabSatellite.setOnClickListener(view -> {
            mapType = GoogleMap.MAP_TYPE_HYBRID;
            mapFragment.getMapAsync(mapReadyCallback);
        });

        fabStreet.setOnClickListener(view -> {
            mapType = GoogleMap.MAP_TYPE_NORMAL;
            mapFragment.getMapAsync(mapReadyCallback);
        });

        fabCurrent.setOnClickListener(view -> setCurrentLocation());

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mapFragment.getMapAsync(mapReadyCallback);

        return root;
    }

    @SuppressLint("SetTextI18n")
    public void setCurrentLocation() {
        // mapType = GoogleMap.MAP_TYPE_NORMAL;
        gpsTracker = new GPSTracker(activity);
        latitude = gpsTracker.getLatitude();
        longitude = gpsTracker.getLongitude();
        LatLng latLng = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .draggable(true)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                .title(getString(R.string.current_location)));

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(18));

        //tvLocation.setText("Latitude - " + latitude + "\nLongitude - " + longitude);
        tvLocation.setText(getString(R.string.location_1) + getAddress(latitude, longitude, activity));
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        mMap = googleMap;
        mMap.clear();
        LatLng latLng;
        latLng = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(latLng).draggable(true));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.setMapType(mapType);
        mMap.setOnMarkerDragListener(this);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMapClickListener(latLng1 -> {
            latitude = latLng1.latitude;
            longitude = latLng1.longitude;
            //Moving the map
            mMap.clear();
            moveMap(true);
        });
        // text.setText("Latitude - " + latitude + "\nLongitude - " + longitude);
        tvLocation.setText(getString(R.string.location_1) + getAddress(latitude, longitude, activity));
    }

    @SuppressLint("SetTextI18n")
    private void moveMap(boolean isFirst) {
        LatLng latLng = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .draggable(true)
                .title(getString(R.string.set_location)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        if (isFirst) {
            mMap.animateCamera(CameraUpdateFactory.zoomTo(18));
        }
        tvLocation.setText(getString(R.string.location_1) + getAddress(latitude, longitude, activity));
        //  text.setText("Latitude - " + latitude + "\nLongitude - " + longitude);
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        mMap.clear();
        latitude = latLng.latitude;
        longitude = latLng.longitude;
        //Moving the map
        moveMap(false);

    }


    @Override
    public void onMarkerDragStart(@NonNull Marker marker) {

    }

    @Override
    public void onMarkerDrag(@NonNull Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        latitude = marker.getPosition().latitude;
        longitude = marker.getPosition().longitude;
        moveMap(false);
    }

    @Override
    public void onStart() {
        googleApiClient.connect();
        super.onStart();
    }

    @Override
    public void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapFragment.getMapAsync(mapReadyCallback);
        Constant.TOOLBAR_TITLE = getString(R.string.app_name);
        activity.invalidateOptionsMenu();
        hideKeyboard();
    }

    public void hideKeyboard() {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(INPUT_METHOD_SERVICE);
            assert inputMethodManager != null;
            inputMethodManager.hideSoftInputFromWindow(root.getApplicationWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.toolbar_logout).setVisible(false);
        menu.findItem(R.id.toolbar_search).setVisible(false);
        menu.findItem(R.id.toolbar_sort).setVisible(false);
        menu.findItem(R.id.toolbar_cart).setVisible(false);
        menu.findItem(R.id.toolbar_layout).setVisible(false);
    }
}