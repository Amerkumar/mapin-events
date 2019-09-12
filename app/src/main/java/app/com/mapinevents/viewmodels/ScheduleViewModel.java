package app.com.mapinevents.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import app.com.mapinevents.MainRepository;
import app.com.mapinevents.model.Schedule;

public class ScheduleViewModel extends ViewModel {

    private MainRepository mainRepository;
    private LiveData<List<Schedule>> scheduleObservable;

    public ScheduleViewModel() {
        mainRepository = new MainRepository();
        scheduleObservable = mainRepository.getScheduleItems();
    }

    public LiveData<List<Schedule>> getScheduleObservable() {
        return scheduleObservable;
    }
}
