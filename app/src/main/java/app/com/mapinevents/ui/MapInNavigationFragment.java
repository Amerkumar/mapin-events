package app.com.mapinevents.ui;

import android.content.DialogInterface;
import android.graphics.Color;
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

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.indooratlas.android.sdk.IALocation;
import com.indooratlas.android.sdk.IALocationManager;
import com.indooratlas.android.sdk.IARoute;
import com.indooratlas.android.sdk.IAWayfindingListener;
import com.indooratlas.android.sdk.IAWayfindingRequest;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.Polygon;
import com.mapbox.mapboxsdk.annotations.PolygonOptions;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
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
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import app.com.mapinevents.R;
import app.com.mapinevents.databinding.MapInNavigationFragmentBinding;
import app.com.mapinevents.databinding.MapInSelectionFragmentBinding;
import app.com.mapinevents.model.POI;
import app.com.mapinevents.utils.MapInConstants;
import app.com.mapinevents.utils.Utils;
import app.com.mapinevents.viewmodels.SharedViewModel;

import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.rgba;
import static com.mapbox.mapboxsdk.style.layers.Property.TEXT_ANCHOR_BOTTOM;
import static com.mapbox.mapboxsdk.style.layers.Property.TEXT_ANCHOR_LEFT;
import static com.mapbox.mapboxsdk.style.layers.Property.TEXT_ANCHOR_RIGHT;
import static com.mapbox.mapboxsdk.style.layers.Property.TEXT_ANCHOR_TOP;
import static com.mapbox.mapboxsdk.style.layers.Property.TEXT_JUSTIFY_AUTO;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillExtrusionColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillExtrusionHeight;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillExtrusionOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textField;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textJustify;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textRadialOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textSize;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textVariableAnchor;

public class MapInNavigationFragment extends Fragment {

    private SupportMapFragment mapFragment;
    private MapboxMap mMap;
    private SharedViewModel model;
    private SymbolLayer hallsLayer;
    private SymbolLayer stallsLayer;
    private POI poi;
    private MapInNavigationFragmentBinding binding;
    private MapInNavigationViewModel mViewModel;
    private Polygon mCirclePolygon;
    private Marker mLocationMarker;
    private IAWayfindingRequest mWayFindingDestination;
    private IARoute mCurrentRoute;


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
    private Marker mDestinationMarker;
    private int mFloor;
    private List<Polyline> mPolylines = new ArrayList<>();
    private Marker mHeadingMarker;
    private LatLng center;


    public static MapInNavigationFragment newInstance() {
        return new MapInNavigationFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIALocationManager = IALocationManager.create(getContext());
        mIALocationManager.lockIndoors(true);

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
                                Navigation.findNavController(v).popBackStack(R.id.mapInFragment, false);
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
                Navigation.findNavController(binding.getRoot()).popBackStack(R.id.mapInFragment, false);
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

        mWayFindingDestination = new IAWayfindingRequest.Builder()
                .withFloor(0)
                .withLatitude(poi.get_geoloc().get("lat"))
                .withLongitude(poi.get_geoloc().get("lng"))
                .build();

        mIALocationManager.requestWayfindingUpdates(mWayFindingDestination, mWayFindingListener);




        binding.indoorNavigationBottomSheet.metersTextView.setText("N/A");
        binding.indoorNavigationBottomSheet.timeTextView.setText("N/A");
        binding.indoorNavigationBottomSheet.navigationDetailTextView.setText("Navigating to " + poi.getName());



        if (savedInstanceState == null) {
            final FragmentTransaction transaction = getActivity().getSupportFragmentManager()
                    .beginTransaction();

            MapboxMapOptions options = MapboxMapOptions.createFromAttributes(getContext(), null);
            options.camera(new CameraPosition.Builder()
                    .target(new LatLng(24.901317946386918, 67.07612826571307))
                    .bearing(145)
                    .zoom(17)
                    .build());

            mapFragment = SupportMapFragment.newInstance(options);

            transaction.add(R.id.mapin_fragment_container, mapFragment, "com.mapbox.map");
            transaction.commit();
        } else {
            mapFragment = (SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentByTag("com.mapbox.map");
        }
        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(@NonNull MapboxMap mapboxMap) {
                    mMap = mapboxMap;

                    if (mDestinationMarker == null) {
                        mDestinationMarker = mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(poi.get_geoloc().get("lat"), poi.get_geoloc().get("lng"))));
                    } else {
                        mDestinationMarker.setPosition(new LatLng(poi.get_geoloc().get("lat"), poi.get_geoloc().get("lng")));
                    }
                    int integer = model.getMapTypeSelected().getValue();
                    switch (integer) {
                        case MapInConstants.MAPBOX_STREET:
                            mMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
                                @Override
                                public void onStyleLoaded(@NonNull Style style) {
                                    setupMainLayer(style);
                                }
                            });
                            break;
                        case MapInConstants.MAPBOX_LIGHT:
                            mMap.setStyle(Style.LIGHT, new Style.OnStyleLoaded() {
                                @Override
                                public void onStyleLoaded(@NonNull Style style) {
                                    setupMainLayer(style);
                                }
                            });
                            break;
                        case MapInConstants.MAPBOX_DARK:
                            mMap.setStyle(Style.DARK, new Style.OnStyleLoaded() {
                                @Override
                                public void onStyleLoaded(@NonNull Style style) {
                                    setupMainLayer(style);
                                }
                            });
                            break;

                    }
                    mapboxMap.getStyle(new Style.OnStyleLoaded() {
                        @Override
                        public void onStyleLoaded(@NonNull Style style) {
                            boolean hall = model.getHallsVisible().getValue();
                            setUpHallLayer(style, hall);
                            boolean stalls = model.getStallsVisible().getValue();
                            setUpStallLayer(style, stalls);
                        }
                    });

                    mapboxMap.addMarker(new MarkerOptions()
                            .position(new LatLng(poi.get_geoloc().get("lat"), poi.get_geoloc().get("lng"))));
                }
            });
        }
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

    private void setUpHallLayer(Style style, boolean halls) {
        try {
            if (halls) {
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
                style.addLayerAbove(hallsLayer, MapInConstants.EXPO_CENTER_MAIN_LAYER);

            }
        } catch (URISyntaxException exception) {
            exception.printStackTrace();
        }
    }


    private void setUpStallLayer(Style style, boolean stalls) {
        try {

            if (stalls) {
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


    private void showLocationCircle(LatLng center, double accuracyRadius) {


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
        }


        IconFactory iconFactory = IconFactory.getInstance(getActivity());
        Icon icon = iconFactory.fromResource(R.drawable.circle_cropped);

        if (mHeadingMarker == null) {
            // location can received before map is initialized, ignoring those updates
            if (mMap != null) {
                mHeadingMarker = mMap.addMarker(new MarkerOptions()
                        .position(center)
                        .icon(icon));
            }
        } else {
            // move existing markers position to received location
            mHeadingMarker.setPosition(center);
            mHeadingMarker.setIcon(icon);
        }
    }


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

        binding.indoorNavigationBottomSheet.metersTextView.setText(String.format("%s | %d", metersToFootsteps(totalDistance), totalDistance));
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

