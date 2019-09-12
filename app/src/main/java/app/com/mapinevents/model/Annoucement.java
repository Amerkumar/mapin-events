package app.com.mapinevents.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.github.wrdlbrnft.sortedlistadapter.SortedListAdapter;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Objects;

public class Annoucement implements SortedListAdapter.ViewModel, Parcelable {

    private String id;
    private int rank;
    private Timestamp timestamp;
    private String title;
    private String description;


    public Annoucement() {
    }

    public Annoucement(String id, int rank, Timestamp timestamp, String title, String description) {
        this.id = id;
        this.rank = rank;
        this.timestamp = timestamp;
        this.title = title;
        this.description = description;
    }


    protected Annoucement(Parcel in) {
        id = in.readString();
        rank = in.readInt();
        timestamp = in.readParcelable(Timestamp.class.getClassLoader());
        title = in.readString();
        description = in.readString();
    }

    public static final Creator<Annoucement> CREATOR = new Creator<Annoucement>() {
        @Override
        public Annoucement createFromParcel(Parcel in) {
            return new Annoucement(in);
        }

        @Override
        public Annoucement[] newArray(int size) {
            return new Annoucement[size];
        }
    };

    public String getId() {
        return id;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String timeStampToReadableDate(Timestamp timestamp) {
        String pattern = "dd MMM, hh:mm a";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        String date = simpleDateFormat.format(timestamp.toDate());
        return date;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeInt(rank);
        parcel.writeParcelable(timestamp, i);
        parcel.writeString(title);
        parcel.writeString(description);

    }

    @Override
    public <T> boolean isSameModelAs(@NonNull T model) {
        if (model instanceof Annoucement) {
            final Annoucement other = (Annoucement) model;
            return other.id == id;
        }
        return false;
    }

    @Override
    public <T> boolean isContentTheSameAs(@NonNull T model) {
        if (model instanceof Annoucement) {
            final Annoucement other = (Annoucement) model;
            if (rank != other.rank) {
                return false;
            }
            return Objects.equals(title, other.title);

        }
        return false;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }
}
