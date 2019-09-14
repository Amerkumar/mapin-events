package app.com.mapinevents.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.appbar.AppBarLayout;
import com.squareup.picasso.Picasso;

import app.com.mapinevents.R;
import app.com.mapinevents.databinding.ScheduleDetailFragmentBinding;
import app.com.mapinevents.databinding.SpeakerItemBinding;
import app.com.mapinevents.model.Schedule;
import app.com.mapinevents.model.Speaker;
import app.com.mapinevents.utils.Utils;
import app.com.mapinevents.viewmodels.ScheduleDetailViewModel;

public class ScheduleDetailFragment extends Fragment {

    private ScheduleDetailViewModel mViewModel;
    private AppBarLayout mAppBar;
    private Toolbar mToolbar;
    private ScheduleDetailFragmentBinding binding;

    public static ScheduleDetailFragment newInstance() {
        return new ScheduleDetailFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = ScheduleDetailFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(ScheduleDetailViewModel.class);
        mAppBar = getActivity().findViewById(R.id.appBar);
        mToolbar = getActivity().findViewById(R.id.toolbar);



        Schedule schedule = ScheduleDetailFragmentArgs.fromBundle(getArguments()).getScheduleModel();

        if (schedule.getUrl() != null && !schedule.getUrl().isEmpty()) {
            Picasso.get().load(schedule.getUrl()).placeholder(R.drawable.error_placeholder).into(binding.mainImageViewSchedule);
        }

        if (schedule.getTitle() != null && !schedule.getTitle().isEmpty()) {
            binding.titleTextview.setText(schedule.getTitle());
        } else {
            binding.titleTextview.setVisibility(View.GONE);
        }
        binding.timeTextview.setText(schedule.timeToDetailFormat(schedule.getStart_time(), schedule.getEnd_time()));

        if (schedule.getPlace() != null && !schedule.getPlace().isEmpty())
            binding.placeTextview.setText(schedule.getPlace());
        else
            binding.placeTextview.setVisibility(View.GONE);

        if (schedule.getDescription() != null && !schedule.getDescription().isEmpty())
            binding.descriptionTextview.setText(schedule.getDescription());
        else
            binding.descriptionTextview.setVisibility(View.GONE);

        if (schedule.getSpeakers() == null || schedule.getSpeakers().size() == 0) {
            binding.speakersContainer.setVisibility(View.GONE);
            binding.speakersTextview.setVisibility(View.GONE);
        }

        if (schedule.getSpeakers() != null)
            for (Speaker speaker : schedule.getSpeakers()) {
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                SpeakerItemBinding speakerItemBinding = SpeakerItemBinding.inflate(inflater);
                speakerItemBinding.nameTextview.setText(speaker.getName());
                if (speaker.getIntro()!= null && !speaker.getIntro().isEmpty())
                    speakerItemBinding.introTextview.setText(speaker.getIntro());
                else
                    speakerItemBinding.introTextview.setVisibility(View.GONE);

                if (speaker.getUrl() != null && !speaker.getUrl().isEmpty())
                    Picasso.get().load(speaker.getUrl()).placeholder(R.drawable.ic_person_24dp).into(speakerItemBinding.profileImage);

                binding.speakersContainer.addView(speakerItemBinding.getRoot());
            }


    }

    @Override
    public void onResume() {
        super.onResume();
        int appBarColor = ContextCompat.getColor(getContext(), android.R.color.transparent);
        appBarColor = Utils.getColorWithAlpha(0.5f, appBarColor);
        mAppBar.setBackgroundColor(appBarColor);
        mToolbar.setTitle("");
        final Drawable upArrow = getResources().getDrawable(R.drawable.ic_arrow_back_24dp);
        upArrow.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        mToolbar.setNavigationIcon(upArrow);
    }

    @Override
    public void onPause() {
        super.onPause();
        mAppBar.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.white));
    }
}
