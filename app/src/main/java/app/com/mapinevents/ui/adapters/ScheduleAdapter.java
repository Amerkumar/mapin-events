package app.com.mapinevents.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.github.wrdlbrnft.sortedlistadapter.SortedListAdapter;

import java.util.Comparator;

import app.com.mapinevents.databinding.ScheduleItemBinding;
import app.com.mapinevents.model.Schedule;
import app.com.mapinevents.model.Schedule;
import app.com.mapinevents.ui.viewholders.ScheduleViewHolder;

public class ScheduleAdapter extends SortedListAdapter<Schedule> {

    public interface Listener {
        void onScheduleItemClicked(Schedule schedule);
    }

    private final Listener mScheduleItemClickListener;

    public ScheduleAdapter(@NonNull Context context, @NonNull Class<Schedule> itemClass, @NonNull Comparator<Schedule> comparator, Listener listener) {
        super(context, itemClass, comparator);
        mScheduleItemClickListener = listener;
    }

    @NonNull
    @Override
    protected ViewHolder<? extends Schedule> onCreateViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent, int viewType) {
        final ScheduleItemBinding binding = ScheduleItemBinding.inflate(inflater, parent, false);
        return new ScheduleViewHolder(binding, mScheduleItemClickListener);
    }

}
