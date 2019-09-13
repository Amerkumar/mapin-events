package app.com.mapinevents;

public class SingletonAppClass {

    private boolean FIRST_APP_OPEN;

    public boolean isFIRST_APP_OPEN() {
        return FIRST_APP_OPEN;
    }

    public void setFIRST_APP_OPEN(boolean FIRST_APP_OPEN) {
        this.FIRST_APP_OPEN = FIRST_APP_OPEN;
    }

    private static final SingletonAppClass ourInstance = new SingletonAppClass();
    public static SingletonAppClass getInstance() {
        return ourInstance;
    }
    private SingletonAppClass() {
    }
}
