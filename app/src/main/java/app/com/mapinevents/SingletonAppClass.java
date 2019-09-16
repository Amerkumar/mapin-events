package app.com.mapinevents;

public class SingletonAppClass {

    private static final SingletonAppClass ourInstance = new SingletonAppClass();
    private boolean FIRST_APP_OPEN;

    private SingletonAppClass() {
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
