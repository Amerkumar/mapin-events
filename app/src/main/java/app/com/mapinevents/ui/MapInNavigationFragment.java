package app.com.mapinevents.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.maps.android.data.geojson.GeoJsonLayer;
import com.indooratlas.android.sdk.IALocation;
import com.indooratlas.android.sdk.IALocationManager;
import com.indooratlas.android.sdk.IAOrientationListener;
import com.indooratlas.android.sdk.IAOrientationRequest;
import com.indooratlas.android.sdk.IARegion;
import com.indooratlas.android.sdk.IARoute;
import com.indooratlas.android.sdk.IAWayfindingListener;
import com.indooratlas.android.sdk.IAWayfindingRequest;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Vector;

import app.com.mapinevents.R;
import app.com.mapinevents.databinding.MapInNavigationFragmentBinding;
import app.com.mapinevents.databinding.MapInSelectionFragmentBinding;
import app.com.mapinevents.model.POI;
import app.com.mapinevents.utils.MapInConstants;
import app.com.mapinevents.utils.Utils;
import app.com.mapinevents.viewmodels.SharedViewModel;

public class MapInNavigationFragment extends Fragment {

    private SharedViewModel model;
    private POI poi;
    private MapInNavigationFragmentBinding binding;
    private MapInNavigationViewModel mViewModel;
    private IAWayfindingRequest mWayFindingDestination;
    private IARoute mCurrentRoute;

    private IAOrientationListener mOrientationListener = new IAOrientationListener() {
        @Override
        public void onHeadingChanged(long l, double v) {
            updateHeading(v);
        }

        @Override
        public void onOrientationChange(long l, double[] doubles) {

        }
    };
    private Circle mCircle;

    private void updateHeading(double heading) {
        if (mHeadingMarker != null) {
            mHeadingMarker.setRotation((float)heading);
        }
    }


