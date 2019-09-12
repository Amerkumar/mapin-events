package app.com.mapinevents.model;

import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.databinding.BindingAdapter;

import com.github.wrdlbrnft.sortedlistadapter.SortedListAdapter;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Objects;

public class Agenda implements SortedListAdapter.ViewModel {

    private String id;
    private int rank;
    private String title;
    private String category;
    private Timestamp start_time;
    private Timestamp end_time;


    public Agenda() {}

    public Agenda(String id, int rank, String title, String category, Timestamp start_time, Timestamp end_time) {
        this.id = id;
        this.rank = rank;
        this.title = title;
        this.category = category;
        this.start_time = start_time;
        this.end_time = end_time;
    }

    @Override
    public <T> boolean isSameModelAs(@NonNull T model) {
        if (model instanceof Annoucement) {
            final Agenda other = (Agenda) model;
            return other.id == id;
        }
        return false;
    }

    @Override
    public <T> boolean isContentTheSameAs(@NonNull T model) {
        if (model instanceof Agenda) {
            final Agenda other = (Agenda) model;
            if (rank != other.rank) {
                return false;
            }
            return Objects.equals(title, other.title);

        }
        return false;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public Timestamp getStart_time() {
        return start_time;
    }

    public Timestamp getEnd_time() {
        return end_time;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String timestampsToReadableDates(Timestamp start_time, Timestamp end_time) {
        String pattern = "EEE, hh:mm a";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        String start_date = simpleDateFormat.format(start_time.toDate());
        String end_date = simpleDateFormat.format(end_time.toDate());
        return start_date +  " - " + end_date;
    }



}
