package app.com.mapinevents.ui;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
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

import app.com.mapinevents.R;
import app.com.mapinevents.databinding.MapInSelectionFragmentBinding;
import app.com.mapinevents.model.POI;
import app.com.mapinevents.utils.MapInConstants;
import app.com.mapinevents.viewmodels.SharedViewModel;

import static com.mapbox.mapboxsdk.style.expressions.Expression.bool;
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

public class MapInSelectionFragment extends Fragment {

    private MapInSelectionViewModel mViewModel;
    private SupportMapFragment mapFragment;
    private MapboxMap mMap;
    private SharedViewModel model;
    private SymbolLayer hallsLayer;
    private SymbolLayer stallsLayer;
    private POI poi;
    private MapInSelectionFragmentBinding binding;

    public static MapInSelectionFragment newInstance() {
        return new MapInSelectionFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = MapInSelectionFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(MapInSelectionViewModel.class);
        model = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);

        poi = MapInSelectionFragmentArgs.fromBundle(getArguments()).getPoi();

        if (savedInstanceState == null) {
            final FragmentTransaction transaction = getActivity().getSupportFragmentManager()
                    .beginTransaction();

            MapboxMapOptions options = MapboxMapOptions.createFromAttributes(getContext(), null);
            options.camera(new CameraPosition.Builder()
                    .target(new LatLng(24.901317946386918, 67.07612826571307))
                    .bearing(145)
                    .tilt(45)
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
                            boolean threeDimenViewEnable = model.getThreeDimenViewEnable().getValue();
//                            if (mMap != null) {
//                                if (threeDimenViewEnable) {
//                                    registerSensorListener();
//                                } else {
//                                    unregisterSensorListener();
//                                }
//                            }
                        }
                    });

                    mapboxMap.addMarker(new MarkerOptions()
                            .position(new LatLng(poi.get_geoloc().get("lat"), poi.get_geoloc().get("lng"))));

                    Toolbar toolbar = getActivity().findViewById(R.id.toolbar);

                    toolbar.setTitle(poi.getName());

                    binding.poiName.setText(poi.getName());
                    binding.poiNumber.setText(poi.getNumber());
                    binding.poiNavigate.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            MapInSelectionFragmentDirections.ActionMapInSelectionFragmentToMapInNavigationFragment actionMapInSelectionFragmentToMapInNavigationFragment =
                                    MapInSelectionFragmentDirections.actionMapInSelectionFragmentToMapInNavigationFragment(poi);
                            Navigation.findNavController(view).navigate(actionMapInSelectionFragmentToMapInNavigationFragment);
                        }
                    });
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

    @Override
    public void onResume() {
        super.onResume();
        getActivity().findViewById(R.id.bottom_nav).setVisibility(View.GONE);

    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().findViewById(R.id.bottom_nav).setVisibility(View.VISIBLE);

    }
}
