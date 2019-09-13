package app.com.mapinevents.ui;

import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.maps.SupportMapFragment;
import com.mapbox.mapboxsdk.style.layers.FillExtrusionLayer;
import com.mapbox.mapboxsdk.style.light.Light;
import com.mapbox.mapboxsdk.style.light.Position;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.ColorUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import app.com.mapinevents.R;
import app.com.mapinevents.databinding.MapInFragmentBinding;
import app.com.mapinevents.viewmodels.MapInViewModel;

import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.rgba;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillExtrusionColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillExtrusionHeight;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillExtrusionOpacity;

public class MapInFragment extends Fragment
    implements SensorEventListener
{

    private MapInViewModel mViewModel;
    private Light light;
    private MapInFragmentBinding binding;
    private boolean isInitPosition;
    private SensorManager sensorManager;
    private float[] gravityArray;
    private float[] magneticArray;
    private float[] inclinationMatrix = new float[9];
    private float[] rotationMatrix = new float[9];

    // Amplifiers that translate small phone orientation movements into larger viewable map changes.
// Pitch is negative to compensate for the negative readings from the device while face up
// 90 is used based on the viewable angle when viewing the map (from phone being flat to facing you).
    private static final int PITCH_AMPLIFIER = -90;
    private static final int BEARING_AMPLIFIER = 90;


    public static MapInFragment newInstance() {
        return new MapInFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(getContext(), getString(R.string.mapbox_access_token));

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = MapInFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(MapInViewModel.class);
        // TODO: Use the ViewModel

        SupportMapFragment mapFragment;

        if (savedInstanceState == null) {
            final FragmentTransaction transaction = getActivity().getSupportFragmentManager()
                    .beginTransaction();

            MapboxMapOptions options = MapboxMapOptions.createFromAttributes(getContext(), null);
            options.camera(new CameraPosition.Builder()
                    .target(new LatLng(24.901317946386918, 67.07612826571307))
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
                    mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
                        @Override
                        public void onStyleLoaded(@NonNull Style style) {

                            // Map is set up and the style has loaded. Now you can add data or make other map adjustments
                            try {
                                style.addSource(new GeoJsonSource("expo-center", new URI("asset://ground_indoor.geojson")));

                                style.addLayer(new FillExtrusionLayer(
                                        "expo-center-layer", "expo-center").withProperties(
                                        fillExtrusionColor(rgba(get("red"), get("green"), get("blue"), get("alpha"))),
                                        fillExtrusionHeight(get("height")),
                                        fillExtrusionOpacity(0.5f)
                                ));

                                setupLight(style);
                            } catch (URISyntaxException exception) {
                                exception.printStackTrace();
                            }
                        }
                    });
                }
            });
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


    private String loadJsonFromAsset(String filename) {
// Using this method to load in GeoJSON files from the assets folder.

        try {
            InputStream is = getActivity().getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            return new String(buffer, "UTF-8");

        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
