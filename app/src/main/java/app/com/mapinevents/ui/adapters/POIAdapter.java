package app.com.mapinevents.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.github.wrdlbrnft.sortedlistadapter.SortedListAdapter;

import java.util.Comparator;

import app.com.mapinevents.databinding.AgendaItemBinding;
import app.com.mapinevents.databinding.PoiItemBinding;
import app.com.mapinevents.model.Agenda;
import app.com.mapinevents.model.POI;
import app.com.mapinevents.model.Schedule;
import app.com.mapinevents.ui.viewholders.AgendaViewHolder;
import app.com.mapinevents.ui.viewholders.POIViewHolder;

public class POIAdapter extends SortedListAdapter<POI> {

    private Context context;

    public interface Listener {
        void onPOIItemClicked(POI poi);
    }

    private final POIAdapter.Listener mPOIItemClickListener;


    public POIAdapter(@NonNull Context context, @NonNull Class<POI> itemClass, @NonNull Comparator<POI> comparator, Listener listener) {
        super(context, itemClass, comparator);
        this.context = context;
        mPOIItemClickListener = listener;

    }

    @NonNull
    @Override
    protected ViewHolder<? extends POI> onCreateViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent, int viewType) {
        final PoiItemBinding binding = PoiItemBinding.inflate(inflater, parent, false);
        return new POIViewHolder(binding, context, mPOIItemClickListener);
    }
}
