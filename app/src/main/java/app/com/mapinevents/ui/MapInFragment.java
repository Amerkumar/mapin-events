package app.com.mapinevents.ui;

import android.Manifest;
import android.accessibilityservice.AccessibilityService;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.wrdlbrnft.sortedlistadapter.SortedListAdapter;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.indooratlas.android.sdk.IALocation;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.Polygon;
import com.mapbox.mapboxsdk.annotations.PolygonOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.maps.SupportMapFragment;
import com.mapbox.mapboxsdk.style.layers.FillExtrusionLayer;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.light.Light;
import com.mapbox.mapboxsdk.style.light.Position;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import app.com.mapinevents.R;
import app.com.mapinevents.databinding.MapInFragmentBinding;
import app.com.mapinevents.model.POI;
import app.com.mapinevents.ui.adapters.POIAdapter;
import app.com.mapinevents.utils.MapInConstants;
import app.com.mapinevents.utils.Utils;
import app.com.mapinevents.viewmodels.MapInViewModel;
import app.com.mapinevents.viewmodels.SharedViewModel;

import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.rgba;
import static com.mapbox.mapboxsdk.style.layers.Property.NONE;
import static com.mapbox.mapboxsdk.style.layers.Property.TEXT_ANCHOR_BOTTOM;
import static com.mapbox.mapboxsdk.style.layers.Property.TEXT_ANCHOR_LEFT;
import static com.mapbox.mapboxsdk.style.layers.Property.TEXT_ANCHOR_RIGHT;
import static com.mapbox.mapboxsdk.style.layers.Property.TEXT_ANCHOR_TOP;
import static com.mapbox.mapboxsdk.style.layers.Property.TEXT_JUSTIFY_AUTO;
import static com.mapbox.mapboxsdk.style.layers.Property.VISIBLE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillExtrusionColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillExtrusionHeight;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillExtrusionOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textField;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textJustify;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textRadialOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textSize;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textVariableAnchor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;

public class MapInFragment extends Fragment
        implements SensorEventListener {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int REQUEST_CHECK_SETTINGS = 2;
    private static final int PERMISSIONS_REQUEST_ACCESS_CHANGE_WIFI_STATE = 3;

    public final static int REQUEST_LOCATION = 199;

    private MapInViewModel mViewModel;
    private Light light;
    private MapInFragmentBinding binding;
    private boolean isInitPosition;
    private boolean is3denabled;
    private boolean isToggleSymbolLayer;
    private SensorManager sensorManager;
    private SensorControl sensorControl;
    private float[] gravityArray;
    private float[] magneticArray;
    private float[] inclinationMatrix = new float[9];
    private float[] rotationMatrix = new float[9];
    private List<POI> mPoiModels;


    // Amplifiers that translate small phone orientation movements into larger viewable map changes.
// Pitch is negative to compensate for the negative readings from the device while face up
// 90 is used based on the viewable angle when viewing the map (from phone being flat to facing you).
    private static final int PITCH_AMPLIFIER = -90;
    private static final int BEARING_AMPLIFIER = 90;
    private MapboxMap mMap;
    private SymbolLayer hallsLayer;
    private SymbolLayer stallsLayer;
    private SharedViewModel model;
    private MenuItem searchItem;


    private static final Comparator<POI> COMPARATOR_POI = new SortedListAdapter.ComparatorBuilder<POI>()
            .setOrderForModel(POI.class, (a, b) -> Integer.signum(a.getRank() - b.getRank()))
            .build();
    private ObjectAnimator mAnimator;
    private View rootView;

    private SupportMapFragment mapFragment;
    private FragmentTransaction transaction;
    private SearchView searchView;
    private LocationManager locationManager;
    private Marker locationMarker;
//    private Polygon circlePolygon;
    private Polygon mCirclePolygon;
    private Marker mLocationMarker;

    public static MapInFragment newInstance() {
        return new MapInFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(getContext(), getString(R.string.mapbox_access_token));
        setHasOptionsMenu(true);
        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            sensorControl = new SensorControl(sensorManager);
            registerSensorListener();
        }


        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        // get location permission
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // permission granted
            // check for gps location services
//            Log.d(TAG, "Location Permission Granted");

            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//                getLocationUpdates();
                Log.d("About GPS", "GPS is Enabled in your devide");
            } else {
                //showAlert
                getLocationSettings();
            }

        } else {
            // show rationale to user and request permission
            requestPermissions(
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
//            Log.d(TAG, "Location Permission Not Granted");
        }

        WifiManager wifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);
    }



    private LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }


    // location permission result
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                //if request is denied -- show rationale why this permission is important
                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION)) {
                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                    new MaterialAlertDialogBuilder(getContext(), R.style.AlertDialogTheme)
                            .setTitle("Location Permission")
                            .setMessage("We need your location for indoor navigation.")
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    requestPermissions(
                                            new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                                }
                            }).show();



                } else {

                    // in case when location permission is not on but gps is on
//                    getLocationUpdates();
                    if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//                        getLocationUpdates();
                        Log.d("About GPS", "Location Permission granted now for gps");
                    } else {
                        //showAlert
                        getLocationSettings();
                    }

                }
            }
        }
    }

    private void getLocationSettings() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(createLocationRequest());


        SettingsClient client = LocationServices.getSettingsClient(getActivity());
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(getActivity(), new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
//                Log.d(TAG, "GPS Is on");
//                getLocationUpdates();
            }
        });

        task.addOnFailureListener(getActivity(), new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(getActivity(),
                                REQUEST_LOCATION);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });


    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        if (rootView == null) {
            binding = MapInFragmentBinding.inflate(inflater, container, false);
            rootView = binding.getRoot();
        }
        return binding.getRoot();
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

