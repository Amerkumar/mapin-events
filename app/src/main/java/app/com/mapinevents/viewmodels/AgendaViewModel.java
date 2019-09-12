package app.com.mapinevents.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import app.com.mapinevents.MainRepository;
import app.com.mapinevents.model.Agenda;
import app.com.mapinevents.model.Annoucement;

public class AgendaViewModel extends ViewModel {

    private MainRepository mainRepository;
    private LiveData<List<Agenda>> mAgendaObservable;

    public AgendaViewModel() {
        mainRepository = new MainRepository();
        mAgendaObservable = mainRepository.getAgendaItems();

    }

    public LiveData<List<Agenda>> getmAgendaObservable() {
        return mAgendaObservable;
    }
}