    private IAWayfindingListener mWayFindingListener = new IAWayfindingListener() {
        @Override
        public void onWayfindingUpdate(IARoute iaRoute) {
            mCurrentRoute = iaRoute;
            if (hasArrivedToDestination(iaRoute)) {
                mCurrentRoute = null;
                mWayFindingDestination = null;
                mIALocationManager.removeWayfindingUpdates();
                mDestinationMarker.remove();
                new MaterialAlertDialogBuilder(getContext())
                        .setTitle("You have reached your destination.")
                        .setMessage("Thankyou for using MapIn.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Navigation.findNavController(binding.getRoot()).popBackStack(R.id.mapInFragment, false);
                            }
                        })
                        .setCancelable(false)
                        .show();
            }
            updateRouteVisualization();
        }
    };
    private IALocationManager mIALocationManager;
    private int mFloor;
    private List<Polyline> mPolylines = new ArrayList<>();
    private Marker mHeadingMarker;
    private LatLng center;
    private Marker mDestinationMarker;
    private GoogleMap mMap;


    public static MapInNavigationFragment newInstance() {
        return new MapInNavigationFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIALocationManager = IALocationManager.create(getContext());

        WifiManager wifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);

    }



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = MapInNavigationFragmentBinding.inflate(inflater, container, false);
        binding.indoorNavigationBottomSheet.cancelNavigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                new MaterialAlertDialogBuilder(getContext(), R.style.AlertDialogTheme)
                        .setTitle("Cancel Navigation")
                        .setMessage("Are you sure you want to cancel navigation?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Navigation.findNavController(v).popBackStack(R.id.mapInGoogleMapsFragment, false);
                            }
                        })
                        .setNegativeButton("No", null)

                        .show();
            }
        });

        binding.indoorNavigationBottomSheet.expandBottomSheetImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // if bottom sheet is collapsed, it means it is not visible
                // expand bottom sheet
                if (BottomSheetBehavior.from(binding.indoorNavigationBottomSheet.bottomSheetContainer).getState()
                        == BottomSheetBehavior.STATE_COLLAPSED) {
                    BottomSheetBehavior.from(binding.indoorNavigationBottomSheet.bottomSheetContainer).setState(BottomSheetBehavior.STATE_EXPANDED);
                } else {
                    BottomSheetBehavior.from(binding.indoorNavigationBottomSheet.bottomSheetContainer).setState(BottomSheetBehavior.STATE_COLLAPSED);
                }

            }
        });


        BottomSheetBehavior.from(binding.indoorNavigationBottomSheet.bottomSheetContainer)
                .setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                    @Override
                    public void onStateChanged(@NonNull View bottomSheet, int newState) {
                        switch (newState) {
                            case BottomSheetBehavior.STATE_EXPANDED:
                                binding.indoorNavigationBottomSheet.expandBottomSheetImageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_expand_less));
                                binding.indoorNavigationBottomSheet.cancelNavigation.setVisibility(View.INVISIBLE);
                                break;
                            case BottomSheetBehavior.STATE_COLLAPSED:
                                binding.indoorNavigationBottomSheet.expandBottomSheetImageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_expand_more));
                                binding.indoorNavigationBottomSheet.cancelNavigation.setVisibility(View.VISIBLE);
                                break;
                        }
                    }

                    @Override
                    public void onSlide(@NonNull View bottomSheet, float slideOffset) {

                    }
                });

        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event

                new MaterialAlertDialogBuilder(getContext(), R.style.AlertDialogTheme)
                        .setTitle("Cancel Navigation")
                        .setMessage("Are you sure you want to cancel navigation?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Navigation.findNavController(binding.getRoot()).popBackStack(R.id.mapInGoogleMapsFragment, false);
                            }
                        })
                        .setNegativeButton("No", null)

                        .show();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(MapInNavigationViewModel.class);
        model = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);


        model.getIaLocationMutableLiveData().observe(this, new Observer<IALocation>() {
            @Override
            public void onChanged(IALocation iaLocation) {

                // location received before map is intialized
                if (mMap == null) {
                    return;
                }

                center = new LatLng(iaLocation.getLatitude(), iaLocation.getLongitude());

                final int newFloor = iaLocation.getFloorLevel();

                if (mFloor != newFloor) {
                    updateRouteVisualization();
                }

                mFloor = newFloor;

                mMap.animateCamera(CameraUpdateFactory.newLatLng(center));

                showLocationCircle(center, iaLocation.getAccuracy());

            }
        });
        poi = MapInNavigationFragmentArgs.fromBundle(getArguments()).getPoi();

        binding.indoorNavigationBottomSheet.metersTextView.setText("N/A");
        binding.indoorNavigationBottomSheet.timeTextView.setText("N/A");
        binding.indoorNavigationBottomSheet.navigationDetailTextView.setText("Navigating to " + poi.getName());
