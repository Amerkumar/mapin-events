package app.com.mapinevents.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Speaker implements Parcelable {
    private String name;
    private String intro;
    private String url;


    public Speaker(String name, String intro, String url) {
        this.name = name;
        this.intro = intro;
        this.url = url;
    }

    public Speaker() {
    }

    protected Speaker(Parcel in) {
        name = in.readString();
        intro = in.readString();
    }

    public static final Creator<Speaker> CREATOR = new Creator<Speaker>() {
        @Override
        public Speaker createFromParcel(Parcel in) {
            return new Speaker(in);
        }

        @Override
        public Speaker[] newArray(int size) {
            return new Speaker[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(intro);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
