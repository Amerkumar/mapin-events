package app.com.mapinevents.ui;

import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import app.com.mapinevents.R;
import app.com.mapinevents.databinding.AboutDeveloperTeamFragmentBinding;

public class AboutDeveloperTeamFragment extends Fragment {

    private AboutDeveloperTeamViewModel mViewModel;
    private Toolbar toolbar;
    private AboutDeveloperTeamFragmentBinding binding;

    public static AboutDeveloperTeamFragment newInstance() {
        return new AboutDeveloperTeamFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = AboutDeveloperTeamFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(AboutDeveloperTeamViewModel.class);
        // TODO: Use the ViewModel
        toolbar = getActivity().findViewById(R.id.toolbar);

        binding.contactClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "https://www.mapin.app";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        toolbar.setTitle("");
    }
}
