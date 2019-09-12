package app.com.mapinevents.ui.viewholders;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

import com.github.wrdlbrnft.sortedlistadapter.SortedListAdapter;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;

import app.com.mapinevents.R;
import app.com.mapinevents.databinding.AgendaItemBinding;
import app.com.mapinevents.databinding.AnnoucementItemBinding;
import app.com.mapinevents.model.Agenda;
import app.com.mapinevents.model.Annoucement;

public class AgendaViewHolder extends SortedListAdapter.ViewHolder<Agenda> {

    private Context context;
    private final AgendaItemBinding binding;

    public AgendaViewHolder(@NonNull AgendaItemBinding binding, Context context) {
        super(binding.getRoot());
        this.binding = binding;
        this.context = context;
    }

    protected void performBind(Agenda item) {
        String iconResourceName = "ic_" + item.getCategory().replaceAll(" ", "_").toLowerCase();

        // if not zero then resource is present
        if (context.getResources().getIdentifier(iconResourceName, "drawable", context.getPackageName()) != 0) {
           binding.agendaItemImageView.setBackgroundResource(context.getResources().getIdentifier(iconResourceName, "drawable", context.getPackageName()));
        }
        // resource not present
        else {
            binding.agendaItemImageView.setBackgroundResource(context.getResources().getIdentifier("ic_more_vert_24dp", "drawable", context.getPackageName()));
        }


        binding.agendaItemContainer.setBackgroundColor(context.getResources().getColor(timeToColorOfDay(item.getStart_time())));

        binding.setModel(item);
    }

    private int timeToColorOfDay(Timestamp start_time) {
        String pattern = "HH";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        String date = simpleDateFormat.format(start_time.toDate());
        int hour = Integer.parseInt(date);
        if (hour < 11) {
            return R.color.colorMorning;
        } else if (hour < 15) {
            return R.color.colorNoon;
        } else if (hour < 17) {
            return R.color.colorAfternoon;
        } else {
            return R.color.colorEvening;
        }
    }
}
