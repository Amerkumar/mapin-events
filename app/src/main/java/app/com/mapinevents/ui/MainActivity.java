package app.com.mapinevents.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.common.collect.Lists;
import com.google.firebase.FirebaseApp;
import com.google.maps.android.data.geojson.GeoJsonFeature;
import com.google.maps.android.data.geojson.GeoJsonLayer;
import com.google.maps.android.data.geojson.GeoJsonPolygonStyle;
import com.indooratlas.android.sdk.IALocation;
import com.indooratlas.android.sdk.IALocationManager;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import app.com.mapinevents.R;
import app.com.mapinevents.SingletonAppClass;
import app.com.mapinevents.model.POI;
import app.com.mapinevents.utils.Utils;
import app.com.mapinevents.viewmodels.SharedViewModel;

import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity implements
        NavController.OnDestinationChangedListener, OnMapReadyCallback {

    public static final int RC_SIGN_IN = 11;
    public static boolean FIRST_APP_OPEN = true;
    private BottomNavigationView bottomNavigationView;
    private Toolbar toolbar;
    private NavController navController;
    private ProgressBar progressBar;
    private AppBarLayout appbar;
    private IALocationManager mIALocationManager;
    private SharedViewModel model;
    private MapView mMapView;
    private GoogleMap mMap;
    private GeoJsonLayer layer;
    private List<POI> pois = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        FirebaseApp.initializeApp(getApplicationContext());


        Set<Integer> topLevelDestinationsSet = new HashSet<>(Arrays.asList(
                R.id.loginFragment,
                R.id.homeFragment,
                R.id.agendaFragment,
                R.id.scheduleFragment,
                R.id.mapInGoogleMapsFragment,
                R.id.moreFragment
        ));


        appbar = findViewById(R.id.appBar);
        toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.progress_horizontal);
        bottomNavigationView = findViewById(R.id.bottom_nav);

        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        AppBarConfiguration appBarConfiguration =
                new AppBarConfiguration.Builder(topLevelDestinationsSet)
                        .build();

        mMapView = findViewById(R.id.map_view);
        mMapView.onCreate(savedInstanceState);

        try {
            MapsInitializer.initialize(getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(this::onMapReady);


        NavigationUI.setupWithNavController(bottomNavigationView, navController);
        NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration);
        navController.addOnDestinationChangedListener(this);

        SingletonAppClass.getInstance().setFIRST_APP_OPEN(true);

        model = ViewModelProviders.of(MainActivity.this).get(SharedViewModel.class);

        IndoorLocationListener.getInstance(MainActivity.this).observe(this, new Observer<IALocation>() {
            @Override
            public void onChanged(IALocation iaLocation) {
                Log.d("MainActivity", String.valueOf(iaLocation.getAccuracy()));
                model.setIALocationMutableLiveData(iaLocation);
            }
        });

        mIALocationManager = IALocationManager.create(getApplicationContext());
        mIALocationManager.lockIndoors(true);
    }


    @Override
    public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
        switch (destination.getId()) {
            case R.id.loginFragment:
                Utils.hideView(bottomNavigationView);
//                Utils.showView(appbar);
                hideMapView();
                break;
            case R.id.scheduleFragment:
                hideMapView();
                Utils.showView(bottomNavigationView);
                Utils.showView(appbar);
                break;
            case R.id.scheduleDetailFragment:
                Utils.hideView(bottomNavigationView);
                hideMapView();
                break;
            case R.id.settingsFragment:
            case R.id.aboutDeveloperTeamFragment:
            case R.id.infoFragment:
                Utils.hideView(bottomNavigationView);
                hideMapView();
                break;
            case R.id.mapInGoogleMapsFragment:
                showMapView();
                Utils.hideView(appbar);
                Utils.showView(bottomNavigationView);
                break;
            case R.id.mapInSelectionFragment:
                showMapView();
                Utils.showView(appbar);
                Utils.hideView(bottomNavigationView);
                break;
            case R.id.mapInNavigationFragment:
                showMapView();
                Utils.hideView(appbar);
                Utils.hideView(bottomNavigationView);
                break;
            case R.id.moreFragment:
                hideMapView();
                Utils.showView(bottomNavigationView);
                Utils.showView(appbar);
                break;
            default:
                Utils.showView(appbar);
                Utils.showView(bottomNavigationView);
                hideMapView();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        NavDestination navDestination = navController.getCurrentDestination();
        if (navDestination != null && navDestination.getId() == R.id.loginFragment) {
            finish();
            return;
        } else if (navDestination != null && navDestination.getId() == R.id.homeFragment) {
            finish();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mIALocationManager != null)
            mIALocationManager.destroy();
    }

    public void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    public void hideProgressBar() {
        progressBar.setVisibility(GONE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.maps_style));

            if (!success) {
//                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
//            Log.e(TAG, "Can't find style. Error: ", e);
        }

        // use the last known location and animate map to the particular lat lng
        // we use this to do it instantly

        mMap.getUiSettings().setMapToolbarEnabled(false);
