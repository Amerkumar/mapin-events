package app.com.mapinevents.ui;

import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.instabug.library.Instabug;

import app.com.mapinevents.R;
import app.com.mapinevents.databinding.MoreFragmentBinding;
import app.com.mapinevents.viewmodels.MoreViewModel;

public class MoreFragment extends Fragment {

    private MoreViewModel mViewModel;
    private MoreFragmentBinding binding;

    public static MoreFragment newInstance() {
        return new MoreFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = MoreFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(MoreViewModel.class);
        // TODO: Use the ViewModel
        binding.infoContainer.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_moreFragment_to_infoFragment));
        binding.settingsContainer.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_moreFragment_to_settingsFragment));
        binding.feedbackContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user.getDisplayName() != null)
                    Instabug.identifyUser(user.getDisplayName(), user.getEmail());
                Instabug.show();

            }
        });
    }

}
