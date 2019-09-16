package app.com.mapinevents.ui.viewholders;

import android.content.Context;

import androidx.annotation.NonNull;

import com.github.wrdlbrnft.sortedlistadapter.SortedListAdapter;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;

import app.com.mapinevents.R;
import app.com.mapinevents.databinding.AgendaItemBinding;
import app.com.mapinevents.databinding.PoiItemBinding;
import app.com.mapinevents.model.Agenda;
import app.com.mapinevents.model.POI;
import app.com.mapinevents.ui.adapters.POIAdapter;

public class POIViewHolder extends SortedListAdapter.ViewHolder<POI> {

    private Context context;
    private final PoiItemBinding binding;

    public POIViewHolder(@NonNull PoiItemBinding binding, Context context, POIAdapter.Listener mPOIItemClickListener) {
        super(binding.getRoot());
        this.binding = binding;
        this.context = context;
        binding.setListener(mPOIItemClickListener);
    }

    protected void performBind(POI item) {
        binding.setModel(item);
    }
}
