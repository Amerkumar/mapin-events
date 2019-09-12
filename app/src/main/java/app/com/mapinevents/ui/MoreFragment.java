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

        binding = MoreFragmentBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(MoreViewModel.class);
        // TODO: Use the ViewModel
        binding.signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
//                AuthUI.getInstance()
//                        .signOut(getContext())
//                        .addOnCompleteListener(new OnCompleteListener<Void>() {
//                            public void onComplete(@NonNull Task<Void> task) {
//
//                            }
//                        });
                FirebaseAuth.getInstance()
                        .signOut()
                ;
                Navigation.findNavController(view)
                        .navigate(R.id.loginFragment);

            }
        });
    }

}
