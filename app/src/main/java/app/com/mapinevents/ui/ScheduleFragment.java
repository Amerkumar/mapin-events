package app.com.mapinevents.ui;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.graphics.Color;
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

import java.util.Comparator;
import java.util.List;

import app.com.mapinevents.R;
import app.com.mapinevents.databinding.ScheduleFragmentBinding;
import app.com.mapinevents.model.Agenda;
import app.com.mapinevents.model.Schedule;
import app.com.mapinevents.ui.adapters.ScheduleAdapter;
import app.com.mapinevents.viewmodels.ScheduleViewModel;

public class ScheduleFragment extends Fragment {

    private static final String KEY_LAYOUT = "KEY";
    private ScheduleViewModel mViewModel;

    private static final Comparator<Schedule> COMPARATOR_SCHEDULE = new SortedListAdapter.ComparatorBuilder<Schedule>()
            .setOrderForModel(Schedule.class, (a, b) -> Integer.signum(a.getRank() - b.getRank()))
            .build();
    private ScheduleFragmentBinding binding;
    private View rootView;
    private ScheduleAdapter scheduleAdapter;


    public static ScheduleFragment newInstance() {
        return new ScheduleFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        if (rootView == null) {
            binding = ScheduleFragmentBinding.inflate(inflater, container, false);
            rootView = binding.getRoot();
            scheduleAdapter = new ScheduleAdapter(getContext(), Schedule.class, COMPARATOR_SCHEDULE, schedule -> {

                ScheduleFragmentDirections.ActionScheduleFragmentToScheduleDetailFragment actionScheduleFragmentToScheduleDetailFragment =
                        ScheduleFragmentDirections.actionScheduleFragmentToScheduleDetailFragment(schedule);
                Navigation.findNavController(binding.getRoot()).navigate(actionScheduleFragmentToScheduleDetailFragment);
            });
            binding.scheduleRecyclerView.setAdapter(scheduleAdapter);
            binding.scheduleRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        }
        return binding.getRoot();
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(ScheduleViewModel.class);

        mViewModel.getScheduleObservable().observe(this, new Observer<List<Schedule>>() {
            @Override
            public void onChanged(List<Schedule> schedules) {
                if (schedules != null) {
                    ((MainActivity)getActivity()).hideProgressBar();
//                    scheduleAdapter.edit().removeAll().commit();
                    Log.d(ScheduleFragment.class.getSimpleName(), String.valueOf(scheduleAdapter.getItemCount()));
                    scheduleAdapter.edit()
                            .replaceAll(schedules)
                            .commit();
                } else {
                    ((MainActivity)getActivity()).showProgressBar();
                }
            }
        });
    }

}
