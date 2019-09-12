package app.com.mapinevents.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.github.wrdlbrnft.sortedlistadapter.SortedListAdapter;

import java.util.Comparator;

import app.com.mapinevents.databinding.AgendaItemBinding;
import app.com.mapinevents.model.Agenda;
import app.com.mapinevents.ui.viewholders.AgendaViewHolder;

public class AgendaAdapter extends SortedListAdapter<Agenda> {

    private Context context;

    public AgendaAdapter(@NonNull Context context, @NonNull Class<Agenda> itemClass, @NonNull Comparator<Agenda> comparator) {
        super(context, itemClass, comparator);
        this.context = context;
    }

    @NonNull
    @Override
    protected ViewHolder<? extends Agenda> onCreateViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent, int viewType) {
        final AgendaItemBinding binding = AgendaItemBinding.inflate(inflater, parent, false);
        return new AgendaViewHolder(binding, context);
    }
}
