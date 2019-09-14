package app.com.mapinevents.ui.viewholders;

import androidx.annotation.NonNull;

import com.github.wrdlbrnft.sortedlistadapter.SortedListAdapter;

import app.com.mapinevents.databinding.AnnoucementItemBinding;
import app.com.mapinevents.databinding.ScheduleItemBinding;
import app.com.mapinevents.model.Annoucement;
import app.com.mapinevents.model.Schedule;
import app.com.mapinevents.ui.adapters.ScheduleAdapter;

public class ScheduleViewHolder extends SortedListAdapter.ViewHolder<Schedule> {

    private final ScheduleItemBinding binding;

    public ScheduleViewHolder(@NonNull ScheduleItemBinding binding, ScheduleAdapter.Listener listener) {
        super(binding.getRoot());
        this.binding = binding;
        binding.setListener(listener);

    }

    protected void performBind(Schedule item) {
        binding.setModel(item);
    }
}
