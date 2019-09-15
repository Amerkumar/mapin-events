package app.com.mapinevents.viewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {

    private final MutableLiveData<Integer> mapTypeSelected = new MutableLiveData<Integer>();

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
}
