package app.com.mapinevents.viewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.indooratlas.android.sdk.IALocation;

public class SharedViewModel extends ViewModel {

    private final MutableLiveData<Integer> mapTypeSelected = new MutableLiveData<Integer>();
    private final MutableLiveData<IALocation> iaLocationMutableLiveData = new MutableLiveData<>();

    public void setHallsVisible(Boolean hallsVisible) {
        this.hallsVisible.setValue(hallsVisible);
    }

    public void setStallsVisible(Boolean stallsVisible) {
        this.stallsVisible.setValue(stallsVisible);
    }

    public void setThreeDimenViewEnable(Boolean threeDimenViewEnable) {
        this.threeDimenViewEnable.setValue(threeDimenViewEnable);
    }




    public MutableLiveData<Boolean> getHallsVisible() {
        return hallsVisible;
    }

    public MutableLiveData<Boolean> getStallsVisible() {
        return stallsVisible;
    }

    public MutableLiveData<Boolean> getThreeDimenViewEnable() {
        return threeDimenViewEnable;
    }

    private final MutableLiveData<Boolean> hallsVisible = new MutableLiveData<>();
    private final MutableLiveData<Boolean> stallsVisible = new MutableLiveData<>();
    private final MutableLiveData<Boolean> threeDimenViewEnable = new MutableLiveData<>();

    public void setMapTypeSelected(Integer mapTypeSelected) {
        this.mapTypeSelected.setValue(mapTypeSelected);
    }

    public MutableLiveData<Integer> getMapTypeSelected() {
        return mapTypeSelected;
    }

    public MutableLiveData<IALocation> getIaLocationMutableLiveData() {
        return iaLocationMutableLiveData;
    }


    public void setIALocationMutableLiveData(IALocation iaLocationMutableLiveData) {
        this.iaLocationMutableLiveData.postValue(iaLocationMutableLiveData);
    }
}
