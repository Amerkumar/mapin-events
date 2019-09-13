package app.com.mapinevents.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import app.com.mapinevents.R;
import app.com.mapinevents.SingletonAppClass;
import app.com.mapinevents.utils.Utils;

import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity implements
        NavController.OnDestinationChangedListener {

    public static final int RC_SIGN_IN = 11;
    public static boolean FIRST_APP_OPEN = true;
    private BottomNavigationView bottomNavigationView;
    private Toolbar toolbar;
    private NavController navController;
    private ProgressBar progressBar;
    private AppBarLayout appbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Set<Integer> topLevelDestinationsSet = new HashSet<>(Arrays.asList(
                R.id.loginFragment,
                R.id.homeFragment,
                R.id.agendaFragment,
                R.id.scheduleFragment,
                R.id.mapInFragment,
                R.id.moreFragment
        ));

        appbar = findViewById(R.id.appBar);
        toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.progress_horizontal);
        bottomNavigationView = findViewById(R.id.bottom_nav);

        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        AppBarConfiguration appBarConfiguration =
                new AppBarConfiguration.Builder(topLevelDestinationsSet)
                        .build();

        NavigationUI.setupWithNavController(bottomNavigationView, navController);
        NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration);
        navController.addOnDestinationChangedListener(this);

        SingletonAppClass.getInstance().setFIRST_APP_OPEN(true);


    }

    @Override
    public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
        switch (destination.getId()) {
            case R.id.loginFragment:
                Utils.hideView(bottomNavigationView);
//                Utils.showView(appbar);
                break;
            case R.id.mapInFragment:
                Utils.showView(bottomNavigationView);
//                Utils.hideView(appbar);
//                toolbar.inflateMenu(R.menu.map_fragment_menu);
                break;
            default:
//                Utils.showView(appbar);
                Utils.showView(bottomNavigationView);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        NavDestination navDestination = navController.getCurrentDestination();
        if (navDestination != null && navDestination.getId() == R.id.loginFragment) {
            finish();
            return;
        } else if (navDestination != null && navDestination.getId() == R.id.homeFragment) {
            finish();
            return;
        }
        super.onBackPressed();
    }


    public void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    public void hideProgressBar() {
        progressBar.setVisibility(GONE);
    }
}