//        getDeviceLocation();
        setGeoJSONLayer();
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(24.90094207, 67.07639873
                      ))
                .zoom(18)                   // Sets the zoom
                .bearing(135)                // Sets the orientation of the camera to east
                .build();

        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        initData();

        for (POI poi : pois)
            addText(this, mMap, new LatLng(poi.get_geoloc().get("lat"), poi.get_geoloc().get("lng")),
                    poi.getName(), 8, 16);
        EventBus.getDefault().postSticky(mMap);
    }

    public Marker addText(final Context context, final GoogleMap map,
                          final LatLng location, final String text, final int padding,
                          final int fontSize) {
        Marker marker = null;

        if (context == null || map == null || location == null || text == null
                || fontSize <= 0) {
            return marker;
        }

        final TextView textView = new TextView(context);
        textView.setText(text);
        textView.setTextSize(fontSize);

        final Paint paintText = textView.getPaint();

        final Rect boundsText = new Rect();
        paintText.getTextBounds(text, 0, textView.length(), boundsText);
        paintText.setTextAlign(Paint.Align.CENTER);

        final Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        final Bitmap bmpText = Bitmap.createBitmap(boundsText.width() + 2
                * padding, boundsText.height() + 2 * padding, conf);

        final Canvas canvasText = new Canvas(bmpText);
        paintText.setColor(Color.RED);


        canvasText.drawText(text, canvasText.getWidth() / 2,
                canvasText.getHeight() - padding - boundsText.bottom, paintText);


        final MarkerOptions markerOptions = new MarkerOptions()
                .position(location)
                .icon(BitmapDescriptorFactory.fromBitmap(bmpText))
                .anchor(0.5f, 1);

        marker = map.addMarker(markerOptions);

        return marker;
    }

    private void initData() {

        HashMap<String, Double> _geoloc = new HashMap<>();
        _geoloc.put("lat", 24.901613540896573);
        _geoloc.put("lng", 67.07579791545868);
        pois.add(new POI("1", 1, "Congress Center", "", _geoloc));

        HashMap<String, Double> _geoloc1 = new HashMap<>();
        _geoloc1.put("lat", 24.901723019090273);
        _geoloc1.put("lng", 67.07647383213043);
        pois.add(new POI("2", 2, "Hall 1", "", _geoloc1));

        HashMap<String, Double> _geoloc2 = new HashMap<>();

        _geoloc2.put("lat", 24.901241314311598);
        _geoloc2.put("lng", 67.07611173391342);
        pois.add(new POI("3", 3, "Hall 2", "", _geoloc2));

        HashMap<String, Double> _geoloc4 = new HashMap<>();
        _geoloc4.put("lat", 24.90097369973331);
        _geoloc4.put("lng", 67.07539290189743);
        pois.add(new POI("4", 4, "Hall 3", "", _geoloc4));

        HashMap<String, Double> _geoloc5 = new HashMap<>();

        _geoloc5.put("lat", 24.900637964259985);
        _geoloc5.put("lng", 67.07618951797485);
        pois.add(new POI("5", 5, "Hall 4", "", _geoloc5));

        HashMap<String, Double> _geoloc6 = new HashMap<>();
        _geoloc6.put("lat", 24.900294929246193);
        _geoloc6.put("lng", 67.07643628120422);
        pois.add(new POI("6", 6, "Hall 5", "", _geoloc6));

        HashMap<String, Double> _geoloc7 = new HashMap<>();

        _geoloc7.put("lat", 24.900328989502956);
        _geoloc7.put("lng", 67.0772436261177);
        pois.add(new POI("7", 7, "Hall 6", "", _geoloc7));
    }


    //    this improve user experience
    @SuppressLint("MissingPermission")
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        FusedLocationProviderClient mFusedLocationClient =
                LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null && mMap != null) {
//                    CameraUpdateFactory.newLatLngZoom(
//                            new LatLng(location.getLatitude(),
//                                    location.getLongitude()), 16.0f);
                }
            }
        });
    }

    private void hideMapView() {
        mMapView.setVisibility(View.GONE);
    }

    private void showMapView() {
        mMapView.setVisibility(View.VISIBLE);

    }

    private void setGeoJSONLayer() {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(loadJSONFromAsset("ground_indoor.geojson"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (layer != null) {
            layer.removeLayerFromMap();
        }

//        if (textMarkers.size() > 0) {
//            for (Marker marker : textMarkers) {
//                marker.remove();
//            }
//            textMarkers.clear();
//        }

        layer = new GeoJsonLayer(mMap, jsonObject);
        layer.addLayerToMap();
        List<GeoJsonFeature> geoJsonFeatureList = Lists.newArrayList(layer.getFeatures());


        for (GeoJsonFeature geoJsonFeature : geoJsonFeatureList) {
            GeoJsonPolygonStyle geoJsonPolygonStyle = new GeoJsonPolygonStyle();
            geoJsonPolygonStyle.setFillColor(Color.rgb(
                    Integer.parseInt(geoJsonFeature.getProperty("red")),
                    Integer.parseInt(geoJsonFeature.getProperty("green")),
                    Integer.parseInt(geoJsonFeature.getProperty("blue"))));


//            if (geoJsonFeature.getProperty("geo_lat") != null && geoJsonFeature.getProperty("geo_lng") != null
//                    && geoJsonFeature.getProperty("Name") != null) {
////                Log.d(TAG, geoJsonFeature.getProperty("name"));
//                textMarkers.add(addText(getContext(), mMap, new LatLng(Double.parseDouble(geoJsonFeature.getProperty("geo_lat")),
//                                Double.parseDouble(geoJsonFeature.getProperty("geo_lng"))),
//                        geoJsonFeature.getProperty("name"), 8, 12));
//            }

            geoJsonFeature.setPolygonStyle(geoJsonPolygonStyle);
        }
    }

    public String loadJSONFromAsset(String filename) {
        String json = null;
        try {
            InputStream is = getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }
}
