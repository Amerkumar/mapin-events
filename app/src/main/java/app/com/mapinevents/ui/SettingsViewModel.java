package app.com.mapinevents.ui;

import androidx.lifecycle.ViewModel;

import app.com.mapinevents.MainRepository;

public class SettingsViewModel extends ViewModel {

    private MainRepository mainRepository;

    public SettingsViewModel() {
        mainRepository = new MainRepository();
    }

    public void setAnnoucementNotification(boolean isTrue) {
        mainRepository.setAnnoucementNotification(isTrue);
    }
}
