package app.com.mapinevents.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.github.wrdlbrnft.sortedlistadapter.SortedListAdapter;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class Schedule implements SortedListAdapter.ViewModel, Parcelable {

    private String id;
    private int rank;
    private String title;
    private String description;
    private String category;
    private String place;
    private Timestamp start_time;
    private Timestamp end_time;
    private HashMap<String, Double> _geoloc;
    private String url;

    public List<Speaker> getSpeakers() {
        return speakers;
    }

    public void setSpeakers(List<Speaker> speakers) {
        this.speakers = speakers;
    }

    private List<Speaker> speakers;

    public Schedule(String id, int rank, String title, String description, String category, String place, Timestamp start_time, Timestamp end_time, HashMap<String, Double> _geoloc, String url, List<Speaker> speakers) {
        this.id = id;
        this.rank = rank;
        this.title = title;
        this.description = description;
        this.category = category;
        this.place = place;
        this.start_time = start_time;
        this.end_time = end_time;
        this._geoloc = _geoloc;
        this.url = url;
        this.speakers = speakers;
    }

    public Schedule() {
    }


    protected Schedule(Parcel in) {
        id = in.readString();
        rank = in.readInt();
        title = in.readString();
        description = in.readString();
        category = in.readString();
        place = in.readString();
        start_time = in.readParcelable(Timestamp.class.getClassLoader());
        end_time = in.readParcelable(Timestamp.class.getClassLoader());
        _geoloc = in.readHashMap(HashMap.class.getClassLoader());
        speakers = in.readArrayList(List.class.getClassLoader());
    }

    public static final Creator<Schedule> CREATOR = new Creator<Schedule>() {
        @Override
        public Schedule createFromParcel(Parcel in) {
            return new Schedule(in);
        }

        @Override
        public Schedule[] newArray(int size) {
            return new Schedule[size];
        }
    };

    @Override
    public <T> boolean isSameModelAs(@NonNull T model) {
        if (model instanceof Schedule) {
            final Schedule other = (Schedule) model;
            return other.id == id;
        }
        return false;
    }

    @Override
    public <T> boolean isContentTheSameAs(@NonNull T model) {
        if (model instanceof Schedule) {
            final Schedule other = (Schedule) model;
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

    public void setId(String id) {
        this.id = id;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public Timestamp getStart_time() {
        return start_time;
    }

    public void setStart_time(Timestamp start_time) {
        this.start_time = start_time;
    }

    public Timestamp getEnd_time() {
        return end_time;
    }

    public void setEnd_time(Timestamp end_time) {
        this.end_time = end_time;
    }

    public HashMap<String, Double> get_geoloc() {
        return _geoloc;
    }

    public void set_geoloc(HashMap<String, Double> _geoloc) {
        this._geoloc = _geoloc;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeInt(rank);
        parcel.writeString(title);
        parcel.writeString(description);
        parcel.writeString(category);
        parcel.writeString(place);
        parcel.writeParcelable(start_time, i);
        parcel.writeParcelable(end_time, i);
        parcel.writeMap(_geoloc);
//        parcel.writearray(speakers, 0);
    }

    public String startTimeToDay(Timestamp start_time) {
        String pattern = "EEE";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
//        Calendar cal = Calendar.getInstance();
//        cal.setTime(start_time.toDate());
//        cal.add(Calendar.HOUR, -12);
        return simpleDateFormat.format(start_time.toDate());
    }

    public String startTimeToTime(Timestamp start_time) {
        String pattern = "hh:mm";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
//        Calendar cal = Calendar.getInstance();
//        cal.setTime(start_time.toDate());
//        cal.add(Calendar.HOUR, -12);
        return simpleDateFormat.format(start_time.toDate());
    }
    public String startTimeToAMPM(Timestamp start_time) {
        String pattern = "a";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
//        Calendar cal = Calendar.getInstance();
//        cal.setTime(start_time.toDate());
//        cal.add(Calendar.HOUR, -12);
        return simpleDateFormat.format(start_time.toDate());
    }

//    Tue, 17th, 2:00 PM - 3:00 PM
    public String timeToDetailFormat(Timestamp start_time, Timestamp end_time) {
        String pattern = "EEE, dd, hh:mm a";
        String pattern1 = "hh:mm a";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat(pattern1);
        return simpleDateFormat.format(start_time.toDate()) + " - " + simpleDateFormat1.format(end_time.toDate());
    }


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;

    }
}
