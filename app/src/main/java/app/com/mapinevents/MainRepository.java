package app.com.mapinevents;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.com.mapinevents.model.Agenda;
import app.com.mapinevents.model.Annoucement;
import app.com.mapinevents.model.POI;
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

    public LiveData<List<POI>> getPoiItems() {
        String path = "mapin_events/" + VENUE_ID + "/pois";
        final MutableLiveData<List<POI>> poiList = new MutableLiveData<>();
        poiList.setValue(null);
        mFirestoreDb.collection(path)
                .orderBy("name", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }


                        List<POI> pois = new ArrayList<>();
                        int rank = 1;
                        for (QueryDocumentSnapshot doc : value) {
                            if (doc.get("name") != null) {
                                POI poi = doc.toObject(POI.class);
                                poi.setRank(rank++);
                                poi.setId(doc.getId());
                                pois.add(poi);
                            }
                        }
                        poiList.postValue(pois);
                    }
                });

        return poiList;
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

    public void setFCMRegistrationToken(FirebaseUser user, String token, boolean isTrue) {
        Map<String, Object> data = new HashMap<>();

        data.put("events_fcm_token", token);
        if (user.getDisplayName() != null)
            data.put("name", user.getDisplayName());
        data.put("email", user.getEmail());
        data.put("last_open_ts", Timestamp.now());
        data.put("mapin_events", true);
        data.put("notification_annoucements", isTrue);
        mFirestoreDb.collection("users").document(FirebaseAuth.getInstance().getUid())
                .set(data, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Main Repo", "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Main Repo", "Error writing document", e);
                    }
                });
    }

    public void setAnnoucementNotification(boolean isTrue) {
        Map<String, Object> data = new HashMap<>();
        data.put("notification_annoucements", isTrue);
        mFirestoreDb.collection("users").document(FirebaseAuth.getInstance().getUid())
                .set(data, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Main Repo", "Error writing document", e);
                    }
                });
    }
}
