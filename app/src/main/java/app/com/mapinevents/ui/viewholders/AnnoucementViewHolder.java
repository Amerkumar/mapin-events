package app.com.mapinevents.ui.viewholders;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.wrdlbrnft.sortedlistadapter.SortedListAdapter;

import app.com.mapinevents.databinding.AnnoucementItemBinding;
import app.com.mapinevents.model.Annoucement;

public class AnnoucementViewHolder extends SortedListAdapter.ViewHolder<Annoucement> {

    private final AnnoucementItemBinding binding;

    public AnnoucementViewHolder(@NonNull AnnoucementItemBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    protected void performBind(Annoucement item) {
        binding.setModel(item);
    }
}
