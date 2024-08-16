package com.ecosys.qagenda;

import android.content.Intent;
import android.os.Bundle;

import com.ecosys.qagenda.ui.home.HomeFragment;
import com.ecosys.qagenda.ui.notes.NotesFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.ecosys.qagenda.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        // Retrieve the intent and the fragment identifier
        Intent intent = getIntent();
        String fragmentToLoad = intent.getStringExtra("fragment_to_load");

        // Load the fragment based on the identifier
        if (fragmentToLoad != null) {
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
        }

    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment_activity_main, fragment)
                .commit();
    }

}