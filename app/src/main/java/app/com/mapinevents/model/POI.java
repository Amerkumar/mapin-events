package app.com.mapinevents.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.github.wrdlbrnft.sortedlistadapter.SortedListAdapter;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Objects;

public class POI implements SortedListAdapter.ViewModel, Parcelable {

    private String id;
    private int rank;
    private String name;
    private String number;
    private HashMap<String, Double> _geoloc;

    public POI() {}

    public POI(String id, int rank, String name, String number, HashMap<String, Double> _geoloc) {
        this.id = id;
        this.rank = rank;
        this.name = name;
        this.number = number;
        this._geoloc = _geoloc;
    }


    protected POI(Parcel in) {
        id = in.readString();
        rank = in.readInt();
        name = in.readString();
        number = in.readString();
    }

    public static final Creator<POI> CREATOR = new Creator<POI>() {
        @Override
        public POI createFromParcel(Parcel in) {
            return new POI(in);
        }

        @Override
        public POI[] newArray(int size) {
            return new POI[size];
        }
    };

    @Override
    public <T> boolean isSameModelAs(@NonNull T model) {
        if (model instanceof POI) {
            final POI other = (POI) model;
            return other.id == id;
        }
        return false;
    }

    @Override
    public <T> boolean isContentTheSameAs(@NonNull T model) {
        if (model instanceof POI) {
            final POI other = (POI) model;
            if (rank != other.rank) {
                return false;
            }
            return Objects.equals(name, other.name);

        }
        return false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeInt(rank);
        parcel.writeString(name);
        parcel.writeString(number);
        parcel.writeMap(_geoloc);
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public HashMap<String, Double> get_geoloc() {
        return _geoloc;
    }

    public void set_geoloc(HashMap<String, Double> _geoloc) {
        this._geoloc = _geoloc;
    }
}
