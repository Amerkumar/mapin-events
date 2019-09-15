package app.com.mapinevents.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import app.com.mapinevents.R;
import app.com.mapinevents.databinding.MapFilterBottomSheetDialogFragmentBinding;
import app.com.mapinevents.utils.MapInConstants;
import app.com.mapinevents.viewmodels.SharedViewModel;

public class MapFilterBottomSheetDialogFragment extends BottomSheetDialogFragment {

    private MapFilterBottomSheetDialogFragmentBinding binding;
    private SharedViewModel model;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = MapFilterBottomSheetDialogFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        model = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);

        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        int mapStyle = sharedPref.getInt(getString(R.string.map_style), MapInConstants.MAPBOX_STREET);

        switch (mapStyle) {
            case MapInConstants.MAPBOX_STREET:
                binding.street.setChecked(true);
                break;
            case MapInConstants.MAPBOX_LIGHT:
                binding.light.setChecked(true);
                break;
            case MapInConstants.MAPBOX_DARK:
                binding.dark.setChecked(true);
                break;
        }

        boolean hallsVisible = sharedPref.getBoolean(getString(R.string.labels_halls), true);
        boolean stallsVisible = sharedPref.getBoolean(getString(R.string.labels_stalls), false);
        boolean threeDimenViewEnable = sharedPref.getBoolean(getString(R.string.three_dimen_view), true);

        binding.hallsCheckbox.setChecked(hallsVisible);
        binding.stallsCheckbox.setChecked(stallsVisible);
        binding.threedViewCheckbox.setChecked(threeDimenViewEnable);

        binding.hallsCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                    SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putBoolean(getString(R.string.labels_halls), b);
                    editor.commit();
                    model.setHallsVisible(b);
                    binding.hallsCheckbox.setChecked(b);

            }
        });

        binding.stallsCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean(getString(R.string.labels_stalls), b);
                editor.commit();
                model.setStallsVisible(b);
                binding.stallsCheckbox.setChecked(b);

            }
        });

        binding.threedViewCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean(getString(R.string.three_dimen_view), b);
                editor.commit();
                model.setThreeDimenViewEnable(b);
                binding.threedViewCheckbox.setChecked(b);

            }
        });


        binding.radioGroupMapType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {

                // This will get the radiobutton that has changed in its check state
                RadioButton checkedRadioButton = (RadioButton) radioGroup.findViewById(i);
                // This puts the value (true/false) into the variable

                boolean isChecked = checkedRadioButton.isChecked();

                // If the radiobutton that has changed in check state is now checked...
                if (isChecked) {
                    // Changes the textview's text to "Checked: example radiobutton text"
//                    checkedRadioButton.setText("Checked");
                    SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putInt(getString(R.string.map_style),Integer.parseInt(checkedRadioButton.getTag().toString()));
                    editor.commit();
                    model.setMapTypeSelected(Integer.parseInt(checkedRadioButton.getTag().toString()));
                    checkedRadioButton.setChecked(true);
                }
            }
        });
    }
}
