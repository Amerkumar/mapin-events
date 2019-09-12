package app.com.mapinevents.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import app.com.mapinevents.MainRepository;
import app.com.mapinevents.model.Annoucement;

public class HomeViewModel extends ViewModel {
    // TODO: Implement the ViewModel


    private MainRepository mMainRepository;
    private final LiveData<List<Annoucement>> mRecentAnnoucements;

    public HomeViewModel() {
        this.mMainRepository = new MainRepository();
        mRecentAnnoucements = mMainRepository.getRecentAnnoucements();
    }

    public LiveData<List<Annoucement>> getmRecentAnnoucements() {
        return mRecentAnnoucements;
    }
}
