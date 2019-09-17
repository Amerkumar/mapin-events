package app.com.mapinevents;

import com.indooratlas.android.sdk.IAExtraInfo;
import com.indooratlas.android.sdk.IALocationManager;

public class SingletonAppClass {

    private static final SingletonAppClass ourInstance = new SingletonAppClass();
    private boolean FIRST_APP_OPEN;

    private IALocationManager iaLocationManager;
    private IAExtraInfo iaExtraInfo;

    private SingletonAppClass() {
    }
    public void setIALocationManager(IALocationManager iaLocationManager) {
        this.iaLocationManager = iaLocationManager;
    }

    public void setIaExtraInfo(IAExtraInfo iaExtraInfo) {
        this.iaExtraInfo = iaExtraInfo;
    }

    public IAExtraInfo getIaExtraInfo() {
        return iaExtraInfo;
    }

    public IALocationManager getIaLocationManager() {
        return iaLocationManager;
    }

    public static SingletonAppClass getInstance() {
        return ourInstance;
    }

    public boolean isFIRST_APP_OPEN() {
        return FIRST_APP_OPEN;
    }

    public void setFIRST_APP_OPEN(boolean FIRST_APP_OPEN) {
        this.FIRST_APP_OPEN = FIRST_APP_OPEN;
    }

}
