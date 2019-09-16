package app.com.mapinevents.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import app.com.mapinevents.MainRepository;
import app.com.mapinevents.model.Agenda;
import app.com.mapinevents.model.POI;

public class MapInViewModel extends ViewModel {

    private MainRepository mainRepository;
    private LiveData<List<POI>> mPOIObservable;

    public MapInViewModel() {
        mainRepository = new MainRepository();
        mPOIObservable = mainRepository.getPoiItems();

    }

    public LiveData<List<POI>> getmAgendaObservable() {
        return mPOIObservable;
    }
}