//        inflater.inflate(R.menu.mapin_fragment_search_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mViewModel = ViewModelProviders.of(this).get(MapInViewModel.class);
        model = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);

        model.getIaLocationMutableLiveData().observe(this, new Observer<IALocation>() {
            @Override
            public void onChanged(IALocation iaLocation) {

                  showBlueDot(new LatLng(iaLocation.getLatitude(), iaLocation.getLongitude()), iaLocation.getAccuracy(),
                          iaLocation.getBearing());
            }
        });

        POIAdapter poiAdapter = new POIAdapter(getContext(), POI.class, COMPARATOR_POI, poi -> {
            Utils.hideKeyboard(getActivity());
            MapInFragmentDirections.ActionMapInFragmentToMapInSelectionFragment actionMapInFragmentToMapInSelectionFragment =
                    MapInFragmentDirections.actionMapInFragmentToMapInSelectionFragment(poi);
            Navigation.findNavController(binding.getRoot()).navigate(actionMapInFragmentToMapInSelectionFragment);
        });

        binding.poiRecyclerView.setAdapter(poiAdapter);
        binding.poiRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mViewModel.getmAgendaObservable().observe(this, new Observer<List<POI>>() {
            @Override
            public void onChanged(List<POI> pois) {
                if (pois != null) {
                    binding.progressHorizontal.setVisibility(View.GONE);
                    poiAdapter.edit().removeAll().commit();
                    poiAdapter.edit()
                            .replaceAll(pois)
                            .commit();
                    mPoiModels = pois;
                } else {
                    binding.progressHorizontal.setVisibility(View.VISIBLE);
                }
            }
        });
        if (searchItem == null) {
            binding.toolbar.inflateMenu(R.menu.mapin_fragment_search_menu);
            searchItem = binding.toolbar.getMenu().findItem(R.id.m_search);
            searchView = (SearchView) searchItem.getActionView();

//        searchItem = binding.toolbar.getMenu().getItem(R.id.m_search);
//
            MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
                @Override
                public boolean onMenuItemActionCollapse(MenuItem item) {
                    // Called when SearchView is collapsing
//                if (mSearchItem.isActionViewExpanded()) {
//                    animateSearchToolbar(1, false, false);
//                }
                    if (searchItem.isActionViewExpanded()) {
//                        animateSearchToolbar(1, false, false);
                        binding.mapFragmentContainer.setVisibility(View.VISIBLE);
                        Utils.showView(getActivity().findViewById(R.id.bottom_nav));
                        binding.symbolLayerToggleFab.setVisibility(View.VISIBLE);
                        binding.lightFab.setVisibility(View.VISIBLE);
                        binding.poiRecyclerView.setVisibility(View.GONE);
                    }
                    return true;
                }

                @Override
                public boolean onMenuItemActionExpand(MenuItem item) {
                    // Called when SearchView is expanding
//                animateSearchToolbar(1, true, true);
//                    animateSearchToolbar(1, true, true);
                    binding.mapFragmentContainer.setVisibility(View.GONE);
                    Utils.hideView(getActivity().findViewById(R.id.bottom_nav));

                    binding.symbolLayerToggleFab.setVisibility(View.GONE);
                    binding.lightFab.setVisibility(View.GONE);
                    binding.poiRecyclerView.setVisibility(View.VISIBLE);

                    return true;
                }
            });


        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (mPoiModels != null) {
                    final List<POI> filteredModelList = filter(mPoiModels, newText);
                    poiAdapter.edit()
                            .replaceAll(filteredModelList)
                            .commit();
                    Log.d("", String.valueOf(filteredModelList.size()));


                }
                return false;
            }
        });


        if (savedInstanceState == null) {
            transaction = getActivity().getSupportFragmentManager()
                    .beginTransaction();

            MapboxMapOptions options = MapboxMapOptions.createFromAttributes(getContext(), null);
            options.camera(new CameraPosition.Builder()
                    .target(new LatLng(24.901317946386918, 67.07612826571307))
                    .bearing(145)
                    .tilt(45)
                    .zoom(17)
                    .build());

            mapFragment = SupportMapFragment.newInstance(options);

            transaction.add(R.id.map_fragment_container, mapFragment, "com.mapbox.map");
            transaction.commit();
        } else {
            Log.d("", "Map Fragment COm");
            mapFragment = (SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentByTag("com.mapbox.map");
        }

//        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPref.edit();
//        editor.putBoolean(getString(R.string.mapbox_), false);
//        editor.commit();

        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        int mapStyle = sharedPref.getInt(getString(R.string.map_style), MapInConstants.MAPBOX_STREET);
        boolean hallsVisible = sharedPref.getBoolean(getString(R.string.labels_halls), true);
        boolean stallsVisible = sharedPref.getBoolean(getString(R.string.labels_stalls), false);
        boolean threeDimenViewEnable = sharedPref.getBoolean(getString(R.string.three_dimen_view), true);

        model.getMapTypeSelected().observe(getActivity(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                if (mMap != null) {

                    switch (integer) {
                        case MapInConstants.MAPBOX_STREET:
                            mMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
                                @Override
                                public void onStyleLoaded(@NonNull Style style) {
                                    setupMainLayer(style);
                                    setupLight(style);
                                }
                            });
                            break;
                        case MapInConstants.MAPBOX_LIGHT:
                            mMap.setStyle(Style.LIGHT, new Style.OnStyleLoaded() {
                                @Override
                                public void onStyleLoaded(@NonNull Style style) {
                                    setupMainLayer(style);
                                    setupLight(style);
                                }
                            });
                            break;
                        case MapInConstants.MAPBOX_DARK:
                            mMap.setStyle(Style.DARK, new Style.OnStyleLoaded() {
                                @Override
                                public void onStyleLoaded(@NonNull Style style) {
                                    setupMainLayer(style);
                                    setupLight(style);
                                }
                            });
                            break;

                    }
                }
            }
        });

        model.getHallsVisible().observe(getActivity(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {

                if (mMap != null) {
                    if (aBoolean) {
                        if (hallsLayer != null) {
                            hallsLayer.setProperties(visibility(VISIBLE));
                        } else {
                            mMap.getStyle(new Style.OnStyleLoaded() {
                                @Override
                                public void onStyleLoaded(@NonNull Style style) {
                                    setUpHallLayer(style);
                                    hallsLayer.setProperties(visibility(VISIBLE));
                                }
                            });
                        }
                    } else {
                        if (hallsLayer != null) {
                            hallsLayer.setProperties(visibility(NONE));
                        } else {
                            mMap.getStyle(new Style.OnStyleLoaded() {
                                @Override
                                public void onStyleLoaded(@NonNull Style style) {
                                    setUpHallLayer(style);
                                    hallsLayer.setProperties(visibility(NONE));
                                }
                            });
                        }
                    }
                }

            }
        });

        model.getStallsVisible().observe(getActivity(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {

                if (mMap != null) {
                    if (aBoolean) {
                        if (stallsLayer != null) {
                            stallsLayer.setProperties(visibility(VISIBLE));
                        } else {
                            mMap.getStyle(new Style.OnStyleLoaded() {
                                @Override
                                public void onStyleLoaded(@NonNull Style style) {
                                    setUpStallLayer(style);
                                    stallsLayer.setProperties(visibility(VISIBLE));
                                }
                            });
                        }

                    } else {
                        if (stallsLayer != null) {
                            stallsLayer.setProperties(visibility(NONE));
                        } else {
                            mMap.getStyle(new Style.OnStyleLoaded() {
                                @Override
                                public void onStyleLoaded(@NonNull Style style) {
                                    setUpStallLayer(style);
                                    stallsLayer.setProperties(visibility(NONE));
                                }
                            });
                        }
                    }
                }

            }
        });

        model.getThreeDimenViewEnable().observe(getActivity(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (mMap != null) {
                    if (aBoolean) {
                        registerSensorListener();
                    } else {
                        unregisterSensorListener();
                    }
                }
            }
        });

        binding.symbolLayerToggleFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MapFilterBottomSheetDialogFragment bottomSheetDialog = new MapFilterBottomSheetDialogFragment();
                bottomSheetDialog.show(getActivity().getSupportFragmentManager(), bottomSheetDialog.getTag());
            }
        });

        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(@NonNull MapboxMap mapboxMap) {
                    mMap = mapboxMap;
                    model.setMapTypeSelected(mapStyle);
                    model.setHallsVisible(hallsVisible);
                    model.setStallsVisible(stallsVisible);
                    model.setThreeDimenViewEnable(threeDimenViewEnable);
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().getSupportFragmentManager().beginTransaction().
                remove(getActivity().getSupportFragmentManager().findFragmentByTag("com.mapbox.map")).commit();
    }

    private void setupMainLayer(Style style) {

        // Map is set up and the style has loaded. Now you can add data or make other map adjustments
        try {
            style.addSource(new GeoJsonSource(MapInConstants.EXPO_CENTER_MAIN_LAYER_SOURCE, new URI("asset://ground_indoor.geojson")));

            FillExtrusionLayer fillExtrusionLayer = new FillExtrusionLayer(
                    MapInConstants.EXPO_CENTER_MAIN_LAYER, MapInConstants.EXPO_CENTER_MAIN_LAYER_SOURCE).withProperties(
                    fillExtrusionColor(rgba(get("red"), get("green"), get("blue"), get("alpha"))),
                    fillExtrusionHeight(get("height")),
                    fillExtrusionOpacity(0.5f)
            );
            style.addLayer(fillExtrusionLayer);

        } catch (URISyntaxException exception) {
            exception.printStackTrace();
        }
    }

    private void setUpHallLayer(Style style) {
        try {

            if (hallsLayer == null) {
                style.addSource(new GeoJsonSource(MapInConstants.EXPO_CENTER_HALLS_POIS_LAYER_SOURCE, new URI("asset://ground_indoor_halls.geojson")));
                hallsLayer = new SymbolLayer(MapInConstants.EXPO_CENTER_HALLS_POIS_LAYER, MapInConstants.EXPO_CENTER_HALLS_POIS_LAYER_SOURCE);
                int color;
                if (model.getMapTypeSelected().getValue() == MapInConstants.MAPBOX_DARK)
                    color = Color.WHITE;
                else
                    color = Color.BLACK;

                hallsLayer.setProperties(textColor(color),
                        textField(get("Name")),
                        textSize(14.0f),
                        textVariableAnchor(
                                new String[]{TEXT_ANCHOR_TOP, TEXT_ANCHOR_BOTTOM, TEXT_ANCHOR_LEFT, TEXT_ANCHOR_RIGHT}),
                        textJustify(TEXT_JUSTIFY_AUTO),
                        textRadialOffset(0.5f));
//
//            symbolLayer.setFilter(eq(literal("$type"), literal("Point")));
                style.addLayerAbove(hallsLayer, MapInConstants.EXPO_CENTER_MAIN_LAYER);

            }

        } catch (URISyntaxException exception) {
            exception.printStackTrace();
        }
    }


    private void setUpStallLayer(Style style) {
        try {

            if (stallsLayer == null) {
                style.addSource(new GeoJsonSource(MapInConstants.EXPO_CENTER_STALLS_POIS_LAYER_SOURCE, new URI("asset://ground_indoor_stalls.geojson")));
                stallsLayer = new SymbolLayer(MapInConstants.EXPO_CENTER_STALLS_POIS_LAYER, MapInConstants.EXPO_CENTER_STALLS_POIS_LAYER_SOURCE);
                int color;
                if (model.getMapTypeSelected().getValue() == MapInConstants.MAPBOX_DARK)
                    color = Color.WHITE;
                else
                    color = Color.BLACK;
                stallsLayer.setProperties(textColor(color),
                        textField(get("Name")),
                        textSize(10.0f),
                        textVariableAnchor(
                                new String[]{TEXT_ANCHOR_TOP, TEXT_ANCHOR_BOTTOM, TEXT_ANCHOR_LEFT, TEXT_ANCHOR_RIGHT}),
                        textJustify(TEXT_JUSTIFY_AUTO),
                        textRadialOffset(0.5f));
//
//            symbolLayer.setFilter(eq(literal("$type"), literal("Point")));
                style.addLayerAbove(stallsLayer, MapInConstants.EXPO_CENTER_MAIN_LAYER);

            }

        } catch (URISyntaxException exception) {
            exception.printStackTrace();
        }
    }

    private void setupLight(@NonNull Style loadedMapStyle) {
        light = loadedMapStyle.getLight();

        binding.lightFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isInitPosition = !isInitPosition;
                if (isInitPosition) {
                    light.setPosition(new Position(1.5f, 90, 80));
                } else {
                    light.setPosition(new Position(1.15f, 210, 30));
                }
            }
        });
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            gravityArray = sensorEvent.values;
        }
        if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            magneticArray = sensorEvent.values;
        }
        if (gravityArray != null && magneticArray != null) {
            boolean success = SensorManager.getRotationMatrix(rotationMatrix, inclinationMatrix, gravityArray, magneticArray);
            if (success) {
                if (mMap != null) {
                    int mapCameraAnimationMillisecondsSpeed = 100;
                    mMap.animateCamera(CameraUpdateFactory
                            .newCameraPosition(createNewCameraPosition()), mapCameraAnimationMillisecondsSpeed
                    );
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private CameraPosition createNewCameraPosition() {
        float[] orientation = new float[3];
        SensorManager.getOrientation(rotationMatrix, orientation);
        float pitch = orientation[1];
        float roll = orientation[2];

        return new CameraPosition.Builder()
                .tilt(pitch * PITCH_AMPLIFIER)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

    }


//    @Override
//    public void onStop() {
//        super.onStop();
//        unregisterSensorListener();
//    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterSensorListener();
    }

    private void unregisterSensorListener() {
        sensorManager.unregisterListener(this, sensorControl.getGyro());
        sensorManager.unregisterListener(this, sensorControl.getMagnetic());

    }

    private void registerSensorListener() {
        int sensorEventDeliveryRate = 200;
        if (sensorControl.getGyro() != null) {
            sensorManager.registerListener(this, sensorControl.getGyro(), sensorEventDeliveryRate);
        } else {
            Log.d(MapInFragment.class.getSimpleName(), "Whoops, no accelerometer sensor");
            Toast.makeText(getContext(), R.string.no_accelerometer, Toast.LENGTH_SHORT).show();
        }
        if (sensorControl.getMagnetic() != null) {
            sensorManager.registerListener(this, sensorControl.getMagnetic(), sensorEventDeliveryRate);
        } else {
            Log.d(MapInFragment.class.getSimpleName(), "Whoops, no magnetic sensor");
            Toast.makeText(getContext(), R.string.no_magnetic, Toast.LENGTH_SHORT).show();
        }
    }

    private class SensorControl {
        private Sensor gyro;
        private Sensor magnetic;

        SensorControl(SensorManager sensorManager) {
            this.gyro = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            this.magnetic = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        }

        Sensor getGyro() {
            return gyro;
        }

        Sensor getMagnetic() {
            return magnetic;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().findViewById(R.id.appBar).setVisibility(View.GONE);
    }

    @Override
    public void onPause() {
        super.onPause();

        getActivity().findViewById(R.id.appBar).setVisibility(View.VISIBLE);
        searchItem.collapseActionView();

    }

    public void animateSearchToolbar(int numberOfMenuIcon, boolean containsOverflow, boolean show) {

        Toolbar mToolbar = binding.toolbar;
        mToolbar.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.white));
//        mDrawerLayout.setStatusBarBackgroundColor(ContextCompat.getColor(getContext(), R.color.quantum_grey_600));

        if (show) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                int width = mToolbar.getWidth() -
                        (containsOverflow ? getResources().getDimensionPixelSize(R.dimen.abc_action_button_min_width_overflow_material) : 0) -
                        ((getResources().getDimensionPixelSize(R.dimen.abc_action_button_min_width_material) * numberOfMenuIcon) / 2);
                Animator createCircularReveal = ViewAnimationUtils.createCircularReveal(mToolbar,
                        isRtl(getResources()) ? mToolbar.getWidth() - width : width, mToolbar.getHeight() / 2, 0.0f, (float) width);
                createCircularReveal.setDuration(250);
                createCircularReveal.start();
            } else {
                TranslateAnimation translateAnimation = new TranslateAnimation(0.0f, 0.0f, (float) (-mToolbar.getHeight()), 0.0f);
                translateAnimation.setDuration(220);
                mToolbar.clearAnimation();
                mToolbar.startAnimation(translateAnimation);
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                int width = mToolbar.getWidth() -
                        (containsOverflow ? getResources().getDimensionPixelSize(R.dimen.abc_action_button_min_width_overflow_material) : 0) -
                        ((getResources().getDimensionPixelSize(R.dimen.abc_action_button_min_width_material) * numberOfMenuIcon) / 2);
                Animator createCircularReveal = ViewAnimationUtils.createCircularReveal(mToolbar,
                        isRtl(getResources()) ? mToolbar.getWidth() - width : width, mToolbar.getHeight() / 2, (float) width, 0.0f);
                createCircularReveal.setDuration(250);
                createCircularReveal.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mToolbar.setBackgroundColor(getThemeColor(getContext(), R.attr.colorPrimary));
//                        mDrawerLayout.setStatusBarBackgroundColor(getThemeColor(MainActivity.this, R.attr.colorPrimaryDark));
                    }
                });
                createCircularReveal.start();
            } else {
                AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
                Animation translateAnimation = new TranslateAnimation(0.0f, 0.0f, 0.0f, (float) (-mToolbar.getHeight()));
                AnimationSet animationSet = new AnimationSet(true);
                animationSet.addAnimation(alphaAnimation);
                animationSet.addAnimation(translateAnimation);
                animationSet.setDuration(220);
                animationSet.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mToolbar.setBackgroundColor(getThemeColor(getContext(), R.attr.colorPrimary));
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                mToolbar.startAnimation(animationSet);
            }
