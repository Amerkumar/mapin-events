package app.com.mapinevents.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.mapbox.mapboxsdk.Mapbox;
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

import app.com.mapinevents.R;
import app.com.mapinevents.databinding.MapInFragmentBinding;
import app.com.mapinevents.utils.MapInConstants;
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


    // Amplifiers that translate small phone orientation movements into larger viewable map changes.
// Pitch is negative to compensate for the negative readings from the device while face up
// 90 is used based on the viewable angle when viewing the map (from phone being flat to facing you).
    private static final int PITCH_AMPLIFIER = -90;
    private static final int BEARING_AMPLIFIER = 90;
    private MapboxMap mMap;
    private SymbolLayer hallsLayer;
    private SymbolLayer stallsLayer;
    private SharedViewModel model;


    public static MapInFragment newInstance() {
        return new MapInFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(getContext(), getString(R.string.mapbox_access_token));

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
//        setHasOptionsMenu(true);
        binding = MapInFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mViewModel = ViewModelProviders.of(this).get(MapInViewModel.class);
        model = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);
        // TODO: Use the ViewModel

        SupportMapFragment mapFragment;


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

            transaction.add(R.id.map_fragment_container, mapFragment, "com.mapbox.map");
            transaction.commit();
        } else {
            mapFragment = (SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentByTag("com.mapbox.map");
        }
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


//        findViewById(R.id.fabLightColor).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                isRedColor = !isRedColor;
//                light.setColor(ColorUtils.colorToRgbaString(isRedColor ? Color.RED : Color.BLUE));
//            }
//        });
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
        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            sensorControl = new SensorControl(sensorManager);
            registerSensorListener();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
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
}
