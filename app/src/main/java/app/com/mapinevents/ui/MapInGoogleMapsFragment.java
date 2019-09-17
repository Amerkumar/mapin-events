package app.com.mapinevents.ui;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.github.wrdlbrnft.sortedlistadapter.SortedListAdapter;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.maps.android.data.geojson.GeoJsonLayer;
import com.indooratlas.android.sdk.IALocation;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import app.com.mapinevents.R;
import app.com.mapinevents.databinding.MapInGoogleMapsFragmentBinding;
import app.com.mapinevents.model.POI;
import app.com.mapinevents.ui.adapters.POIAdapter;
import app.com.mapinevents.utils.Utils;
import app.com.mapinevents.viewmodels.MapInViewModel;
import app.com.mapinevents.viewmodels.SharedViewModel;

public class MapInGoogleMapsFragment extends Fragment {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    public final static int REQUEST_LOCATION = 199;
    private static final Comparator<POI> COMPARATOR_POI = new SortedListAdapter.ComparatorBuilder<POI>()
            .setOrderForModel(POI.class, (a, b) -> Integer.signum(a.getRank() - b.getRank()))
            .build();
    private MapInViewModel mViewModel;
    private MapInGoogleMapsFragmentBinding binding;
    private View rootView;
    private LocationManager locationManager;
    private SharedViewModel model;
    private List<POI> mPoiModels;
    private MenuItem searchItem;
    private SearchView searchView;
    private GoogleMap mMap;
    private List<Marker> textMarkers = new LinkedList<>();
    private GeoJsonLayer layer;
    private List<POI> pois = new ArrayList<>();
    private Circle mCircle;
    private Marker mMarker;
    private IALocation currentLocation;


    public static MapInGoogleMapsFragment newInstance() {
        return new MapInGoogleMapsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        initData();

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
            binding = MapInGoogleMapsFragmentBinding.inflate(inflater, container, false);
            rootView = binding.getRoot();
            binding.myLocationFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mMap != null && currentLocation != null) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 18.5f));
                    }
                }
            });
        }
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(MapInViewModel.class);
        model = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);
        model.getIaLocationMutableLiveData().observe(this, new Observer<IALocation>() {
            @Override
            public void onChanged(IALocation iaLocation) {
                if (mMap == null)
                    return;

                currentLocation = iaLocation;

//                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(iaLocation.getLatitude(), iaLocation.getLongitude()), 18.0f));
                showBlueDot(new LatLng(iaLocation.getLatitude(), iaLocation.getLongitude()),
                        iaLocation.getAccuracy(),
                        iaLocation.getBearing());
            }
        });
        POIAdapter poiAdapter = new POIAdapter(getContext(), POI.class, COMPARATOR_POI, poi -> {
            Utils.hideKeyboard(getActivity());
//            MapInFragmentDirections.ActionMapInFragmentToMapInSelectionFragment actionMapInFragmentToMapInSelectionFragment =
//                    MapInFragmentDirections.actionMapInFragmentToMapInSelectionFragment(poi);
//            Navigation.findNavController(binding.getRoot()).navigate(actionMapInFragmentToMapInSelectionFragment);
            MapInGoogleMapsFragmentDirections.ActionMapInGoogleMapsFragmentToMapInSelectionFragment actionMapInGoogleMapsFragmentToMapInSelectionFragment =
                    MapInGoogleMapsFragmentDirections.actionMapInGoogleMapsFragmentToMapInSelectionFragment(poi);
            Navigation.findNavController(binding.getRoot()).navigate(actionMapInGoogleMapsFragmentToMapInSelectionFragment);
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
                        getActivity().findViewById(R.id.map_view).setVisibility(View.VISIBLE);
                        Utils.showView(getActivity().findViewById(R.id.bottom_nav));
                        binding.myLocationFab.setVisibility(View.VISIBLE);
                        binding.poiRecyclerView.setVisibility(View.GONE);
                    }
                    return true;
                }

                @Override
                public boolean onMenuItemActionExpand(MenuItem item) {
                    // Called when SearchView is expanding
//                animateSearchToolbar(1, true, true);
//                    animateSearchToolbar(1, true, true);
                    getActivity().findViewById(R.id.map_view).setVisibility(View.GONE);
                    Utils.hideView(getActivity().findViewById(R.id.bottom_nav));
                    binding.myLocationFab.setVisibility(View.GONE);
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


    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(GoogleMap map) {
        Log.d(MapInGoogleMapsFragment.class.getSimpleName(), "Sticky event called");
        mMap = map;
//        setGeoJSONLayer();
//        setStallsLayer();
//        mMap.animateCamera(CameraUpdateFactory
//                .newLatLngZoom(new LatLng(24.90180884018499,67.07578086274215), 18.0f));

    }

    private void initData() {
        HashMap<String, Double> _geoloc = new HashMap<>();
        _geoloc.put("lat", 24.901613540896573);
        _geoloc.put("lng", 67.07579791545868);
        pois.add(new POI("1", 1, "Congress Center", "", _geoloc));

        _geoloc.put("lat", 24.901723019090273);
        _geoloc.put("lng", 67.07647383213043);
        pois.add(new POI("2", 2, "Hall 1","" ,_geoloc));

        _geoloc.put("lat", 24.901241314311598);
        _geoloc.put("lng", 67.07611173391342);
        pois.add(new POI("3", 3, "Hall 2","" ,_geoloc));

        _geoloc.put("lat", 24.90097369973331);
        _geoloc.put("lng", 67.07539290189743);
        pois.add(new POI("4", 4, "Hall 3","" ,_geoloc));

        _geoloc.put("lat", 24.900637964259985);
        _geoloc.put("lng", 67.07618951797485);
        pois.add(new POI("5", 5, "Hall 4","" ,_geoloc));

        _geoloc.put("lat", 24.900294929246193);
        _geoloc.put("lng", 67.07643628120422);
        pois.add(new POI("6", 6, "Hall 5","" ,_geoloc));

        _geoloc.put("lat", 24.900328989502956);
        _geoloc.put("lng", 67.0772436261177);
        pois.add(new POI("7", 7, "Hall 6","" ,_geoloc));

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





    @Override
    public void onPause() {
        super.onPause();
//        if (layer != null)
//            layer.addLayerToMap();

        if (searchItem != null)
        searchItem.collapseActionView();

        if (mCircle != null && mCircle.isVisible()) {
            mCircle.setVisible(false);
            mMarker.setVisible(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
//        if (layer != null)
//            layer.removeLayerFromMap();
        if (mCircle != null && !mCircle.isVisible()) {
            mCircle.setVisible(true);
            mMarker.setVisible(true);
        }
    }

    private void showBlueDot(LatLng center, double accuracyRadius, double bearing) {
        if (mCircle == null) {
            // location can received before map is initialized, ignoring those updates
            if (mMap != null) {
                mCircle = mMap.addCircle(new CircleOptions()
                        .center(center)
                        .radius(accuracyRadius)
                        .fillColor(0x201681FB)
                        .strokeColor(0x500A78DD)
                        .zIndex(1.0f)
                        .visible(true)
                        .strokeWidth(5.0f));
                mMarker = mMap.addMarker(new MarkerOptions()
                        .position(center)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_blue_dot))
                        .anchor(0.5f, 0.5f)
                        .rotation((float) bearing)
                        .flat(true));
            }
        } else {
            // move existing buildingMarkers position to received location
            mCircle.setCenter(center);
            mCircle.setRadius(accuracyRadius);
            mMarker.setPosition(center);
            mMarker.setRotation((float) bearing);
        }
    }
}
