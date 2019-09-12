package app.com.mapinevents.ui;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.wrdlbrnft.sortedlistadapter.SortedListAdapter;

import java.util.Comparator;
import java.util.List;

import app.com.mapinevents.databinding.AgendaFragmentBinding;
import app.com.mapinevents.model.Agenda;
import app.com.mapinevents.ui.adapters.AgendaAdapter;
import app.com.mapinevents.viewmodels.AgendaViewModel;

public class AgendaFragment extends Fragment {

    private AgendaViewModel mViewModel;
    private AgendaFragmentBinding binding;

    private static final Comparator<Agenda> COMPARATOR_AGENDA = new SortedListAdapter.ComparatorBuilder<Agenda>()
            .setOrderForModel(Agenda.class, (a, b) -> Integer.signum(a.getRank() - b.getRank()))
            .build();


    public static AgendaFragment newInstance() {
        return new AgendaFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = AgendaFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(AgendaViewModel.class);

        AgendaAdapter agendaAdapter = new AgendaAdapter(getContext(), Agenda.class, COMPARATOR_AGENDA);
        binding.agendaRecyclerView.setAdapter(agendaAdapter);
        binding.agendaRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mViewModel.getmAgendaObservable().observe(this, new Observer<List<Agenda>>() {
            @Override
            public void onChanged(List<Agenda> agenda) {
                if (agenda != null) {
                    ((MainActivity)getActivity()).hideProgressBar();
                    Log.d(HomeFragment.class.getSimpleName(), String.valueOf(agenda.size()));
                    agendaAdapter.edit().removeAll().commit();
                    agendaAdapter.edit()
                            .replaceAll(agenda)
                            .commit();
                } else {
                    ((MainActivity)getActivity()).showProgressBar();
                }
            }
        });
    }

}