//        Snackbar.make(binding.getRoot(), "Hall 5 & 6 is not mapped, navigation will be inacurate there")
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
        mMap = map;

        mWayFindingDestination = new IAWayfindingRequest.Builder()
                .withFloor(0)
                .withLatitude(poi.get_geoloc().get("lat"))
                .withLongitude(poi.get_geoloc().get("lng"))
                .build();

        mIALocationManager.requestWayfindingUpdates(mWayFindingDestination, mWayFindingListener);

        if (mDestinationMarker == null) {
            mDestinationMarker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(poi.get_geoloc().get("lat"), poi.get_geoloc().get("lng")))
                    .icon(Utils.getBitmapFromVector(getContext(),
                            R.drawable.ic_location_pin_48px_square,
                            getResources().getColor(R.color.mapinBlue))));
        } else {
            mDestinationMarker.setPosition(new LatLng(poi.get_geoloc().get("lat"), poi.get_geoloc().get("lng")));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mIALocationManager.registerOrientationListener(
                // update if heading changes by 1 degrees or more
                new IAOrientationRequest(1, 0),
                mOrientationListener);

        if (mHeadingMarker != null && !mHeadingMarker.isVisible())
            mHeadingMarker.setVisible(true);

        if (mDestinationMarker != null && !mDestinationMarker.isVisible())
            mDestinationMarker.setVisible(true);

        if (mCircle != null && !mCircle.isVisible())
            mCircle.setVisible(true);

    }

    @Override
    public void onPause() {
        super.onPause();
        if (mDestinationMarker != null && mDestinationMarker.isVisible())
            mDestinationMarker.setVisible(false);

        if (mCurrentRoute != null)
            mCurrentRoute = null;

        if (mHeadingMarker != null && mHeadingMarker.isVisible())
            mHeadingMarker.setVisible(false);
        if (mCircle != null && mCircle.isVisible())
            mCircle.setVisible(false);
        mIALocationManager.unregisterOrientationListener(mOrientationListener);
        clearRouteVisualization();
        mIALocationManager.removeWayfindingUpdates();
    }

    private void showLocationCircle(LatLng center, double accuracyRadius) {
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
                mHeadingMarker = mMap.addMarker(new MarkerOptions()
                        .position(center)
                        .icon(Utils.getBitmapFromVector(getContext(),
                                R.drawable.ic_navigation_black_48dp,
                                getResources().getColor(R.color.mapinBlue)))
                        .anchor(0.5f, 0.5f)
                        .flat(true));
            }
        } else {
            // move existing markers position to received location
            mCircle.setCenter(center);
            mHeadingMarker.setPosition(center);
            mCircle.setRadius(accuracyRadius);
        }
    }



    private boolean hasArrivedToDestination(IARoute route) {
        // empty routes are only returned when there is a problem, for example,
        // missing or disconnected routing graph
        if (route.getLegs().size() == 0) {
            return false;
        }

        final double FINISH_THRESHOLD_METERS = 8.0;
        double routeLength = 0;
        for (IARoute.Leg leg : route.getLegs()) routeLength += leg.getLength();
        return routeLength < FINISH_THRESHOLD_METERS;
    }

    /**
     * Clear the visualizations for the wayfinding paths
     */
    private void clearRouteVisualization() {
        for (Polyline pl : mPolylines) {
            pl.remove();
        }
        mPolylines.clear();
    }

    /**
     * Visualize the IndoorAtlas Wayfinding route on top of the Google Maps.
     */
    private void updateRouteVisualization() {

        clearRouteVisualization();

        int totalDistance = 0;

        if (mCurrentRoute == null) {
            return;
        }

        for (IARoute.Leg leg : mCurrentRoute.getLegs()) {

//            if (leg.getEdgeIndex() == null) {
//                // Legs without an edge index are, in practice, the last and first legs of the
//                // route. They connect the destination or current location to the routing graph.
//                // All other legs travel along the edges of the routing graph.
//
//                // Omitting these "artificial edges" in visualization can improve the aesthetics
//                // of the route. Alternatively, they could be visualized with dashed lines.
//                continue;
//            }

            PolylineOptions opt = new PolylineOptions();

            opt.add(new LatLng(leg.getBegin().getLatitude(), leg.getBegin().getLongitude()));
            opt.add(new LatLng(leg.getEnd().getLatitude(), leg.getEnd().getLongitude()));

            // Here wayfinding path in different floor_num than current location is visualized in
            // a semi-transparent color
            if (leg.getBegin().getFloor() == mFloor && leg.getEnd().getFloor() == mFloor) {
                opt.color(0xFF0000FF);
            } else {
                opt.color(0x300000FF);
            }

            totalDistance = (int) (totalDistance + leg.getLength());

            mPolylines.add(mMap.addPolyline(opt));
        }
        if (metersToTime(totalDistance) > 60) {
            // seconds greater than 60
            // convert into minutes
            long time = Math.round(Math.floor(metersToTime(totalDistance) / 60));
            binding.indoorNavigationBottomSheet.timeTextView.setText(String.format("%d min", time));
        } else { // seconds less than 60
            binding.indoorNavigationBottomSheet.timeTextView.setText(String.format("%s sec", metersToTime(totalDistance)));
        }

        binding.indoorNavigationBottomSheet.metersTextView.setText(String.format("%s footsteps | %d meters", metersToFootsteps(totalDistance), totalDistance));
    }

    // using average stride length
    private double metersToFootsteps(double distance) {
        return Math.round(distance / 0.762);
    }

    // using average speed
    // return time in seconds
    private double  metersToTime(double distance) {
        return Math.round(distance / 1.4); }

}

