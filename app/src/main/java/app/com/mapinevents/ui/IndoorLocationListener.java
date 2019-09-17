package app.com.mapinevents.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.indooratlas.android.sdk.IALocation;
import com.indooratlas.android.sdk.IALocationListener;
import com.indooratlas.android.sdk.IALocationManager;
import com.indooratlas.android.sdk.IALocationRequest;

import app.com.mapinevents.SingletonAppClass;

public class IndoorLocationListener extends LiveData<IALocation> {
    private static IndoorLocationListener instance;
    private IALocationManager mIALocationManager;

    public static IndoorLocationListener getInstance(Context context){
        if (instance == null)
            instance = new IndoorLocationListener(context);
        return instance;
    }

    @SuppressLint("MissingPermission")
    private IndoorLocationListener(Context context) {
        mIALocationManager = IALocationManager.create(context);
        SingletonAppClass.getInstance().setIALocationManager(mIALocationManager);
    }


   IALocationListener mIALocationListener = new IALocationListener() {
       @Override
       public void onLocationChanged(IALocation iaLocation) {
           if (iaLocation != null){
               setValue(iaLocation);
           }
       }

       @Override
       public void onStatusChanged(String s, int i, Bundle bundle) {

       }
   };

    @SuppressLint("MissingPermission")
    @Override
    protected void onActive() {
        super.onActive();
        Log.d("Indoor Listener", "onActive");
        mIALocationManager.requestLocationUpdates(IALocationRequest.create(), mIALocationListener);
//        Log.d("Indoor LocationListener", IALocationManager.getExtraInfo().traceId);
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        Log.d("Indoor Listener", "inActive");
        mIALocationManager.removeLocationUpdates(mIALocationListener);
    }


}
