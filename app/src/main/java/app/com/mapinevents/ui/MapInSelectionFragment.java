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

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.net.URI;
import java.net.URISyntaxException;

import app.com.mapinevents.R;
import app.com.mapinevents.databinding.MapInSelectionFragmentBinding;
import app.com.mapinevents.model.POI;
import app.com.mapinevents.utils.MapInConstants;
import app.com.mapinevents.utils.Utils;
import app.com.mapinevents.viewmodels.SharedViewModel;

public class MapInSelectionFragment extends Fragment {


    private POI poi;
    private MapInSelectionFragmentBinding binding;
    private SharedViewModel model;
    private GoogleMap mMap;
    private Marker marker;

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
        model = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);
        poi = MapInSelectionFragmentArgs.fromBundle(getArguments()).getPoi();
        ((Toolbar)getActivity().findViewById(R.id.toolbar)).setTitle(poi.getName());
        binding.poiName.setText(poi.getName());
        binding.poiNumber.setText(poi.getName());
        binding.poiNavigate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MapInSelectionFragmentDirections.ActionMapInSelectionFragmentToMapInNavigationFragment actionMapInSelectionFragmentToMapInNavigationFragment =
                        MapInSelectionFragmentDirections.actionMapInSelectionFragmentToMapInNavigationFragment(poi);
                Navigation.findNavController(view).navigate(actionMapInSelectionFragmentToMapInNavigationFragment);
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
        if (marker != null && marker.isVisible())
            marker.remove();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(GoogleMap map) {
        mMap = map;
//        setGeoJSONLayer();
//        setStallsLayer();
        mMap.animateCamera(CameraUpdateFactory
                .newLatLngZoom(new LatLng(24.90180884018499,67.07578086274215), 18.0f));

        if (marker != null)
            marker.remove();
        marker = mMap.addMarker(new MarkerOptions()
                .icon(Utils.getBitmapFromVector(getContext(),
                        R.drawable.ic_location_pin_48px_square,
                        getResources().getColor(R.color.mapinBlue)))
                .position(new LatLng(poi.get_geoloc().get("lat"), poi.get_geoloc().get("lng"))));

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(poi.get_geoloc().get("lat"), poi.get_geoloc().get("lng")), 19.5f));
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
