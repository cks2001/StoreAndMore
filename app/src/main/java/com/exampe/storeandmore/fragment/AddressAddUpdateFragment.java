package com.exampe.storeandmore.fragment;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static com.exampe.storeandmore.fragment.AddressListFragment.addressAdapter;
import static com.exampe.storeandmore.fragment.AddressListFragment.addresses;
import static com.exampe.storeandmore.fragment.AddressListFragment.recyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.exampe.storeandmore.R;
import com.exampe.storeandmore.activity.MainActivity;
import com.exampe.storeandmore.adapter.AddressAdapter;
import com.exampe.storeandmore.helper.ApiConfig;
import com.exampe.storeandmore.helper.Constant;
import com.exampe.storeandmore.helper.Session;
import com.exampe.storeandmore.model.Address;
import com.exampe.storeandmore.model.City;

@SuppressLint("NotifyDataSetChanged")
public class AddressAddUpdateFragment extends Fragment implements OnMapReadyCallback {
    @SuppressLint("StaticFieldLeak")
    public static TextView tvCurrent;
    public static double latitude = 0.00, longitude = 0.00;
    public static Address address1;
    public static SupportMapFragment mapFragment;
    public static OnMapReadyCallback mapReadyCallback;
    View root;
    public static String cityName = "0", areaName = "0", cityId = "0", areaId = "0";
    ArrayList<City> cityArrayList, areaArrayList;
    @SuppressLint("StaticFieldLeak")
    public static TextView edtCity, edtArea;
    Button btnSubmit;
    ProgressBar progressBar;
    CheckBox chIsDefault;
    RadioButton rdHome, rdOffice, rdOther;
    Session session;
    TextView tvUpdate, edtName, edtMobile, edtAlternateMobile, edtAddress, edtLandmark, edtPinCode, edtState, edtCounty;
    ScrollView scrollView;
    String name, mobile, alternateMobile, address2, landmark, pincode, state, country, addressType, isDefault = "0";
    int position;
    Activity activity;
    String For;
    boolean isLoadMore;
    int offset;
    CityAdapter cityAdapter;
    AreaAdapter areaAdapter;
    int total = 0;

