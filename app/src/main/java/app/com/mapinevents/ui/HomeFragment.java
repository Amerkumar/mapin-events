package app.com.mapinevents.ui;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.wrdlbrnft.sortedlistadapter.SortedListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.indooratlas.android.sdk.IAExtraInfo;
import com.indooratlas.android.sdk.IALocationManager;

import java.util.Comparator;
import java.util.List;

import app.com.mapinevents.R;
import app.com.mapinevents.SingletonAppClass;
import app.com.mapinevents.databinding.HomeFragmentBinding;
import app.com.mapinevents.model.Annoucement;
import app.com.mapinevents.ui.adapters.AnnoucementAdapter;
import app.com.mapinevents.viewmodels.HomeViewModel;
import app.com.mapinevents.viewmodels.SharedViewModel;

public class HomeFragment extends Fragment {

    private HomeViewModel mViewModel;
    private HomeFragmentBinding binding;
    private AnnoucementAdapter annoucementAdapter;


    private static final Comparator<Annoucement> COMPARATOR_ANNOUCEMENT = new SortedListAdapter.ComparatorBuilder<Annoucement>()
            .setOrderForModel(Annoucement.class, (a, b) -> Integer.signum(a.getRank() - b.getRank()))
            .build();
    private SharedViewModel mSharedViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


        if (user == null) {
            Navigation.findNavController(getActivity(), R.id.nav_host_fragment)
                    .navigate(R.id.loginFragment);
        }

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = HomeFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        mSharedViewModel = ViewModelProviders.of(this).get(SharedViewModel.class);

        if (FirebaseAuth.getInstance().getCurrentUser() != null && SingletonAppClass.getInstance().isFIRST_APP_OPEN()) {
            FirebaseInstanceId.getInstance().getInstanceId()
                    .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                        @Override
                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                            if (!task.isSuccessful()) {
//                            Log.w(TAG, "getInstanceId failed", task.getException());
                                return;
                            }
                            // Get new Instance ID token
                            String token = task.getResult().getToken();
                            SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                            boolean swichBool = sharedPref.getBoolean(getString(R.string.notification_annoucement_key), true);
                            SingletonAppClass.getInstance().setFIRST_APP_OPEN(false);
                            mViewModel.sendFCMToken(FirebaseAuth.getInstance().getCurrentUser(),token, swichBool);
                        }
                    });

        }

        binding.exploreCardView.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_homeFragment_to_mapInFragment));

        annoucementAdapter = new AnnoucementAdapter(getContext(), Annoucement.class, COMPARATOR_ANNOUCEMENT);

//        mSharedViewModel.getIaExtraInfoMutableLiveData().observe(getActivity(), new Observer<IAExtraInfo>() {
//            @Override
//            public void onChanged(IAExtraInfo iaExtraInfo) {
//
//                if (iaExtraInfo != null && SingletonAppClass.getInstance().getIaExtraInfo() == null) {
//                    SingletonAppClass.getInstance().setIaExtraInfo(iaExtraInfo);
//                    mViewModel.setUserTraceId(iaExtraInfo);
//                }
//            }
//        });


        binding.annoucements.setAdapter(annoucementAdapter);
        binding.annoucements.setLayoutManager(new LinearLayoutManager(getContext()));
        mViewModel.getmRecentAnnoucements().observe(this, new Observer<List<Annoucement>>() {
            @Override
            public void onChanged(List<Annoucement> annoucements) {
                if (annoucements != null) {
                    ((MainActivity)getActivity()).hideProgressBar();
                    Log.d(HomeFragment.class.getSimpleName(), String.valueOf(annoucements.size()));
                    annoucementAdapter.edit().removeAll().commit();
                    annoucementAdapter.edit()
                            .replaceAll(annoucements)
                            .commit();
                    if (annoucements.size() == 0) {
                        binding.noAnnoucementsTextview.setVisibility(View.VISIBLE);
                    } else {
                        binding.noAnnoucementsTextview.setVisibility(View.GONE);
                    }
                } else {
                    ((MainActivity)getActivity()).showProgressBar();
                }
            }
        });
    }

}
