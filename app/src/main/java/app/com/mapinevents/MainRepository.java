package app.com.mapinevents;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import app.com.mapinevents.model.Agenda;
import app.com.mapinevents.model.Annoucement;
import app.com.mapinevents.model.Schedule;


public class MainRepository {

    private static final String TAG = MainRepository.class.getSimpleName();
    private final FirebaseFirestore mFirestoreDb;
    private static String VENUE_ID = "2dc80dc0-d46d-11e9-86d0-ab8019709bd3";

    public MainRepository() {
        // Access a Cloud Firestore instance from your Activity
        mFirestoreDb = FirebaseFirestore.getInstance();
    }

    public LiveData<List<Annoucement>> getRecentAnnoucements() {
        String path = "mapin_events/" + VENUE_ID + "/annoucements";
        final MutableLiveData<List<Annoucement>> annoucementList = new MutableLiveData<>();
        annoucementList.setValue(null);
        mFirestoreDb.collection(path)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(5)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        List<Annoucement> annoucements = new ArrayList<>();
                        int rank = 1;
                        for (QueryDocumentSnapshot doc : value) {
                            if (doc.get("timestamp") != null) {
                                Annoucement annoucement = doc.toObject(Annoucement.class);
                                annoucement.setRank(rank++);
                                annoucement.setId(doc.getId());
                                annoucements.add(annoucement);
                            }
                        }
                        annoucementList.postValue(annoucements);
                    }
                });

        return annoucementList;
    }


    public LiveData<List<Agenda>> getAgendaItems() {
        String path = "mapin_events/" + VENUE_ID + "/agenda";
        final MutableLiveData<List<Agenda>> agendaList = new MutableLiveData<>();
        agendaList.setValue(null);
        mFirestoreDb.collection(path)
                .orderBy("start_time", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }


                        List<Agenda> agendas = new ArrayList<>();
                        int rank = 1;
                        for (QueryDocumentSnapshot doc : value) {
                            if (doc.get("start_time") != null) {
                                Agenda agenda = doc.toObject(Agenda.class);
                                agenda.setRank(rank++);
                                agenda.setId(doc.getId());
                                agendas.add(agenda);
                            }
                        }
                        agendaList.postValue(agendas);
                    }
                });

        return agendaList;
    }


    public LiveData<List<Schedule>> getScheduleItems() {
        String path = "mapin_events/" + VENUE_ID + "/schedule";
        final MutableLiveData<List<Schedule>> scheduleList = new MutableLiveData<>();
        scheduleList.setValue(null);
        mFirestoreDb.collection(path)
                .orderBy("start_time", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }


                        List<Schedule> schedules = new ArrayList<>();
                        int rank = 1;
                        for (QueryDocumentSnapshot doc : value) {
                            if (doc.get("start_time") != null) {
                                Schedule schedule = doc.toObject(Schedule.class);
                                schedule.setRank(rank++);
                                schedule.setId(doc.getId());
                                schedules.add(schedule);
                            }
                        }
                        scheduleList.postValue(schedules);
                    }
                });

        return scheduleList;
    }
}