    @SuppressWarnings("deprecation")
    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_address_add_update, container, false);
        activity = getActivity();
        setHasOptionsMenu(true);

        edtCity = root.findViewById(R.id.edtCity);
        edtArea = root.findViewById(R.id.edtArea);
        edtName = root.findViewById(R.id.edtName);
        edtMobile = root.findViewById(R.id.edtMobile);
        edtAlternateMobile = root.findViewById(R.id.edtAlternateMobile);
        edtLandmark = root.findViewById(R.id.edtLandmark);
        edtAddress = root.findViewById(R.id.edtAddress);
        edtPinCode = root.findViewById(R.id.edtPinCode);
        edtState = root.findViewById(R.id.edtState);
        edtCounty = root.findViewById(R.id.edtCountry);
        btnSubmit = root.findViewById(R.id.btnSubmit);
        scrollView = root.findViewById(R.id.scrollView);
        progressBar = root.findViewById(R.id.progressBar);
        chIsDefault = root.findViewById(R.id.chIsDefault);
        rdHome = root.findViewById(R.id.rdHome);
        rdOffice = root.findViewById(R.id.rdOffice);
        rdOther = root.findViewById(R.id.rdOther);
        tvCurrent = root.findViewById(R.id.tvCurrent);
        tvUpdate = root.findViewById(R.id.tvUpdate);

        session = new Session(activity);

        edtName.setText(session.getData(Constant.NAME));
        edtAddress.setText(session.getData(Constant.ADDRESS));
        edtPinCode.setText(session.getData(Constant.PINCODE));
        edtMobile.setText(session.getData(Constant.MOBILE));
        cityId = session.getData(Constant.CITY_ID);
        areaId = session.getData(Constant.AREA_ID);

        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        cityArrayList = new ArrayList<>();
        areaArrayList = new ArrayList<>();

        Bundle bundle = getArguments();
        assert bundle != null;
        For = bundle.getString("for");
        position = bundle.getInt("position");
        if (For.equals("update")) {
            btnSubmit.setText(getString(R.string.update));
            address1 = (Address) bundle.getSerializable("model");
            cityId = address1.getId();
            areaId = address1.getArea_id();
            edtCity.setText(address1.getCity_name());
            edtArea.setText(address1.getArea_name());
            latitude = Double.parseDouble(address1.getLatitude());
            longitude = Double.parseDouble(address1.getLongitude());
            tvCurrent.setText(getString(R.string.location_1) + ApiConfig.getAddress(latitude, longitude, getActivity()));
            mapFragment.getMapAsync(this);
            SetData();
        } else {
            edtArea.setEnabled(false);
            address1 = new Address();
            progressBar.setVisibility(View.VISIBLE);
            scrollView.setVisibility(View.VISIBLE);
            btnSubmit.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }

        mapReadyCallback = googleMap -> {
            double saveLatitude, saveLongitude;
            if (latitude <= 0 || longitude <= 0) {
                saveLatitude = Double.parseDouble(session.getCoordinates(Constant.LATITUDE));
                saveLongitude = Double.parseDouble(session.getCoordinates(Constant.LONGITUDE));
            } else {
                saveLatitude = latitude;
                saveLongitude = longitude;
            }

            googleMap.clear();

            LatLng latLng = new LatLng(saveLatitude, saveLongitude);
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            googleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .draggable(true)
                    .title(getString(R.string.current_location)));

            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

            googleMap.animateCamera(CameraUpdateFactory.zoomTo(18));
        };

        btnSubmit.setOnClickListener(view -> AddUpdateAddress());

        tvUpdate.setOnClickListener(view -> displayLocationSettingsRequest(activity));

        edtCity.setOnClickListener(v -> OpenDialog(activity, "city"));

        edtArea.setOnClickListener(v -> {
            if (!cityId.equals("0") && edtArea.isEnabled()) {
                OpenDialog(activity, "area");
            } else {
                edtArea.requestFocus();
                Toast.makeText(activity, getString(R.string.select_city_first),Toast.LENGTH_SHORT).show();
            }
        });

        chIsDefault.setOnClickListener(view -> {
            if (isDefault.equalsIgnoreCase("0")) {
                isDefault = "1";
            } else {
                isDefault = "0";
            }
        });

        return root;
    }

    @Deprecated
    public void displayLocationSettingsRequest(final Activity activity) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(activity).addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(result1 -> {
            final Status status = result1.getStatus();
            switch (status.getStatusCode()) {
                case LocationSettingsStatusCodes.SUCCESS:
                    if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 110);
                    } else {
                        Fragment fragment = new MapFragment();
                        Bundle bundle1 = new Bundle();
                        bundle1.putString(Constant.FROM, "address");
                        bundle1.putDouble("latitude", latitude);
                        bundle1.putDouble("longitude", longitude);
                        fragment.setArguments(bundle1);
                        MainActivity.fm.beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();
                    }
                    break;
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    try {
                        status.startResolutionForResult(activity, 110);
                    } catch (IntentSender.SendIntentException e) {
                        Log.i("TAG", "PendingIntent unable to execute request.");
                    }
                    break;
            }
        });
    }

    void SetData() {

        name = address1.getName();
        mobile = address1.getMobile();
        address2 = address1.getAddress();
        alternateMobile = address1.getAlternate_mobile();
        landmark = address1.getLandmark();
        pincode = address1.getPincode();
        state = address1.getState();
        country = address1.getCountry();
        isDefault = address1.getIs_default();
        addressType = address1.getType();


        progressBar.setVisibility(View.VISIBLE);
        edtName.setText(name);
        edtMobile.setText(mobile);
        edtAlternateMobile.setText(alternateMobile);
        edtAddress.setText(address2);
        edtLandmark.setText(landmark);
        edtPinCode.setText(pincode);
        edtState.setText(state);
        edtCounty.setText(country);
        chIsDefault.setChecked(isDefault.equalsIgnoreCase("1"));

        if (addressType.equalsIgnoreCase("home")) {
            rdHome.setChecked(true);
        } else if (addressType.equalsIgnoreCase("office")) {
            rdOffice.setChecked(true);
        } else {
            rdOther.setChecked(true);
        }

        progressBar.setVisibility(View.GONE);

        btnSubmit.setVisibility(View.VISIBLE);
    }

    @SuppressLint("NotifyDataSetChanged")
    void AddUpdateAddress() {
        String isDefault = chIsDefault.isChecked() ? "1" : "0";
        String type = rdHome.isChecked() ? "Home" : rdOffice.isChecked() ? "Office" : "Other";
        if (cityId.equals("0")) {
            Toast.makeText(activity, "Please select city!", Toast.LENGTH_SHORT).show();
        } else if (areaId.equals("0")) {
            Toast.makeText(activity, "Please select area!", Toast.LENGTH_SHORT).show();
        } else if (edtName.getText().toString().trim().isEmpty()) {
            edtName.requestFocus();
            edtName.setError("Please enter name!");
        } else if (edtMobile.getText().toString().trim().isEmpty()) {
            edtMobile.requestFocus();
            edtMobile.setError("Please enter mobile!");

        } else if (edtAddress.getText().toString().trim().isEmpty()) {
            edtAddress.requestFocus();
            edtAddress.setError("Please enter address!");
        } else if (edtLandmark.getText().toString().trim().isEmpty()) {
            edtLandmark.requestFocus();
            edtLandmark.setError("Please enter landmark!");
        } else if (edtPinCode.getText().toString().trim().isEmpty()) {
            edtPinCode.requestFocus();
            edtPinCode.setError("Please enter pin code!");
        } else if (edtState.getText().toString().trim().isEmpty()) {
            edtState.requestFocus();
            edtState.setError("Please enter state!");

        } else if (edtCounty.getText().toString().trim().isEmpty()) {
            edtCounty.requestFocus();
            edtCounty.setError("Please enter country");
        } else {
            Map<String, String> params = new HashMap<>();
            if (For.equalsIgnoreCase("add")) {
                params.put(Constant.ADD_ADDRESS, Constant.GetVal);
            } else if (For.equalsIgnoreCase("update")) {
                params.put(Constant.UPDATE_ADDRESS, Constant.GetVal);
                params.put(Constant.ID, address1.getId());
            }

            params.put(Constant.USER_ID, session.getData(Constant.ID));
            params.put(Constant.TYPE, type);
            params.put(Constant.NAME, edtName.getText().toString().trim());
            params.put(Constant.COUNTRY_CODE, session.getData(Constant.COUNTRY_CODE));
            params.put(Constant.MOBILE, edtMobile.getText().toString().trim());
            params.put(Constant.ALTERNATE_MOBILE, edtAlternateMobile.getText().toString().trim());
            params.put(Constant.ADDRESS, edtAddress.getText().toString().trim());
            params.put(Constant.LANDMARK, edtLandmark.getText().toString().trim());
            params.put(Constant.CITY_ID, cityId);
            params.put(Constant.AREA_ID, areaId);
            params.put(Constant.PINCODE, edtPinCode.getText().toString().trim());
            params.put(Constant.STATE, edtState.getText().toString().trim());
            params.put(Constant.COUNTRY, edtCounty.getText().toString().trim());
            params.put(Constant.IS_DEFAULT, isDefault);
            if (address1 != null && (address1.getLongitude() != null && address1.getLatitude() != null)) {
                params.put(Constant.LONGITUDE, address1.getLongitude());
                params.put(Constant.LATITUDE, address1.getLatitude());
            }

            ApiConfig.RequestToVolley((result, response) -> {
                if (result) {
                    try {

                        String msg;
                        JSONObject jsonObject = new JSONObject(response);
                        if (!jsonObject.getBoolean(Constant.ERROR)) {
                            Gson g = new Gson();
                            Address address = g.fromJson(jsonObject.toString(), Address.class);
                            address.setCity_name("" + cityName);
                            address.setArea_name("" + areaName);

                            if (address.getIs_default().equals("1")) {
                                for (int i = 0; i < addresses.size(); i++) {
                                    addresses.get(i).setIs_default("0");
                                }
                            }

                            if (For.equalsIgnoreCase("add")) {
                                msg = "Address added.";
                                if (addressAdapter != null) {
                                    addresses.add(address);
                                } else {
                                    addresses = new ArrayList<>();
                                    addresses.add(address);
                                    addressAdapter = new AddressAdapter(activity, getActivity(), addresses);
                                    recyclerView.setAdapter(addressAdapter);
                                    recyclerView.setVisibility(View.VISIBLE);
                                }
                            } else {
                                addresses.set(position, address);
                                msg = "Address updated.";
                            }

                            AddressListFragment.tvAlert.setVisibility(View.GONE);

                            if (addressAdapter != null) {
                                addressAdapter.notifyDataSetChanged();
                            }

                            if (address.getIs_default().equals("1")) {
                                Constant.selectedAddressId = address.getId();
                            } else {
                                if (Constant.selectedAddressId.equals(address.getId()))
                                    Constant.selectedAddressId = "";
                            }

                            MainActivity.fm.popBackStack();
                            Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, getActivity(), Constant.GET_ADDRESS_URL, params, true);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    public void OpenDialog(Activity activity, String from) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater1 = (LayoutInflater) requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final View dialogView = inflater1.inflate(R.layout.dialog_city_area_selection, null);
        alertDialog.setView(dialogView);
        alertDialog.setCancelable(true);
        final AlertDialog dialog = alertDialog.create();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        RecyclerView recyclerView;
        NestedScrollView scrollView;
        TextView tvSearch, tvAlert;
        EditText searchView;
        ShimmerFrameLayout shimmerFrameLayout;

        scrollView = dialogView.findViewById(R.id.scrollView);
        tvSearch = dialogView.findViewById(R.id.tvSearch);
        tvAlert = dialogView.findViewById(R.id.tvAlert);
        searchView = dialogView.findViewById(R.id.searchView);
        recyclerView = dialogView.findViewById(R.id.recyclerView);
        shimmerFrameLayout = dialogView.findViewById(R.id.shimmerFrameLayout);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(linearLayoutManager);
        shimmerFrameLayout.setVisibility(View.VISIBLE);
        shimmerFrameLayout.startShimmer();

        searchView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search, 0, R.drawable.ic_close_, 0);

        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchView.getText().toString().trim().length() > 0) {
                    searchView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search, 0, R.drawable.ic_close, 0);
                } else {
                    searchView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search, 0, R.drawable.ic_close_, 0);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        if (from.equals("city")) {
            tvAlert.setText(getString(R.string.no_cities_found));
            GetCityData("", recyclerView, tvAlert, linearLayoutManager, scrollView, dialog, shimmerFrameLayout);
            tvSearch.setOnClickListener(v -> GetCityData(searchView.getText().toString().trim(), recyclerView, tvAlert, linearLayoutManager, scrollView, dialog, shimmerFrameLayout));
            searchView.setOnTouchListener((v, event) -> {
                final int DRAWABLE_RIGHT = 2;
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (searchView.getText().toString().trim().length() > 0) {
                        if (event.getRawX() >= (searchView.getRight() - searchView.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                            searchView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search, 0, R.drawable.ic_close_, 0);
                            searchView.setText("");
                            GetCityData("", recyclerView, tvAlert, linearLayoutManager, scrollView, dialog, shimmerFrameLayout);
                        }
                        return true;
                    }
                }
                return false;
            });
        } else {
            tvAlert.setText(getString(R.string.no_areas_found));
            GetAreaData("", recyclerView, tvAlert, linearLayoutManager, scrollView, dialog, shimmerFrameLayout);
            tvSearch.setOnClickListener(v -> GetAreaData(searchView.getText().toString().trim(), recyclerView, tvAlert, linearLayoutManager, scrollView, dialog, shimmerFrameLayout));
            searchView.setOnTouchListener((v, event) -> {
                final int DRAWABLE_RIGHT = 2;
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (searchView.getText().toString().trim().length() > 0) {
                        if (event.getRawX() >= (searchView.getRight() - searchView.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                            searchView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search, 0, R.drawable.ic_close_, 0);
                            searchView.setText("");
                            GetAreaData("", recyclerView, tvAlert, linearLayoutManager, scrollView, dialog, shimmerFrameLayout);
                        }
                        return true;
                    }
                }
                return false;
            });
        }

        dialog.show();
    }

    void GetCityData(String search, RecyclerView recyclerView, TextView tvAlert, LinearLayoutManager linearLayoutManager, NestedScrollView scrollView, AlertDialog dialog, ShimmerFrameLayout shimmerFrameLayout) {
        cityArrayList = new ArrayList<>();
        progressBar.setVisibility(View.VISIBLE);
        Map<String, String> params = new HashMap<>();
        params.put(Constant.SEARCH, search);
        params.put(Constant.OFFSET, "" + offset);
        params.put(Constant.LIMIT, "" + (Constant.LOAD_ITEM_LIMIT + 20));

        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (!jsonObject.getBoolean(Constant.ERROR)) {
                        try {

                            total = Integer.parseInt(jsonObject.getString(Constant.TOTAL));

                            JSONObject object = new JSONObject(response);
                            JSONArray jsonArray = object.getJSONArray(Constant.DATA);

                            Gson g = new Gson();

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                City city = g.fromJson(jsonObject1.toString(), City.class);
                                cityArrayList.add(city);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (offset == 0) {
                            progressBar.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            tvAlert.setVisibility(View.GONE);
                            cityAdapter = new CityAdapter(activity, cityArrayList, dialog);
                            cityAdapter.setHasStableIds(true);
                            recyclerView.setAdapter(cityAdapter);
                            shimmerFrameLayout.setVisibility(View.GONE);
                            shimmerFrameLayout.stopShimmer();
                            scrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {

                                // if (diff == 0) {
                                if (scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
                                    if (cityArrayList.size() < total) {
                                        if (!isLoadMore) {
                                            if (linearLayoutManager.findLastCompletelyVisibleItemPosition() == cityArrayList.size() - 1) {
                                                //bottom of list!
                                                cityArrayList.add(null);
                                                cityAdapter.notifyItemInserted(cityArrayList.size() - 1);
                                                offset += Constant.LOAD_ITEM_LIMIT + 20;

                                                Map<String, String> params1 = new HashMap<>();
                                                params1.put(Constant.SEARCH, search);
                                                params1.put(Constant.OFFSET, "" + offset);
                                                params1.put(Constant.LIMIT, "" + (Constant.LOAD_ITEM_LIMIT + 20));

                                                ApiConfig.RequestToVolley((result1, response1) -> {
                                                    if (result1) {
                                                        try {
                                                            JSONObject jsonObject1 = new JSONObject(response1);
                                                            if (!jsonObject1.getBoolean(Constant.ERROR)) {
                                                                cityArrayList.remove(cityArrayList.size() - 1);
                                                                cityAdapter.notifyItemRemoved(cityArrayList.size());

                                                                JSONObject object = new JSONObject(response1);
                                                                JSONArray jsonArray = object.getJSONArray(Constant.DATA);

                                                                Gson g = new Gson();
                                                                for (int i = 0; i < jsonArray.length(); i++) {
                                                                    JSONObject jsonObject2 = jsonArray.getJSONObject(i);
                                                                    City city = g.fromJson(jsonObject2.toString(), City.class);
                                                                    cityArrayList.add(city);
                                                                }
                                                                cityAdapter.notifyDataSetChanged();
                                                                cityAdapter.setLoaded();
                                                                isLoadMore = false;
                                                            }
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                }, activity, Constant.CITY_URL, params1, false);

                                            }
                                            isLoadMore = true;
                                        }

                                    }
                                }
                            });
                        }
                    } else {
                        shimmerFrameLayout.setVisibility(View.GONE);
                        shimmerFrameLayout.stopShimmer();
                        progressBar.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.GONE);
                        tvAlert.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    shimmerFrameLayout.setVisibility(View.GONE);
                    shimmerFrameLayout.stopShimmer();
                    progressBar.setVisibility(View.GONE);
                    e.printStackTrace();
                }
            }
        }, activity, Constant.CITY_URL, params, false);
    }

    void GetAreaData(String search, RecyclerView recyclerView, TextView tvAlert, LinearLayoutManager linearLayoutManager, NestedScrollView scrollView, AlertDialog dialog, ShimmerFrameLayout shimmerFrameLayout) {
        areaArrayList = new ArrayList<>();
        progressBar.setVisibility(View.VISIBLE);
        Map<String, String> params = new HashMap<>();
        params.put(Constant.CITY_ID, cityId);
        params.put(Constant.SEARCH, search);
        params.put(Constant.OFFSET, "" + offset);
        params.put(Constant.LIMIT, "" + (Constant.LOAD_ITEM_LIMIT + 20));

        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (!jsonObject.getBoolean(Constant.ERROR)) {
                        try {
                            total = Integer.parseInt(jsonObject.getString(Constant.TOTAL));

                            JSONObject object = new JSONObject(response);
                            JSONArray jsonArray = object.getJSONArray(Constant.DATA);

                            Gson g = new Gson();

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                City area = g.fromJson(jsonObject1.toString(), City.class);
                                areaArrayList.add(area);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (offset == 0) {
                            progressBar.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            tvAlert.setVisibility(View.GONE);
                            areaAdapter = new AreaAdapter(activity, areaArrayList, dialog);
                            areaAdapter.setHasStableIds(true);
                            recyclerView.setAdapter(areaAdapter);
                            shimmerFrameLayout.setVisibility(View.GONE);
                            shimmerFrameLayout.stopShimmer();
                            scrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {

                                // if (diff == 0) {
                                if (scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
                                    if (areaArrayList.size() < total) {
                                        if (!isLoadMore) {
                                            if (linearLayoutManager.findLastCompletelyVisibleItemPosition() == areaArrayList.size() - 1) {
                                                //bottom of list!
                                                areaArrayList.add(null);
                                                areaAdapter.notifyItemInserted(areaArrayList.size() - 1);
                                                offset += Constant.LOAD_ITEM_LIMIT + 20;

                                                Map<String, String> params1 = new HashMap<>();
                                                params1.put(Constant.CITY_ID, cityId);
                                                params1.put(Constant.SEARCH, search);
                                                params1.put(Constant.OFFSET, "" + offset);
                                                params1.put(Constant.LIMIT, "" + (Constant.LOAD_ITEM_LIMIT + 20));

                                                ApiConfig.RequestToVolley((result1, response1) -> {
                                                    if (result1) {
                                                        try {
                                                            JSONObject jsonObject1 = new JSONObject(response1);
                                                            if (!jsonObject1.getBoolean(Constant.ERROR)) {
                                                                areaArrayList.remove(areaArrayList.size() - 1);
                                                                areaAdapter.notifyItemRemoved(areaArrayList.size());

                                                                JSONObject object = new JSONObject(response1);
                                                                JSONArray jsonArray = object.getJSONArray(Constant.DATA);

                                                                Gson g = new Gson();
                                                                for (int i = 0; i < jsonArray.length(); i++) {
                                                                    JSONObject jsonObject2 = jsonArray.getJSONObject(i);
                                                                    City area = g.fromJson(jsonObject2.toString(), City.class);
                                                                    areaArrayList.add(area);
                                                                }
                                                                areaAdapter.notifyDataSetChanged();
                                                                areaAdapter.setLoaded();
                                                                isLoadMore = false;
                                                            }
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                }, activity, Constant.GET_AREA_BY_CITY, params1, false);

                                            }
                                            isLoadMore = true;
                                        }
                                    }
                                }
                            });
                        }
                    } else {
                        shimmerFrameLayout.setVisibility(View.GONE);
                        shimmerFrameLayout.stopShimmer();
                        progressBar.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.GONE);
                        tvAlert.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    shimmerFrameLayout.setVisibility(View.GONE);
                    shimmerFrameLayout.stopShimmer();
                    progressBar.setVisibility(View.GONE);
                    e.printStackTrace();
                }
            }
        }, activity, Constant.GET_AREA_BY_CITY, params, false);
    }

    static class CityAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        // for load more
        public final int VIEW_TYPE_ITEM = 0;
        public final int VIEW_TYPE_LOADING = 1;
        final Activity activity;
        final ArrayList<City> cities;
        public boolean isLoading;
        final Session session;
        final AlertDialog dialog;


        public CityAdapter(Activity activity, ArrayList<City> cities, AlertDialog dialog) {
            this.activity = activity;
            this.session = new Session(activity);
            this.cities = cities;
            this.dialog = dialog;
        }

        public void add(int position, City city) {
            cities.add(position, city);
            notifyItemInserted(position);
        }

        public void setLoaded() {
            isLoading = false;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, final int viewType) {
            View view;
            switch (viewType) {
                case (VIEW_TYPE_ITEM):
                    view = LayoutInflater.from(activity).inflate(R.layout.lyt_city_area_list, parent, false);
                    return new HolderItems(view);
                case (VIEW_TYPE_LOADING):
                    view = LayoutInflater.from(activity).inflate(R.layout.item_progressbar, parent, false);
                    return new ViewHolderLoading(view);
                default:
                    throw new IllegalArgumentException("unexpected viewType: " + viewType);
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holderParent, final int position) {

            if (holderParent instanceof HolderItems) {
                final HolderItems holder = (HolderItems) holderParent;
                try {
                    final City city = cities.get(position);
                    holder.tvPinCode.setText(city.getName());

                    holder.tvPinCode.setOnClickListener(v -> {
                        edtCity.setText(city.getName());
                        cityName = city.getName();
                        cityId = city.getId();
                        areaId = "0";
                        edtArea.setText("");
                        edtArea.setEnabled(true);
                        dialog.dismiss();
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (holderParent instanceof ViewHolderLoading) {
                ViewHolderLoading loadingViewHolder = (ViewHolderLoading) holderParent;
                loadingViewHolder.progressBar.setIndeterminate(true);
            }
        }

        @Override
        public int getItemCount() {
            return cities.size();
        }

        @Override
        public int getItemViewType(int position) {
            return cities.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        static class ViewHolderLoading extends RecyclerView.ViewHolder {
            public final ProgressBar progressBar;

            public ViewHolderLoading(View view) {
                super(view);
                progressBar = view.findViewById(R.id.itemProgressbar);
            }
        }

        static class HolderItems extends RecyclerView.ViewHolder {

            final TextView tvPinCode;

            public HolderItems(@NonNull View itemView) {
                super(itemView);

                tvPinCode = itemView.findViewById(R.id.tvPinCode);
            }
        }
    }

    static class AreaAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        // for load more
        public final int VIEW_TYPE_ITEM = 0;
        public final int VIEW_TYPE_LOADING = 1;
        final Activity activity;
        final ArrayList<City> areas;
        public boolean isLoading;
        final Session session;
        final AlertDialog dialog;


        public AreaAdapter(Activity activity, ArrayList<City> areas, AlertDialog dialog) {
            this.activity = activity;
            this.session = new Session(activity);
            this.areas = areas;
            this.dialog = dialog;
        }

        public void add(int position, City area) {
            areas.add(position, area);
            notifyItemInserted(position);
        }

        public void setLoaded() {
            isLoading = false;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, final int viewType) {
            View view;
            switch (viewType) {
                case (VIEW_TYPE_ITEM):
                    view = LayoutInflater.from(activity).inflate(R.layout.lyt_city_area_list, parent, false);
                    return new HolderItems(view);
                case (VIEW_TYPE_LOADING):
                    view = LayoutInflater.from(activity).inflate(R.layout.item_progressbar, parent, false);
                    return new ViewHolderLoading(view);
                default:
                    throw new IllegalArgumentException("unexpected viewType: " + viewType);
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holderParent, final int position) {

            if (holderParent instanceof HolderItems) {
                final HolderItems holder = (HolderItems) holderParent;
                try {
                    final City area = areas.get(position);

                    holder.tvPinCode.setText(area.getName());

                    holder.tvPinCode.setOnClickListener(v -> {
                        edtArea.setText(area.getName());
                        areaName = area.getName();
                        areaId = area.getId();
                        dialog.dismiss();
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (holderParent instanceof ViewHolderLoading) {
                ViewHolderLoading loadingViewHolder = (ViewHolderLoading) holderParent;
                loadingViewHolder.progressBar.setIndeterminate(true);
            }
        }

        @Override
        public int getItemCount() {
            return areas.size();
        }

        @Override
        public int getItemViewType(int position) {
            return areas.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        static class ViewHolderLoading extends RecyclerView.ViewHolder {
            public final ProgressBar progressBar;

            public ViewHolderLoading(View view) {
                super(view);
                progressBar = view.findViewById(R.id.itemProgressbar);
            }
        }

        static class HolderItems extends RecyclerView.ViewHolder {

            final TextView tvPinCode;

            public HolderItems(@NonNull View itemView) {
                super(itemView);

                tvPinCode = itemView.findViewById(R.id.tvPinCode);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        Constant.TOOLBAR_TITLE = activity.getString(R.string.address);
        requireActivity().invalidateOptionsMenu();
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
    public void onMapReady(@NonNull GoogleMap googleMap) {
        double saveLatitude, saveLongitude;
        if (For.equals("update")) {
            btnSubmit.setText(getString(R.string.update));
            assert getArguments() != null;
            address1 = (Address) getArguments().getSerializable("model");
            cityId = address1.getId();
            areaId = address1.getArea_id();
            latitude = Double.parseDouble(address1.getLatitude());
            longitude = Double.parseDouble(address1.getLongitude());
        }
        if (latitude <= 0 || longitude <= 0) {
            saveLatitude = Double.parseDouble(session.getCoordinates(Constant.LATITUDE));
            saveLongitude = Double.parseDouble(session.getCoordinates(Constant.LONGITUDE));
        } else {
            saveLatitude = latitude;
            saveLongitude = longitude;
        }
        googleMap.clear();

        LatLng latLng = new LatLng(saveLatitude, saveLongitude);
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .draggable(true)
                .title(getString(R.string.current_location)));

        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(18));
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        menu.findItem(R.id.toolbar_layout).setVisible(false);
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.toolbar_cart).setVisible(false);
        menu.findItem(R.id.toolbar_sort).setVisible(false);
        menu.findItem(R.id.toolbar_search).setVisible(false);
    }

}