package app.com.mapinevents.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.github.wrdlbrnft.sortedlistadapter.SortedListAdapter;

import java.util.Comparator;

import app.com.mapinevents.databinding.AnnoucementItemBinding;
import app.com.mapinevents.model.Annoucement;
import app.com.mapinevents.ui.viewholders.AnnoucementViewHolder;

public class AnnoucementAdapter extends SortedListAdapter<Annoucement> {

    public AnnoucementAdapter(@NonNull Context context, @NonNull Class<Annoucement> itemClass, @NonNull Comparator<Annoucement> comparator) {
        super(context, itemClass, comparator);

    }

    @NonNull
    @Override
    protected ViewHolder<? extends Annoucement> onCreateViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent, int viewType) {
        final AnnoucementItemBinding binding = AnnoucementItemBinding.inflate(inflater, parent, false);
        return new AnnoucementViewHolder(binding);
    }
}
