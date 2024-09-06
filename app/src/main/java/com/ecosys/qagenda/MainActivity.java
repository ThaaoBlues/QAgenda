package com.ecosys.qagenda;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.ecosys.qagenda.ui.home.HomeFragment;
import com.ecosys.qagenda.ui.notes.NotesFragment;
import com.ecosys.qagenda.ui.notifications.NotificationsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.ecosys.qagenda.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.

        binding.navView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                int itemId = item.getItemId();

                if (itemId == R.id.navigation_home) {
                    loadFragment(new HomeFragment());
                } else if (itemId == R.id.navigation_notes) {
                    Log.d("MainActivity", "navigation_notes clicked");
                    loadFragment(new NotesFragment());
                } else if (itemId == R.id.navigation_notifications) {
                    loadFragment(new NotificationsFragment());
                }

                return true;

            }
        });

        // Retrieve the intent and the fragment identifier
        Intent intent = getIntent();
        String fragmentToLoad = intent.getStringExtra("fragment_to_load");

        // Load the fragment based on the identifier
        if (fragmentToLoad != null) {

            // first, clear old fragment content
            FragmentManager fragmentManager = getSupportFragmentManager();
            Fragment currentFragment = fragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main);

            if (currentFragment != null) {
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.remove(currentFragment).commit();
            }

            // then, load new fragment
            switch (fragmentToLoad) {
                case "agenda":

                    loadFragment(new HomeFragment());
                    break;
                case "notes":
                    loadFragment(new NotesFragment());
                    break;
                // Add more cases for other fragments
                default:
                    // Load a default fragment if needed
                    loadFragment(new HomeFragment());
                    break;
            }
        }else{
            loadFragment(new HomeFragment());
        }

    }


    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment_activity_main, fragment)
                .commit();
    }

}