//            mDrawerLayout.setStatusBarBackgroundColor(getThemeColor(getContext(), R.attr.colorPrimaryDark));
        }
    }

    private boolean isRtl(Resources resources) {
        return resources.getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
    }

    private static int getThemeColor(Context context, int id) {
        Resources.Theme theme = context.getTheme();
        TypedArray a = theme.obtainStyledAttributes(new int[]{id});
        int result = a.getColor(0, 0);
        a.recycle();
        return result;
    }


    private static List<POI> filter(List<POI> models, String query) {
        final String lowerCaseQuery = query.toLowerCase();

        final List<POI> filteredModelList = new ArrayList<>();
        for (POI model : models) {
            final String name = model.getName().toLowerCase();
            final String rank = String.valueOf(model.getRank());
            final String number = model.getNumber();
            if (name.contains(lowerCaseQuery) || number.contains(lowerCaseQuery) || rank.contains(lowerCaseQuery)) {
                filteredModelList.add(model);
            }
        }
        return filteredModelList;
    }

//    private void showBlueDot(LatLng center, double accuracyRadius, double bearing) {
//        if (mCircle == null) {
//            // location can received before map is initialized, ignoring those updates
//            if (mMap != null) {
//                mCircle = mMap.addCircle(new CircleOptions()
//                        .center(center)
//                        .radius(accuracyRadius)
//                        .fillColor(0x201681FB)
//                        .strokeColor(0x500A78DD)
//                        .zIndex(1.0f)
//                        .visible(true)
//                        .strokeWidth(5.0f));
//                mMarker = mMap.addMarker(new MarkerOptions()
//                        .position(center)
//                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_blue_dot))
//                        .anchor(0.5f, 0.5f)
//                        .rotation((float) bearing)
//                        .flat(true));
//            }
//        } else {
//            // move existing buildingMarkers position to received location
//            mCircle.setCenter(center);
//            mCircle.setRadius(accuracyRadius);
//            mMarker.setPosition(center);
//            mMarker.setRotation((float) bearing);
//        }
//    }

    private PolygonOptions generatePerimeter(LatLng centerCoordinates, double radiusInKilometers, int numberOfSides) {
        List<LatLng> positions = new ArrayList<>();
        double distanceX = radiusInKilometers / (111.319 * Math.cos(centerCoordinates.getLatitude() * Math.PI / 180));
        double distanceY = radiusInKilometers / 110.574;

        double slice = (2 * Math.PI) / numberOfSides;

        double theta;
        double x;
        double y;
        LatLng position;
        for (int i = 0; i < numberOfSides; ++i) {
            theta = i * slice;
            x = distanceX * Math.cos(theta);
            y = distanceY * Math.sin(theta);

            position = new LatLng(centerCoordinates.getLatitude() + y,
                    centerCoordinates.getLongitude() + x);
            positions.add(position);
        }
        return new PolygonOptions()
                .addAll(positions)
                .fillColor(Color.BLUE)
                .alpha(0.4f);
    }

    private float metersToKilometer(float accuracyInMeters) {
        return (float) (accuracyInMeters * 0.001);
    }

    private void showBlueDot(LatLng center, double accuracyRadius, double bearing) {

        IconFactory iconFactory = IconFactory.getInstance(getActivity());
        Icon icon = iconFactory.fromResource(R.drawable.circle_cropped);


        if (mCirclePolygon == null) {
            // location can received before map is initialized, ignoring those updates
            if (mMap != null) {
                mCirclePolygon = mMap.addPolygon(generatePerimeter(
                        new LatLng(center.getLatitude(), center.getLongitude()),
                        metersToKilometer((float) accuracyRadius),
                        64));
            }
        } else {
            // move existing buildingMarkers position to received location
            mCirclePolygon.remove();
            mCirclePolygon = mMap.addPolygon(generatePerimeter(
                    new LatLng(center.getLatitude(), center.getLongitude()),
                    metersToKilometer((float) accuracyRadius),
                    64));
            mLocationMarker.setPosition(center);
            mLocationMarker.setIcon(icon);
        }
        if (mLocationMarker == null) {
            if (mMap != null) {
                mLocationMarker = mMap.addMarker(new MarkerOptions()
                        .position(center)
                        .setIcon(icon)
                );
            } else {
                mLocationMarker.setPosition(center);
//                mLocationMarker.setIcon(icon);
            }
        }
    }


}
